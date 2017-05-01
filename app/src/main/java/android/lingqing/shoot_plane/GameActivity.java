package android.lingqing.shoot_plane;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GameActivity extends AppCompatActivity {

    String username;
    public static final String PREFS_NAME = "MyPrefsFile";

    GameView gameView = null;
    int count = 0;
    /**游戏主菜单状态**/
    private  static final boolean GAME_RUNNING = false;
    private  static final boolean GAME_OVER = true;
    /**游戏状态**/
    private boolean gameState = GAME_RUNNING;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // get sreen height and weight
        Display display = getWindowManager().getDefaultDisplay();

        // setContentView
        gameView = new GameView(this, display.getWidth(), display.getHeight());
        setContentView(gameView);
    }

    public boolean onTouchEvent(MotionEvent event) {
        // 获得触摸的坐标
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            // 触摸屏幕时刻
            case MotionEvent.ACTION_DOWN:
                gameView.UpdateTouchEvent(x, y);
                break;
            // 触摸并移动时刻
            case MotionEvent.ACTION_MOVE:
                gameView.UpdateTouchEvent(x, y);
                break;
            // 终止触摸时刻
            case MotionEvent.ACTION_UP:
                gameView.UpdateTouchEvent(x, y);
                break;
        }
        return false;
    }

    public void sendRecord() {

        SharedPreferences editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        username = editor.getString("username", "DEFAULT");



        // Do something in response to sign up butto
        Runnable runnable = new Runnable(){
            public void run() {
            String response = null;
            try {

                URL url = new URL("http://54.236.38.109:5000/send_game_data");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("username", username);
                postDataParams.put("score", count);

                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                OutputStream os = conn.getOutputStream();
                os.write(postDataParams.toString().getBytes());
                os.close();
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = NetworkHandler.convertStreamToString(in);

                Log.v("Mydebug", response);

            } catch (Exception e) {
                Log.e("MYDEBUG", "Exception: " + e.getMessage());
            }

            }
        };
        new Thread(runnable).start();


    }

    private class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
        private int screenWidth = 0;
        /**屏幕的宽高**/

        private int screenHeight = 0;
        private int backgroundHeight = 0;




        Paint paint = null;

        /**游戏背景资源 两张图片进行切换让屏幕滚动起来**/
        private Bitmap backgroundBitmap = null;

        /**记录两张背景图片时时更新的Y坐标**/
        private int bgBitposY0 =0;
        private int bgBitposY1 =0;

        /**飞机动画帧数**/

        final static int AIRCRAFT_ALIVE_FRAME_NUM = 6;
        final static int AIRCRAFT_DEAD_FRAME_NUM = 6;





        /**敌人对象的数量**/
        final static int ENEMY_NUM = 5 ;

        /**敌人行走动画帧数**/
        final static int ENEMY_ALIVE_FRAME_NUM = 1 ;
        /**敌人行死亡画帧数**/
        final static int ENEMY_DEAD_FRAME_NUM = 6 ;

        /**敌人飞机偏移量**/
        final  int ENEMY_POS_OFFSET = getWindowManager().getDefaultDisplay().getWidth() / ENEMY_NUM ;

        /**游戏主线程**/
        private Thread mainThread = null;
        /**线程循环标志**/
        private boolean isThreadRunning = false;

        private SurfaceHolder surfaceHolder = null;
        private Canvas canvas = null;

        private Context context = null;




        Aircraft aircraft = null;
        /**敌人类**/
        Enemy[] enemies = null;

