package edu.columbia.ee.elen4901.spaceshooting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Zmy-Apple on 17/4/30.
 */

public class Bullet {
    /**BULLET_STEP_X**/
    static final int BULLET_STEP_X = 3;
    /**BULLET_STEP_Y**/
    static final int BULLET_STEP_Y = 15;
    /**BULLET_WIDTH**/
    static final int BULLET_WIDTH = 40;
    /** position of bullet **/
    public int bulletX = 0;
    public int bulletY = 0;
    /** bulletAnimation **/
    private Animation bulletAnimation = null;
    /** whether  bullet  is disappeared **/
    boolean isVisible = false;
    Context context = null;


    public Bullet(Context context, Bitmap[] frameBitmaps) {
        this.context = context;
        bulletAnimation = new Animation(context, frameBitmaps, true);
    }
    /**initial position **/
    public void init(int x, int y) {
        bulletX = x;
        bulletY = y;
        isVisible = true;
    }
    /**draw bullet**/
    public void DrawBullet(Canvas canvas, Paint paint) {
        if (isVisible) {
            bulletAnimation.DrawAnimation(canvas, paint, bulletX, bulletY);
        }
    }
    /**update postion**/
    public void UpdateBullet(int direction) {
        if (isVisible) {
            bulletY -= direction * BULLET_STEP_Y;
            if (bulletY < 0 || bulletY > 1920) {
                isVisible = false;
            }
        }
//        else {
//            init(aircraftX, aircraftY);
//        }
    }
}
