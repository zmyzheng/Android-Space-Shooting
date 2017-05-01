package android.lingqing.shoot_plane;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.InputStream;

/**
 * Created by Zmy-Apple on 17/4/30.
 */

public class Aircraft {

    static final int AIRCRAFT_STEP = 50;

    public int aircraftX = 600;
    public int aircraftY = 600;
    /** aliveEnemyAnimation **/
    private Animation aliveAircraftAnimation = null;
    /** deadEnemyAnimation **/
    private Animation deadAircraftAnimation = null;

    public Bullet[] aircraftBullets = null;
    static final int BULLET_NUM = 25;
    static final int BULLET_FRAME_NUM = 4;
    /**没过500毫秒发射一颗子弹**/
    final static int BULLET_INTERVAL = 500;

    /**子弹图片向上偏移量处理触屏**/
    final static int BULLET_UP_OFFSET = 40;
    /**子弹图片向左偏移量处理触屏**/
    final static int BULLET_LEFT_OFFSET = 0;
    /**初始化发射子弹ID升序**/
    public int bulletCount = 0;
    /**上一颗子弹发射的时间**/
    public Long lastBulletSendTime = 0L;

    /**状态**/
    boolean isAlive = true;
    Context context = null;

    public Aircraft(Context context, Bitmap[] frameBitmap, Bitmap[] deadBitmap) {
        this.context = context;
        aliveAircraftAnimation = new Animation(context, frameBitmap, true);
        deadAircraftAnimation = new Animation(context, deadBitmap, false);
        init(600,600);
    }
    /**初始化坐标**/
    public void init(int x, int y) {
        aircraftX = x;
        aircraftY = y;

        isAlive = true;

        aliveAircraftAnimation.reset();
        deadAircraftAnimation.reset();

        Bitmap[] bulletFrameBitmaps = new Bitmap[BULLET_FRAME_NUM];
        for(int i=0; i<BULLET_FRAME_NUM;i++) {
            bulletFrameBitmaps[i] = ReadBitMap(context,R.drawable.bullet);
        }
        /**创建子弹类对象**/
        aircraftBullets = new Bullet[BULLET_NUM];
        for (int i =0; i< BULLET_NUM;i++) {
            aircraftBullets[i] = new Bullet(context,bulletFrameBitmaps);
        }
    }
    /**绘制动画**/
    public boolean DrawAircraft(Canvas canvas, Paint paint) {

        if(isAlive) {
            aliveAircraftAnimation.DrawAnimation(canvas, paint, aircraftX, aircraftY);
            /**绘制子弹动画*/
            for (int i =0; i < BULLET_NUM; i++) {
                aircraftBullets[i].DrawBullet(canvas, paint);
            }
            return false;
        }
        else {
            return deadAircraftAnimation.DrawAnimation(canvas, paint, aircraftX, aircraftY);

        }


    }
    /**更新状态**/
    public void UpdateAircraft(int touchPosX, int touchPosY) {
        if (isAlive) {
            if (aircraftX < touchPosX) {
                aircraftX += AIRCRAFT_STEP;
            } else {
                aircraftX -= AIRCRAFT_STEP;
            }
            if (aircraftY < touchPosY) {
                aircraftY += AIRCRAFT_STEP;
            } else {
                aircraftY -= AIRCRAFT_STEP;
            }

            if (Math.abs(aircraftX - touchPosX) <= AIRCRAFT_STEP) {
                aircraftX = touchPosX;
            }
            if (Math.abs(aircraftY - touchPosY) <= AIRCRAFT_STEP) {
                aircraftY = touchPosY;
            }
            //当敌人状态为死亡并且死亡动画播放完毕 不在绘制敌人

        }
        else {
            if(deadAircraftAnimation.isFinished) {

            }
        }
        /** 更新子弹动画 **/
        for (int i = 0; i < BULLET_NUM; i++) {

            aircraftBullets[i].UpdateBullet(1);

        }

        /**根据时间初始化为发射的子弹**/
        if (bulletCount < BULLET_NUM) {
            long now = System.currentTimeMillis();
            if (now - lastBulletSendTime >= BULLET_INTERVAL) {
                aircraftBullets[bulletCount].init(aircraftX - BULLET_LEFT_OFFSET, aircraftY - BULLET_UP_OFFSET);
                lastBulletSendTime = now;
                bulletCount++;
            }
        }
        else {
            bulletCount = 0;
        }
    }

    public Bitmap ReadBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }


}
