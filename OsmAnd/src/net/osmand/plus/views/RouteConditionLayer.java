package net.osmand.plus.views;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import net.osmand.PlatformUtil;
import net.osmand.binary.BinaryMapDataObject;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.render.OsmandRenderer;

import org.apache.commons.logging.Log;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * Created by wangan on 2016/4/24.
 * Draw route condition map
 */
public class RouteConditionLayer extends OsmandMapLayer{
    private final static Log log = PlatformUtil.getLog(RouteConditionLayer.class);

    OsmandMapTileView view;
    private OsmandRenderer osmandRenderer;
    private OsmandRenderer.RenderingContext tempRenderingContext;

    public Map<String,BinaryMapDataObject> roadConditionObject = new LinkedHashMap<>();
    private Paint paint;

    private void initUI() {
        paint = new Paint();

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
    }


    @Override
    public void initLayer(OsmandMapTileView view) {
        this.view = view;
        osmandRenderer = view.getApplication().getResourceManager().getRenderer().getRenderer();
        roadConditionObject = view.getApplication().getResourceManager().getRenderer().roadConditionObject;

        tempRenderingContext = view.getApplication().getResourceManager().getRenderer().getTempRenderContext();

        initUI();
    }



    @Override
    public void onPrepareBufferImage(Canvas canvas, RotatedTileBox tileBox, DrawSettings settings) {
        Set keySet = roadConditionObject.keySet();
        Iterator it = keySet.iterator();
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
        /*Path path = null;
        while(it.hasNext()){
            String roadName = it.next().toString();
            if(roadName.equals("八一路")){
                log.debug("道路是什么名字呀  " + roadName);
                BinaryMapDataObject road = roadConditionObject.get(roadName);
                for(int i = 0; i<road.getPointsLength(); i++){
                    if(tempRenderingContext == null){
                        log.debug("要死啦要死啦tempRenderingContext还是为空啊");
                    }
                    PointF p = osmandRenderer.calcPoint(road, i, tempRenderingContext);
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
        }*/
    }

    public void DrawRoad(){

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
