package net.osmand.plus.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import net.osmand.Location;
import net.osmand.data.QuadRect;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.render.OsmandRenderer;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;

/**
 * Created by 10394 on 2016/5/24.
 */
public class RoadConditionLayer extends OsmandMapLayer {
    private OsmandMapTileView view;
    private List<List<Location>>roads = new ArrayList<List<Location>>();


    private Path path;
    private Paint paint;

    @Override
    public void initLayer(OsmandMapTileView view) {
        this.view = view;
        initUI();

    }
    private void initUI() {
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);

    }
    @Override
    public void onPrepareBufferImage(Canvas canvas, RotatedTileBox tileBox, DrawSettings settings) {
        path = null;
        Location o1 =new Location();
        o1.setLatitude(30.540102);
        o1.setLongitude(114.362639);
        Location o2 =new Location();
        o2.setLatitude(30.538392);
        o2.setLongitude(114.366161);
        Location o3 =new Location();
        o3.setLatitude(30.536525);
        o3.setLongitude(114.371514);
        Location o4 =new Location();
        o4.setLatitude(30.534783);
        o4.setLongitude(114.381755);
        List<Location> road = new ArrayList<Location>();
        road.add(0,o1);
        road.add(1,o2);
        road.add(2,o3);
        road.add(3,o4);
        roads.add(0,road);
        if(roads != null){
            if(tileBox.getZoom()>=13 && tileBox.getZoom()<=18){
                drawRoad(tileBox, canvas);
            }
        }

    }

    public void drawRoad(RotatedTileBox tb, Canvas canvas) {
        for(List<Location> road : roads){
            drawLocation(tb, canvas, road);
        }
    }

    public void drawLocation(RotatedTileBox tb, Canvas canvas,List<Location> road){
        if(road.size()>0){
            for (Location o : road) {
                int x = (int) tb.getPixXFromLatLon(o.getLatitude(), o.getLongitude());
                int y = (int) tb.getPixYFromLatLon(o.getLatitude(), o.getLongitude());
                if(path == null){
                    path = new Path();
                    path.moveTo(x, y);
                }else{
                    path.lineTo(x, y);
                }
            }
            canvas.drawPath(path, paint);
            path = null;
        }

    }

    @Override
    public void onDraw(Canvas canvas, RotatedTileBox tileBox, DrawSettings settings) {}
    @Override
    public void destroyLayer() {

    }
    @Override
    public boolean drawInScreenPixels() {
        return false;
    }

    @Override
    public boolean onLongPressEvent(PointF point, RotatedTileBox tileBox) {
        return false;
    }

    @Override
    public boolean onSingleTap(PointF point, RotatedTileBox tileBox) {
        return false;
    }
}
