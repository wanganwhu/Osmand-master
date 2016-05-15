package net.osmand.plus.views.mapwidgets;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import net.osmand.plus.ApplicationMode;
import net.osmand.plus.ContextMenuAdapter;
import net.osmand.plus.ContextMenuItem;
import net.osmand.plus.IconsCache;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.OsmandSettings.MapMarkersMode;
import net.osmand.plus.OsmandSettings.OsmandPreference;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.dialogs.ConfigureMapMenu;
import net.osmand.plus.views.MapInfoLayer;
import net.osmand.plus.views.OsmandMapLayer.DrawSettings;
import net.osmand.plus.views.OsmandMapTileView;
import net.osmand.plus.widgets.IconPopupMenu;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MapWidgetRegistry {

	public static final String COLLAPSED_PREFIX = "+";
	public static final String HIDE_PREFIX = "-";
	public static final String SHOW_PREFIX = "";
	public static final String SETTINGS_SEPARATOR = ";";
	private Set<MapWidgetRegInfo> leftWidgetSet = new TreeSet<>();
	private Set<MapWidgetRegInfo> rightWidgetSet = new TreeSet<>();
	private Map<ApplicationMode, Set<String>> visibleElementsFromSettings = new LinkedHashMap<>();
	private final OsmandSettings settings;


	public MapWidgetRegistry(OsmandSettings settings) {
		this.settings = settings;

		for (ApplicationMode ms : ApplicationMode.values(settings)) {
			String mpf = settings.MAP_INFO_CONTROLS.getModeValue(ms);
			if (mpf.equals(SHOW_PREFIX)) {
				visibleElementsFromSettings.put(ms, null);
			} else {
				LinkedHashSet<String> set = new LinkedHashSet<>();
				visibleElementsFromSettings.put(ms, set);
				Collections.addAll(set, mpf.split(SETTINGS_SEPARATOR));
			}
		}
	}

	public void populateStackControl(LinearLayout stack,
									 ApplicationMode mode, boolean left, boolean expanded) {
		Set<MapWidgetRegInfo> s = left ? this.leftWidgetSet : this.rightWidgetSet;
		for (MapWidgetRegInfo r : s) {
			if (r.visible(mode) || r.widget.isExplicitlyVisible()) {
				stack.addView(r.widget.getView());
			}
		}
		if (expanded) {
			for (MapWidgetRegInfo r : s) {
				if (r.visibleCollapsed(mode) &&
						!r.widget.isExplicitlyVisible()) {
					stack.addView(r.widget.getView());
				}
			}
		}
	}

	public boolean hasCollapsibles(ApplicationMode mode) {
		for (MapWidgetRegInfo r : leftWidgetSet) {
			if (r.visibleCollapsed(mode)) {
				return true;
			}
		}
		for (MapWidgetRegInfo r : rightWidgetSet) {
			if (r.visibleCollapsed(mode)) {
				return true;
			}
		}
		return false;
	}


	public void updateInfo(ApplicationMode mode, DrawSettings drawSettings, boolean expanded) {
		update(mode, drawSettings, expanded, leftWidgetSet);
		update(mode, drawSettings, expanded, rightWidgetSet);
	}

	private void update(ApplicationMode mode, DrawSettings drawSettings, boolean expanded, Set<MapWidgetRegInfo> l) {
		for (MapWidgetRegInfo r : l) {
			if (r.visible(mode) || (r.visibleCollapsed(mode) && expanded)) {
				r.widget.updateInfo(drawSettings);
			}
		}
	}


	public void removeSideWidgetInternal(TextInfoWidget widget) {
		Iterator<MapWidgetRegInfo> it = leftWidgetSet.iterator();
		while (it.hasNext()) {
			if (it.next().widget == widget) {
				it.remove();
			}
		}
		it = rightWidgetSet.iterator();
		while (it.hasNext()) {
			if (it.next().widget == widget) {
				it.remove();
			}
		}
	}

	public <T extends TextInfoWidget> T getSideWidget(Class<T> cl) {
		for (MapWidgetRegInfo ri : leftWidgetSet) {
			if (cl.isInstance(ri)) {
				//noinspection unchecked
				return (T) ri.widget;
			}
		}
		for (MapWidgetRegInfo ri : rightWidgetSet) {
			if (cl.isInstance(ri)) {
				//noinspection unchecked
				return (T) ri.widget;
			}
		}
		return null;
	}

	public MapWidgetRegInfo registerSideWidgetInternal(TextInfoWidget widget,
													   WidgetState widgetState,
													   String key, boolean left, int priorityOrder) {
		MapWidgetRegInfo ii = new MapWidgetRegInfo(key, widget, widgetState, priorityOrder, left);
		processVisibleModes(key, ii);
		if (widget != null) {
			widget.setContentTitle(widgetState.getMenuTitleId());
		}
		if (left) {
			this.leftWidgetSet.add(ii);
		} else {
			this.rightWidgetSet.add(ii);
		}
		return ii;
	}


	public MapWidgetRegInfo registerSideWidgetInternal(TextInfoWidget widget,
													   @DrawableRes int drawableMenu,
													   @StringRes int messageId,
													   String key, boolean left, int priorityOrder) {
		MapWidgetRegInfo ii = new MapWidgetRegInfo(key, widget, drawableMenu,
				messageId, priorityOrder, left);
		processVisibleModes(key, ii);
		if (widget != null) {
			widget.setContentTitle(messageId);
		}
		if (left) {
			this.leftWidgetSet.add(ii);
		} else {
			this.rightWidgetSet.add(ii);
		}
		return ii;
	}

	private void processVisibleModes(String key, MapWidgetRegInfo ii) {
		for (ApplicationMode ms : ApplicationMode.values(settings)) {
			boolean collapse = ms.isWidgetCollapsible(key);
			boolean def = ms.isWidgetVisible(key);
			Set<String> set = visibleElementsFromSettings.get(ms);
			if (set != null) {
				if (set.contains(key)) {
					def = true;
					collapse = false;
				} else if (set.contains(HIDE_PREFIX + key)) {
					def = false;
					collapse = false;
				} else if (set.contains(COLLAPSED_PREFIX + key)) {
					def = false;
					collapse = true;
				}
			}
			if (def) {
				ii.visibleModes.add(ms);
			} else if (collapse) {
				ii.visibleCollapsible.add(ms);
			}
		}
	}

	private void restoreModes(Set<String> set, Set<MapWidgetRegInfo> mi, ApplicationMode mode) {
		for (MapWidgetRegInfo m : mi) {
			if (m.visibleModes.contains(mode)) {
				set.add(m.key);
			} else if (m.visibleCollapsible != null && m.visibleCollapsible.contains(mode)) {
				set.add(COLLAPSED_PREFIX + m.key);
			} else {
				set.add(HIDE_PREFIX + m.key);
			}
		}
	}

	private void setVisibility(MapWidgetRegInfo m, boolean visible, boolean collapsed) {
		ApplicationMode mode = settings.APPLICATION_MODE.get();
		defineDefaultSettingsElement(mode);
		// clear everything
		this.visibleElementsFromSettings.get(mode).remove(m.key);
		this.visibleElementsFromSettings.get(mode).remove(COLLAPSED_PREFIX + m.key);
		this.visibleElementsFromSettings.get(mode).remove(HIDE_PREFIX + m.key);
		m.visibleModes.remove(mode);
		m.visibleCollapsible.remove(mode);
		if (visible && collapsed) {
			// Set "collapsed" state
			m.visibleCollapsible.add(mode);
			this.visibleElementsFromSettings.get(mode).add(COLLAPSED_PREFIX + m.key);
		} else if (visible) {
			// Set "visible" state
			m.visibleModes.add(mode);
			this.visibleElementsFromSettings.get(mode).add(SHOW_PREFIX + m.key);
		} else {
			// Set "hidden" state
			this.visibleElementsFromSettings.get(mode).add(HIDE_PREFIX + m.key);
		}
		saveVisibleElementsToSettings(mode);
		if (m.stateChangeListener != null) {
			m.stateChangeListener.run();
		}
	}

	private void defineDefaultSettingsElement(ApplicationMode mode) {
		if (this.visibleElementsFromSettings.get(mode) == null) {
			LinkedHashSet<String> set = new LinkedHashSet<>();
			restoreModes(set, leftWidgetSet, mode);
			restoreModes(set, rightWidgetSet, mode);
			this.visibleElementsFromSettings.put(mode, set);
		}
	}

	private void saveVisibleElementsToSettings(ApplicationMode mode) {
		StringBuilder bs = new StringBuilder();
		for (String ks : this.visibleElementsFromSettings.get(mode)) {
			bs.append(ks).append(SETTINGS_SEPARATOR);
		}
		settings.MAP_INFO_CONTROLS.set(bs.toString());
	}


	private void resetDefault(ApplicationMode mode, Set<MapWidgetRegInfo> set) {
		for (MapWidgetRegInfo ri : set) {
			ri.visibleCollapsible.remove(mode);
			ri.visibleModes.remove(mode);
			if (mode.isWidgetVisible(ri.key)) {
				if (mode.isWidgetCollapsible(ri.key)) {
					ri.visibleCollapsible.add(mode);
				} else {
					ri.visibleModes.add(mode);
				}
			}
		}
	}

	public void resetToDefault() {
		ApplicationMode appMode = settings.getApplicationMode();
		resetDefault(appMode, leftWidgetSet);
		resetDefault(appMode, rightWidgetSet);
		resetDefaultAppearance(appMode);
		this.visibleElementsFromSettings.put(appMode, null);
		settings.MAP_INFO_CONTROLS.set(SHOW_PREFIX);
	}

	private void resetDefaultAppearance(ApplicationMode appMode) {
//		settings.SHOW_RULER.resetToDefault();		
		settings.SHOW_DESTINATION_ARROW.resetToDefault();
		settings.TRANSPARENT_MAP_THEME.resetToDefault();
		settings.SHOW_STREET_NAME.resetToDefault();
		settings.CENTER_POSITION_ON_MAP.resetToDefault();
		settings.MAP_MARKERS_MODE.resetToDefault();
	}

	public void addControlsAppearance(final MapActivity map, final ContextMenuAdapter cm, ApplicationMode mode) {
		addControlId(map, cm, R.string.map_widget_show_destination_arrow, settings.SHOW_DESTINATION_ARROW);
		addControlId(map, cm, R.string.map_widget_transparent, settings.TRANSPARENT_MAP_THEME);
		addControlId(map, cm, R.string.always_center_position_on_map, settings.CENTER_POSITION_ON_MAP);
		if (mode != ApplicationMode.DEFAULT) {
			addControlId(map, cm, R.string.map_widget_top_text, settings.SHOW_STREET_NAME);
		}
		if (settings.USE_MAP_MARKERS.get()) {
			cm.addItem(new ContextMenuItem.ItemBuilder().setTitleId(R.string.map_markers, map)
					.setDescription(settings.MAP_MARKERS_MODE.get().toHumanString(map))
					.setListener(new ContextMenuAdapter.ItemClickListener() {
						@Override
						public boolean onContextMenuClick(final ArrayAdapter<ContextMenuItem> ad,
														  int itemId, final int pos, boolean isChecked) {
							final OsmandMapTileView view = map.getMapView();
							AlertDialog.Builder bld = new AlertDialog.Builder(view.getContext());
							bld.setTitle(R.string.map_markers);
							final String[] items = new String[MapMarkersMode.values().length];
							for (int i = 0; i < items.length; i++) {
								items[i] = MapMarkersMode.values()[i].toHumanString(map);
							}
							int i = settings.MAP_MARKERS_MODE.get().ordinal();
							bld.setSingleChoiceItems(items, i, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									settings.MAP_MARKERS_MODE.set(MapMarkersMode.values()[which]);
									for (MapWidgetRegInfo info : rightWidgetSet) {
										if ("map_marker_1st".equals(info.key) || "map_marker_2nd".equals(info.key)) {
											setVisibility(info, settings.MAP_MARKERS_MODE.get().isWidgets(), false);
										}
									}
									MapInfoLayer mil = map.getMapLayers().getMapInfoLayer();
									if (mil != null) {
										mil.recreateControls();
									}
									map.refreshMap();
									dialog.dismiss();
									cm.getItem(pos).setDescription(settings.MAP_MARKERS_MODE.get().toHumanString(map));
									ad.notifyDataSetChanged();
								}
							});
							bld.show();
							return false;
						}
					}).setLayout(R.layout.list_item_text_button).createItem());
		}
	}

	private void addControlId(final MapActivity map, ContextMenuAdapter cm,
							  @StringRes int stringId, OsmandPreference<Boolean> pref) {
		cm.addItem(new ContextMenuItem.ItemBuilder().setTitleId(stringId, map)
				.setSelected(pref.get())
				.setListener(new ApearanceItemClickListener(pref, map)).createItem());
	}

	public static boolean distChanged(int oldDist, int dist) {
		return !(oldDist != 0 && oldDist - dist < 100 && Math.abs(((float) dist - oldDist) / oldDist) < 0.01);
	}


	public void addControls(MapActivity map, ContextMenuAdapter cm, ApplicationMode mode) {
		cm.addItem(new ContextMenuItem.ItemBuilder().setTitleId(R.string.map_widget_right, map)
				.setCategory(true).setLayout(R.layout.list_group_title_with_switch).createItem());
		addControls(map, cm, rightWidgetSet, mode);
		if (mode != ApplicationMode.DEFAULT) {
			cm.addItem(new ContextMenuItem.ItemBuilder().setTitleId(R.string.map_widget_left, map)
					.setCategory(true).setLayout(R.layout.list_group_title_with_switch).createItem());
			addControls(map, cm, leftWidgetSet, mode);
		}
		cm.addItem(new ContextMenuItem.ItemBuilder().setTitleId(R.string.map_widget_appearance_rem, map)
				.setCategory(true).setLayout(R.layout.list_group_title_with_switch).createItem());
		addControlsAppearance(map, cm, mode);
	}

	public String getText(Context ctx, final ApplicationMode mode, final MapWidgetRegInfo r) {
		return (r.visibleCollapsed(mode) ? " + " : "  ") + ctx.getString(r.getMessageId());
	}

	public Set<MapWidgetRegInfo> getRightWidgetSet() {
		return rightWidgetSet;
	}

	public Set<MapWidgetRegInfo> getLeftWidgetSet() {
		return leftWidgetSet;
	}

	private void addControls(final MapActivity mapActivity, final ContextMenuAdapter contextMenuAdapter,
							 Set<MapWidgetRegInfo> groupTitle, final ApplicationMode mode) {
		for (final MapWidgetRegInfo r : groupTitle) {
			if (mode == ApplicationMode.DEFAULT) {
				if ("intermediate_distance".equals(r.key)
						|| "distance".equals(r.key)
						|| "time".equals(r.key)) {
					continue;
				}
			}
			if ("map_marker_1st".equals(r.key) || "map_marker_2nd".equals(r.key)) {
				continue;
			}

			final boolean selected = r.visibleCollapsed(mode) || r.visible(mode);
			final String desc = mapActivity.getString(R.string.shared_string_collapse);
			contextMenuAdapter.addItem(new ContextMenuItem.ItemBuilder().setTitleId(r.getMessageId(), mapActivity)
					.setIcon(r.getDrawableMenu())
					.setSelected(selected)
					.setColor(selected ? R.color.osmand_orange : ContextMenuItem.INVALID_ID)
					.setSecondaryIcon(R.drawable.ic_action_additional_option)
					.setDescription(r.visibleCollapsed(mode) ? desc : null)
					.setListener(new ContextMenuAdapter.OnRowItemClick() {
						@Override
						public boolean onRowItemClick(final ArrayAdapter<ContextMenuItem> adapter,
													  final View view,
													  final int itemId,
													  final int pos) {
							View textWrapper = view.findViewById(R.id.text_wrapper);
							IconPopupMenu popup = new IconPopupMenu(view.getContext(), textWrapper);
							MenuInflater inflater = popup.getMenuInflater();
							final Menu menu = popup.getMenu();
							inflater.inflate(R.menu.widget_visibility_menu, menu);
							IconsCache ic = mapActivity.getMyApplication().getIconsCache();
							menu.findItem(R.id.action_show).setIcon(ic.getThemedIcon(R.drawable.ic_action_view));
							menu.findItem(R.id.action_hide).setIcon(ic.getThemedIcon(R.drawable.ic_action_hide));
							menu.findItem(R.id.action_collapse).setIcon(ic.getThemedIcon(R.drawable.ic_action_widget_collapse));

							final int[] menuIconIds = r.getDrawableMenuIds();
							final int[] menuTitleIds = r.getMessageIds();
							final int[] menuItemIds = r.getItemIds();
							int checkedId = r.getItemId();
							boolean selected = r.visibleCollapsed(mode) || r.visible(mode);
							if (menuIconIds != null && menuTitleIds != null && menuItemIds != null
									&& menuIconIds.length == menuTitleIds.length && menuIconIds.length == menuItemIds.length) {
								for (int i = 0; i < menuIconIds.length; i++) {
									int iconId = menuIconIds[i];
									int titleId = menuTitleIds[i];
									int id = menuItemIds[i];
									MenuItem menuItem = menu.add(R.id.single_selection_group, id, i, titleId)
											.setChecked(id == checkedId);
									menuItem.setIcon(menuItem.isChecked() && selected
											? ic.getIcon(iconId, R.color.osmand_orange) : ic.getThemedIcon(iconId));
								}
								menu.setGroupCheckable(R.id.single_selection_group, true, true);
								menu.setGroupVisible(R.id.single_selection_group, true);
							}

							popup.setOnMenuItemClickListener(
									new IconPopupMenu.OnMenuItemClickListener() {
										@Override
										public boolean onMenuItemClick(MenuItem menuItem) {

											switch (menuItem.getItemId()) {
												case R.id.action_show:
													setVisibility(adapter, pos, true, false);
													return true;
												case R.id.action_hide:
													setVisibility(adapter, pos, false, false);
													return true;
												case R.id.action_collapse:
													setVisibility(adapter, pos, true, true);
													return true;
												default:
													if (menuItemIds != null) {
														for (int menuItemId : menuItemIds) {
															if (menuItem.getItemId() == menuItemId) {
																r.changeState(menuItemId);
																MapInfoLayer mil = mapActivity.getMapLayers().getMapInfoLayer();
																if (mil != null) {
																	mil.recreateControls();
																}
																ContextMenuItem item = adapter.getItem(pos);
																item.setIcon(r.getDrawableMenu());
																item.setTitle(mapActivity.getResources().getString(r.getMessageId()));
																adapter.notifyDataSetChanged();
																return true;
															}
														}
													}
											}
											return false;
										}
									});
							popup.show();
							return false;
						}

						@Override
						public boolean onContextMenuClick(ArrayAdapter<ContextMenuItem> a,
														  int itemId, int pos, boolean isChecked) {
							setVisibility(a, pos, isChecked, false);
							return false;
						}

						private void setVisibility(ArrayAdapter<ContextMenuItem> adapter,
												   int position,
												   boolean visible,
												   boolean collapsed) {
							MapWidgetRegistry.this.setVisibility(r, visible, collapsed);
							MapInfoLayer mil = mapActivity.getMapLayers().getMapInfoLayer();
							if (mil != null) {
								mil.recreateControls();
							}
							ContextMenuItem item = adapter.getItem(position);
							item.setSelected(visible);
							item.setColorRes(visible ? R.color.osmand_orange : ContextMenuItem.INVALID_ID);
							item.setDescription(visible && collapsed ? desc : null);
							adapter.notifyDataSetChanged();
						}
					}).createItem());
		}
	}


	public static class MapWidgetRegInfo implements Comparable<MapWidgetRegInfo> {
		public final TextInfoWidget widget;
		@DrawableRes
		private int drawableMenu;
		@StringRes
		private int messageId;
		private WidgetState widgetState;
		public final String key;
		public final boolean left;
		public final int priorityOrder;
		private final Set<ApplicationMode> visibleCollapsible = new LinkedHashSet<>();
		private final Set<ApplicationMode> visibleModes = new LinkedHashSet<>();
		private Runnable stateChangeListener = null;

		public MapWidgetRegInfo(String key, TextInfoWidget widget, @DrawableRes int drawableMenu,
								@StringRes int messageId, int priorityOrder, boolean left) {
			this.key = key;
			this.widget = widget;
			this.drawableMenu = drawableMenu;
			this.messageId = messageId;
			this.priorityOrder = priorityOrder;
			this.left = left;
		}

		public MapWidgetRegInfo(String key, TextInfoWidget widget, WidgetState widgetState,
								int priorityOrder, boolean left) {
			this.key = key;
			this.widget = widget;
			this.widgetState = widgetState;
			this.priorityOrder = priorityOrder;
			this.left = left;
		}

		public int getDrawableMenu() {
			if (widgetState != null) {
				return widgetState.getMenuIconId();
			} else {
				return drawableMenu;
			}
		}

		public int getMessageId() {
			if (widgetState != null) {
				return widgetState.getMenuTitleId();
			} else {
				return messageId;
			}
		}

		public int getItemId() {
			if (widgetState != null) {
				return widgetState.getMenuItemId();
			} else {
				return messageId;
			}
		}

		public int[] getDrawableMenuIds() {
			if (widgetState != null) {
				return widgetState.getMenuIconIds();
			} else {
				return null;
			}
		}

		public int[] getMessageIds() {
			if (widgetState != null) {
				return widgetState.getMenuTitleIds();
			} else {
				return null;
			}
		}

		public int[] getItemIds() {
			if (widgetState != null) {
				return widgetState.getMenuItemIds();
			} else {
				return null;
			}
		}

		public void changeState(int stateId) {
			if (widgetState != null) {
				widgetState.changeState(stateId);
			}
		}

		public boolean visibleCollapsed(ApplicationMode mode) {
			return visibleCollapsible.contains(mode);
		}

		public boolean visible(ApplicationMode mode) {
			return visibleModes.contains(mode);
		}

		public MapWidgetRegInfo required(ApplicationMode... modes) {
			Collections.addAll(visibleModes, modes);
			return this;
		}


		public void setStateChangeListener(Runnable stateChangeListener) {
			this.stateChangeListener = stateChangeListener;
		}

		@Override
		public int hashCode() {
			return getMessageId();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj == null) {
				return false;
			} else if (getClass() != obj.getClass()) {
				return false;
			}
			MapWidgetRegInfo other = (MapWidgetRegInfo) obj;
			return getMessageId() == other.getMessageId();
		}

		@Override
		public int compareTo(@NonNull MapWidgetRegInfo another) {
			if (getMessageId() == another.getMessageId()) {
				return 0;
			}
			if (priorityOrder == another.priorityOrder) {
				return getMessageId() - another.getMessageId();
			}
			return priorityOrder - another.priorityOrder;
		}
	}

	public ContextMenuAdapter getViewConfigureMenuAdapter(final MapActivity map) {
		final ContextMenuAdapter cm = new ContextMenuAdapter();
		cm.setDefaultLayoutId(R.layout.list_item_icon_and_menu);
		cm.addItem(new ContextMenuItem.ItemBuilder().setTitleId(R.string.app_modes_choose, map)
				.setLayout(R.layout.mode_toggles).createItem());
		cm.setChangeAppModeListener(new ConfigureMapMenu.OnClickListener() {

			@Override
			public void onClick() {
				map.getDashboard().updateListAdapter(getViewConfigureMenuAdapter(map));
			}
		});
		final ApplicationMode mode = settings.getApplicationMode();
		addControls(map, cm, mode);
		return cm;
	}

	class ApearanceItemClickListener implements ContextMenuAdapter.ItemClickListener {
		private MapActivity map;
		private OsmandPreference<Boolean> pref;

		public ApearanceItemClickListener(OsmandPreference<Boolean> pref, MapActivity map) {
			this.pref = pref;
			this.map = map;
		}

		@Override
		public boolean onContextMenuClick(ArrayAdapter<ContextMenuItem> a,
										  int itemId, int pos, boolean isChecked) {
			pref.set(!pref.get());
			map.updateApplicationModeSettings();
			a.notifyDataSetChanged();
			return false;
		}
	}

	public static abstract class WidgetState {

		private OsmandApplication ctx;

		public OsmandApplication getCtx() {
			return ctx;
		}

		public WidgetState(OsmandApplication ctx) {
			this.ctx = ctx;
		}

		public abstract int getMenuTitleId();
		public abstract int getMenuIconId();
		public abstract int getMenuItemId();

		public abstract int[] getMenuTitleIds();
		public abstract int[] getMenuIconIds();
		public abstract int[] getMenuItemIds();

		public abstract void changeState(int stateId);
	}
}