//
//
        /**手指在屏幕触摸的坐标**/
        public int touchPosX = 0;
        public int touchPosY = 0;



        /**
         * 构造方法
         *
         * @param context
         */
        public GameView(Context context, int screenWidth, int screenHeight) {
            super(context);
            this.context = context;
            this.paint = new Paint();
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            /**获取mSurfaceHolder**/
            surfaceHolder = getHolder();
            surfaceHolder.addCallback(this);
            setFocusable(true);
            init();
            //setGameState(GAME_RUNNING);
        }

        private void init() {
            /**游戏背景**/
            backgroundBitmap = ReadBitMap(context,R.drawable.map);


            /**第一张图片在屏幕0点，第二张图片在第一张图片上方**/
            bgBitposY0 = 0;
            bgBitposY1 = - backgroundBitmap.getHeight();
            backgroundHeight = backgroundBitmap.getHeight();
            Log.e("guojs","ScreenHeight"+screenHeight);



            /**这里敌人行走动画就1帧**/
            Bitmap[] aliveEnemyFrameBitmaps = new Bitmap[ENEMY_ALIVE_FRAME_NUM];
            aliveEnemyFrameBitmaps[0] = ReadBitMap(context,R.drawable.enemy);
            /**敌人死亡动画**/
            Bitmap [] deadEnemyFrameBitmaps = new Bitmap[ENEMY_DEAD_FRAME_NUM];
            for(int i =0; i< ENEMY_DEAD_FRAME_NUM; i++) {
                deadEnemyFrameBitmaps[i] = ReadBitMap(context,R.drawable.bomb_enemy_0 + i);
            }

            /**创建敌人对象**/
            enemies = new Enemy[ENEMY_NUM];
            for(int i =0; i< ENEMY_NUM; i++) {
                enemies[i] = new Enemy(context,aliveEnemyFrameBitmaps,deadEnemyFrameBitmaps);
                enemies[i].init(i * ENEMY_POS_OFFSET, 0);
            }


            Bitmap[] aliveAircraftFrameBitmaps = new Bitmap[AIRCRAFT_ALIVE_FRAME_NUM];
            for(int i =0; i< AIRCRAFT_ALIVE_FRAME_NUM; i++) {
                aliveAircraftFrameBitmaps[i] = ReadBitMap(context,R.drawable.spaceship);
            }

            Bitmap [] deadAircraftFrameBitmaps = new Bitmap[AIRCRAFT_DEAD_FRAME_NUM];
            for(int i =0; i< AIRCRAFT_DEAD_FRAME_NUM; i++) {
                deadAircraftFrameBitmaps[i] = ReadBitMap(context,R.drawable.bomb_enemy_0 + i);
            }

            aircraft = new Aircraft(context,aliveAircraftFrameBitmaps,deadAircraftFrameBitmaps);



        }
        //设置游戏的状态
