package edu.columbia.ee.elen4901.spaceshooting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.InputStream;

/**
 * Created by Zmy-Apple on 17/4/30.
 */

public class Animation {
    /** lastFramePlayTime **/
    private long lastFramePlayTime = 0;
    /** frameID **/
    private int frameID = 0;
    /** frameCount **/
    private int frameNum = 0;
    /** animation of picture resource **/
    private Bitmap[] frameBitmaps = null;
    /** isLoop **/
    private boolean isLoop = false;
    /** whether animation play isFinished **/
    boolean isFinished = false;
    /** frameInterval == 30ms **/
    private static final int FRAME_INTERVAL = 30;

    /**
     * constructor
     * @param context
     * @param frameBitmaps
     * @param isLoop
     */
    public Animation(Context context, Bitmap[] frameBitmaps, boolean isLoop) {
        this.frameNum = frameBitmaps.length;
        this.frameBitmaps = frameBitmaps;
        this.isLoop = isLoop;
    }
    /**
     * constructor
     * @param context
     * @param frameBitmapIDs
     * @param isLoop
     */
    public Animation(Context context, int[] frameBitmapIDs, boolean isLoop) {
        this.frameNum = frameBitmapIDs.length;
        this.frameBitmaps = new Bitmap[frameNum];
        for(int i =0; i < frameNum; i++) {
            frameBitmaps[i] = ReadBitMap(context,frameBitmapIDs[i]);
        }
        this.isLoop = isLoop;
    }

    private Bitmap ReadBitMap(Context context, int frameBitmapID) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(frameBitmapID);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    //reset animation
    public void reset() {
        lastFramePlayTime = 0;
        frameID =0;
        isFinished= false;
    }

    /**
     * draw one frame
     * @param canvas
     * @param paint
     * @param x
     * @param y
     * @param frameID
     */
    public void DrawFrame(Canvas canvas, Paint paint, int x, int y, int frameID) {
        canvas.drawBitmap(frameBitmaps[frameID], x, y, paint);
    }

    /**
     * draw animation
     * @param canvas
     * @param paint
     * @param x
     * @param y
     */
    public boolean DrawAnimation(Canvas canvas, Paint paint, int x, int y) {
        //if not finished, continue
        if (!isFinished) {
            canvas.drawBitmap(frameBitmaps[frameID], x, y, paint);
            long time = System.currentTimeMillis();
            if (time - lastFramePlayTime > FRAME_INTERVAL) {
                frameID++;
                lastFramePlayTime = time;
                if (frameID >= frameNum) {
                    //animation play over
                    isFinished = true;
                    if (isLoop) {
                        //loop
                        isFinished = false;
                        frameID = 0;
                    }
                }
            }
        }
        return isFinished;
    }
}
