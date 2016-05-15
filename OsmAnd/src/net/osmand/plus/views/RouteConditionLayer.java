package net.osmand.plus.views;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import net.osmand.PlatformUtil;
import net.osmand.binary.BinaryMapDataObject;
import net.osmand.data.RotatedTileBox;

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
    public Map<String,BinaryMapDataObject> roadConditionObject = new LinkedHashMap<>();
    private Paint paint;

    private Path path = new Path();
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
        roadConditionObject = view.getApplication().getResourceManager().getRenderer().roadConditionObject;
        initUI();
    }



    @Override
    public void onPrepareBufferImage(Canvas canvas, RotatedTileBox tileBox, DrawSettings settings) {
        path.reset();
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
        while(it.hasNext()){
            String roadName = it.next().toString();
            if(roadName.equals("八一路")){
                log.debug("道路是什么名字呀  "+roadName);
                BinaryMapDataObject road = roadConditionObject.get(roadName);
                path.moveTo(road.getPoint31XTile(0),road.getPoint31YTile(0));
                for(int i = 1; i<road.getPointsLength(); i++){
                    path.lineTo(road.getPoint31XTile(i),road.getPoint31YTile(i));
                }
                canvas.drawPath(path,paint);
                break;
            }
        }
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