//        private void setGameState(int newState) {
//            gameState =  newState;
//        }

        /**
         * 读取本地资源的图片
         *
         * @param context
         * @param resId
         * @return
         */
        public Bitmap ReadBitMap(Context context, int resId) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            opt.inPurgeable = true;
            opt.inInputShareable = true;
            // 获取资源图片
            InputStream is = context.getResources().openRawResource(resId);
            return BitmapFactory.decodeStream(is, null, opt);
        }

        protected void Draw() {
            if(gameState ==GAME_RUNNING) {

                renderBg();
                updateBg();
            }
            else{
                //surfaceDestroyed(surfaceHolder);
                //setContentView(R.layout.activity_game);
                paint = new Paint();
                paint.setTextSize((float)100.0);
                paint.setColor(Color.RED );
                canvas.drawText("SCORE: " + count+ "", 300, 400, paint);

                sendRecord();
                isThreadRunning = false;
            }

        }
        /** 绘制游戏地图 **/
        public void renderBg() {

            canvas.drawBitmap(backgroundBitmap, 0, bgBitposY0, paint);
            canvas.drawBitmap(backgroundBitmap, 0, bgBitposY1, paint);
            /**绘制飞机动画**/
            gameState = aircraft.DrawAircraft(canvas, paint);



            /**绘制敌人动画**/
            for(int i =0; i< ENEMY_NUM; i++) {
                enemies[i].DrawEnemy(canvas, paint);
            }
        }
        private void updateBg() {
            /** 更新场景的参数**/
            bgBitposY0 += 10;
            bgBitposY1 += 10;
            if (bgBitposY0 == backgroundHeight) {
                bgBitposY0 = - backgroundHeight;
            }
            if (bgBitposY1 == backgroundHeight) {
                bgBitposY1 = - backgroundHeight;
            }

            /** 手指触摸屏幕更新飞机坐标 **/

            aircraft.UpdateAircraft( touchPosX, touchPosY);


            /**绘制敌人动画**/
            for(int i =0; i< ENEMY_NUM; i++) {
                enemies[i].UpdateEnemy(screenHeight, ENEMY_NUM, ENEMY_POS_OFFSET);

            }


            //更新子弹与敌人的碰撞
            Collision(aircraft);
            Collision(enemies);

        }

        public void Collision(Aircraft aircraft) {
            //更新子弹与敌人碰撞
            for (int i = 0; i < aircraft.BULLET_NUM; i++) {
                for (int j = 0; j < ENEMY_NUM; j++) {
                    if(enemies[j].isAlive &&(aircraft.aircraftBullets[i].bulletX >= enemies[j].enemyX - 10) && (aircraft.aircraftBullets[i].bulletX <= enemies[j].enemyX + 40)
                            && (aircraft.aircraftBullets[i].bulletY >= enemies[j].enemyY -10) && (aircraft.aircraftBullets[i].bulletY<=enemies[j].enemyY + 10)) {
                        enemies[j].isAlive = false;
                        aircraft.aircraftBullets[i].isVisible = false;
                        count++;
                    }
                }
            }
        }

        public void Collision(Enemy[] enemies) {
            //更新diren与aircraft碰撞

            for (int i = 0; i < ENEMY_NUM; i++) {
                if ((aircraft.aircraftX >= enemies[i].enemyX - 40) && (aircraft.aircraftX <= enemies[i].enemyX + 40)
                        && (aircraft.aircraftY >= enemies[i].enemyY - 40) && (aircraft.aircraftY <= enemies[i].enemyY + 40)

                        ) {
                    enemies[i].isAlive = false;
                    aircraft.isAlive = false;

                }
                for (int j = 0; j < enemies[i].BULLET_NUM; j++) {
                    if ((aircraft.aircraftX >= enemies[i].enemyBullets[j].bulletX - 20) && (aircraft.aircraftX <= enemies[i].enemyBullets[j].bulletX + 20)
                            && (aircraft.aircraftY >= enemies[i].enemyBullets[j].bulletY - 20) && (aircraft.aircraftY <= enemies[i].enemyBullets[j].bulletY + 20)

                            ) {
                        enemies[i].enemyBullets[j].isVisible = false;
                        aircraft.isAlive = false;

                    }
                }
            }
        }



        public void UpdateTouchEvent(int x, int y) {
            // 在这里检测按钮按下播放不同的特效
            if (gameState == GAME_RUNNING) {

                touchPosX = x;
                touchPosY = y;
            }

        }

        @Override
        public void run() {
            while (isThreadRunning) {
                //在这里加上线程安全锁
                synchronized (surfaceHolder) {
                    /**拿到当前画布 然后锁定**/
                    canvas =surfaceHolder.lockCanvas();
                    Draw();
                    /**绘制结束后解锁显示在屏幕上**/
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //当SurfaceView的属性如高宽发生改变时触发
        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
                                   int arg3) {
            // surfaceView的大小发生改变的时候

        }
        //当surfaceView被创建时触发
        @Override
        public void surfaceCreated(SurfaceHolder arg0) {
            /**启动游戏主线程**/
            isThreadRunning = true;
            mainThread = new Thread(this);
            mainThread.start();
        }
        // surfaceView销毁时触发
        @Override
        public void surfaceDestroyed(SurfaceHolder arg0) {
            isThreadRunning = false;
        }
    }
}
