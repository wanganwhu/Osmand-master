package net.osmand.plus.views;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import net.osmand.Location;
import net.osmand.PlatformUtil;
import net.osmand.binary.BinaryMapDataObject;
import net.osmand.data.QuadPointDouble;
import net.osmand.data.QuadRect;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.render.OsmandRenderer;
import net.osmand.util.MapUtils;

import org.apache.commons.logging.Log;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by wangan on 2016/4/24.
 * Draw route condition map
 */
public class RouteConditionLayer extends OsmandMapLayer{
    private final static Log log = PlatformUtil.getLog(RouteConditionLayer.class);

    public static final int TILE_SIZE = 256;

    OsmandMapTileView view;
    private OsmandRenderer osmandRenderer;
    //private List<Location> points = new ArrayList<Location>();
    public Map<String,BinaryMapDataObject> roadConditionObject = new LinkedHashMap<String, BinaryMapDataObject>();
    private RotatedTileBox requestedBox = null;

    private Paint paint;
    private Path path;

    public class Flag{
        public double leftX;
        public double topY;
        public int width;
        public int height;
        public double tileDivisor;
        public float rotate;
        public float cosRotateTileSize;
        public float sinRotateTileSize;
    }

    private void initUI() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        log.debug("inintUI会调用几次呢？");
    }


    @Override
    public void initLayer(OsmandMapTileView view) {
        this.view = view;
        osmandRenderer = view.getApplication().getResourceManager().getRenderer().getRenderer();
        roadConditionObject = view.getApplication().getResourceManager().getRenderer().roadConditionObject;
        initUI();
    }

    private void updatePaints(DrawSettings nightMode, RotatedTileBox tileBox){
        OsmandRenderer.RenderingContext rc = new OsmandRenderer.RenderingContext(view.getContext());
        rc.setDensityValue((float) tileBox.getMapDensity());
    }


    @Override
    public void onPrepareBufferImage(Canvas canvas, RotatedTileBox tileBox, DrawSettings settings) {
        /*currentRenderingContext = view.getApplication().getResourceManager().getRenderer().getTempRenderContext();
        if(currentRenderingContext == null)
            log.debug("rc为null");
        currentRenderingContext.setDensityValue((float) tileBox.getMapDensity());
        Set keySet = roadConditionObject.keySet();
        Iterator it = keySet.iterator();
        */
        /*while(it.hasNext()){
            Object roadName = it.next();
            BinaryMapDataObject road = roadConditionObject.get(roadName);
            //////////////////////////////////////
            //查看下road的名字
            log.debug(roadName.toString());
            path.moveTo(road.getPoint31XTile(0),road.getPoint31YTile(0));
            for(int i = 1; i < road.getPointsLength();i++){
                path.lineTo(road.getPoint31XTile(i),road.getPoint31YTile(i));
            }
            canvas.drawPath(path,paint);
            path.reset();
        }*/

        requestedBox = new RotatedTileBox(tileBox);
        //log.debug("看看requestedBox是什么 "+ requestedBox.toString());
        Flag flag = new Flag();
        final QuadPointDouble lt = requestedBox.getLeftTopTile(requestedBox.getZoom());
        double cfd = MapUtils.getPowZoom(requestedBox.getZoomFloatPart())* requestedBox.getMapDensity();
        lt.x *= cfd;
        lt.y *= cfd;
        final double tileDivisor = MapUtils.getPowZoom(31 - requestedBox.getZoom()) / cfd;
        flag.leftX = lt.x;
        flag.topY = lt.y;
        flag.rotate = requestedBox.getRotate();
        flag.width = requestedBox.getPixWidth();
        flag.height = requestedBox.getPixHeight();
        flag.tileDivisor = tileDivisor;
        flag.cosRotateTileSize = (float) (Math.cos((float) Math.toRadians(flag.rotate)) * TILE_SIZE);
        flag.sinRotateTileSize = (float) (Math.sin((float) Math.toRadians(flag.rotate)) * TILE_SIZE);


        Path path = null;
        Set keySet = roadConditionObject.keySet();
        Iterator it = keySet.iterator();
        //log.debug(String.format("roadObject的size %s", roadObject.size() +" "));
        while(it.hasNext()){
            String roadName = it.next().toString();
            if(roadName.equals("八一路")){
                BinaryMapDataObject road = roadConditionObject.get(roadName);
                for(int i = 0; i<road.getPointsLength(); i++){
                    PointF p =calcPoint(road, i, flag);
                    if(path == null){
                        path = new Path();
                        path.moveTo(p.x,p.y);
                    }else{
                        path.lineTo(p.x, p.y);
                    }
                }
                canvas.drawPath(path,paint);
                break;
            }
        }


    }

    public PointF calcPoint(BinaryMapDataObject o, int ind, Flag flag){
        return calcPoint(o.getPoint31XTile(ind), o.getPoint31YTile(ind), flag);
    }
    public PointF calcPoint(int xt, int yt, Flag flag){
        PointF pointF = new PointF();
        double tx = xt / flag.tileDivisor;
        double ty = yt / flag.tileDivisor;
        double dTileX = (tx - flag.leftX);
        double dTileY = (ty - flag.topY);
        float x = (float) (flag.cosRotateTileSize * dTileX - flag.sinRotateTileSize * dTileY);
        float y = (float) (flag.sinRotateTileSize * dTileX + flag.cosRotateTileSize * dTileY);
        pointF.x = x;
        pointF.y = y;
        return pointF;
    }



    @Override
    public void onDraw(Canvas canvas, RotatedTileBox tileBox, DrawSettings settings) {

    }

    @Override
    public void destroyLayer() {

    }

    @Override
    public boolean drawInScreenPixels() {
        return false;
    }
}
