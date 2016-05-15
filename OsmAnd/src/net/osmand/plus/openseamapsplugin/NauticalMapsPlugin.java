package net.osmand.plus.openseamapsplugin;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import net.osmand.IndexConstants;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.download.DownloadActivity;
import net.osmand.plus.render.RendererRegistry;

public class NauticalMapsPlugin extends OsmandPlugin {

	public static final String ID = "nauticalPlugin.plugin";
	public static final String COMPONENT = "net.osmand.nauticalPlugin";
	private static String previousRenderer = RendererRegistry.DEFAULT_RENDER;
	private OsmandApplication app;
	

	public NauticalMapsPlugin(OsmandApplication app) {
		this.app = app;
	}
	
	@Override
	public int getLogoResourceId() {
		return R.drawable.ic_plugin_nautical_map;
	}
	
	@Override
	public int getAssetResourceName() {
		return R.drawable.nautical_map;
	}

	@Override
	public String getDescription() {
		return app.getString(net.osmand.plus.R.string.plugin_nautical_descr);
	}

	@Override
	public String getName() {
		return app.getString(net.osmand.plus.R.string.plugin_nautical_name);
	}

	@Override
	public String getHelpFileName() {
		return "feature_articles/nautical-charts.html";
	}

	@Override
	public boolean init(final OsmandApplication app, final Activity activity) {
		if(activity != null) {
			// called from UI 
			previousRenderer = app.getSettings().RENDERER.get(); 
			app.getSettings().RENDERER.set(RendererRegistry.NAUTICAL_RENDER);
			if(!app.getResourceManager().getIndexFileNames().containsKey("World_seamarks"+
					 IndexConstants.BINARY_MAP_INDEX_EXT)){
				AlertDialog.Builder dlg = new AlertDialog.Builder(activity);
				dlg.setMessage(net.osmand.plus.R.string.nautical_maps_missing);
				dlg.setPositiveButton(R.string.shared_string_ok, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final Intent intent = new Intent(activity, app.getAppCustomization().getDownloadIndexActivity());
						intent.putExtra(DownloadActivity.TAB_TO_OPEN, DownloadActivity.DOWNLOAD_TAB);
						activity.startActivity(intent);
					}
				});
				dlg.setNegativeButton(R.string.shared_string_cancel, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						app.getSettings().RENDERER.set(previousRenderer);						
					}
				});
				dlg.show();
			}
			
		}
		return true;
	}
	
	@Override
	public void disable(OsmandApplication app) {
		super.disable(app);
		if(app.getSettings().RENDERER.get().equals(RendererRegistry.NAUTICAL_RENDER)) {
			app.getSettings().RENDERER.set(previousRenderer);
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Class<? extends Activity> getSettingsActivity() {
		return null;
	}
}
