package edu.columbia.ee.elen4901.spaceshooting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.InputStream;
import java.util.Random;

/**
 * Created by Zmy-Apple on 17/4/30.
 */

public class Enemy {

    //static final int ALIVE = 1;
    //static final int DEAD = 0;
    /**ENEMY_STEP_Y**/
    static final int ENEMY_STEP_Y = 5;
    /**BULLET_WIDTH**/
    static final int BULLET_WIDTH = 40;
    /** enemy position **/
    public int enemyX = 0;
    public int enemyY = 0;
    /** aliveEnemyAnimation **/
    private Animation aliveEnemyAnimation = null;
    /** deadEnemyAnimation **/
    private Animation deadEnemyAnimation = null;
    /****/



    boolean isVisible = false;
    /**state**/
    boolean isAlive = true;
    Context context = null;

    public Bullet[] enemyBullets = null;
    static final int BULLET_NUM = 2;
    static final int BULLET_FRAME_NUM = 4;

    final static int BULLET_INTERVAL = 3000;


    final static int BULLET_UP_OFFSET = 0;

    final static int BULLET_LEFT_OFFSET = 0;

    public int bulletCount = 0;

    public Long lastBulletSendTime = 0L;

    public Enemy(Context context, Bitmap[] frameBitmap, Bitmap[] deadBitmap) {
        this.context = context;
        aliveEnemyAnimation = new Animation(context, frameBitmap, true);
        deadEnemyAnimation = new Animation(context, deadBitmap, false);
        Bitmap[] bulletFrameBitmaps = new Bitmap[BULLET_FRAME_NUM];
        for(int i=0; i<BULLET_FRAME_NUM;i++) {
            bulletFrameBitmaps[i] = ReadBitMap(context,R.drawable.enemybullet);
        }

        enemyBullets = new Bullet[BULLET_NUM];
        for (int i =0; i< BULLET_NUM;i++) {
            enemyBullets[i] = new Bullet(context,bulletFrameBitmaps);
        }
    }

    public void init(int x, int y) {
        enemyX = x;
        enemyY = y;
        isVisible = true;
        isAlive = true;
        aliveEnemyAnimation.reset();
        deadEnemyAnimation.reset();

    }

    public void DrawEnemy(Canvas canvas, Paint paint) {
        if (isVisible) {
            if(isAlive) {
                aliveEnemyAnimation.DrawAnimation(canvas, paint, enemyX, enemyY);
                for (int i =0; i < BULLET_NUM; i++) {
                    enemyBullets[i].DrawBullet(canvas, paint);
                }
            }
            else {
                deadEnemyAnimation.DrawAnimation(canvas, paint, enemyX, enemyY);
            }

        }
    }

    public void UpdateEnemy(int screenHeight, int ENEMY_NUM, int ENEMY_POS_OFFSET ) {
        if (isVisible) {
            enemyY += ENEMY_STEP_Y;
            if (enemyY > screenHeight) {
                isVisible = false;
            }

            if(!isAlive) {
                if(deadEnemyAnimation.isFinished) {
                    isVisible = false;
                }
            }
            for (int i = 0; i < BULLET_NUM; i++) {
                enemyBullets[i].UpdateBullet(-1);
                if (!enemyBullets[i].isVisible) {

                        enemyBullets[i].init(enemyX - BULLET_LEFT_OFFSET, enemyY - BULLET_UP_OFFSET);

                }
            }
            if (bulletCount < BULLET_NUM) {
                long now = System.currentTimeMillis();
                if (now - lastBulletSendTime >= BULLET_INTERVAL) {
                    enemyBullets[bulletCount].init(enemyX - BULLET_LEFT_OFFSET, enemyY - BULLET_UP_OFFSET);
                    lastBulletSendTime = now;
                    bulletCount++;
                }
            }
        }
        else {
            init(UtilRandom(0,ENEMY_NUM) *ENEMY_POS_OFFSET, 0);
        }
    }

    public Bitmap ReadBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;

        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }


    /**
     * random number
     * @param botton
     * @param top
     * @return
     */
    private int UtilRandom(int botton, int top) {
        return ((Math.abs(new Random().nextInt()) % (top - botton)) + botton);
    }
}
