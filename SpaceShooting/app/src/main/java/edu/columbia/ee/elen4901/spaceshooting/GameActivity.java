package edu.columbia.ee.elen4901.spaceshooting;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
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
    /**game state**/
    private  static final boolean GAME_RUNNING = false;
    private  static final boolean GAME_OVER = true;

    private boolean gameState = GAME_RUNNING;

    private MediaPlayer mp_background;
    private MediaPlayer mp_bit;
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

        //declare the audio resource to these two MediaPlayer objects
        mp_background = MediaPlayer.create(this, R.raw.main);
        mp_bit = MediaPlayer.create(this, R.raw.blaster);
        //play background music here
        mp_background.start();

        setContentView(gameView);
    }
    @Override
    protected void onDestroy() {
        mp_background.stop();
        super.onDestroy();
    }

    public boolean onTouchEvent(MotionEvent event) {
        // get the touch coordinate
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            // ACTION_DOWN
            case MotionEvent.ACTION_DOWN:
                gameView.UpdateTouchEvent(x, y);
                break;
            // ACTION_MOVE
            case MotionEvent.ACTION_MOVE:
                gameView.UpdateTouchEvent(x, y);
                break;
            // ACTION_UP
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
        /**screen size**/

        private int screenHeight = 0;
        private int backgroundHeight = 0;

        Paint paint = null;

        /**background picture change to look like scroll**/
        private Bitmap backgroundBitmap = null;

        /**record bg y coordinate**/
        private int bgBitposY0 =0;
        private int bgBitposY1 =0;

        /**AIRCRAFT_ALIVE_FRAME_NUM**/

        final static int AIRCRAFT_ALIVE_FRAME_NUM = 6;
        final static int AIRCRAFT_DEAD_FRAME_NUM = 6;

        /**ENEMY_NUM**/
        final static int ENEMY_NUM = 5 ;

        /**ENEMY_ALIVE_FRAME_NUM**/
        final static int ENEMY_ALIVE_FRAME_NUM = 1 ;
        /**ENEMY_DEAD_FRAME_NUM**/
        final static int ENEMY_DEAD_FRAME_NUM = 6 ;

        /**ENEMY_POS_OFFSET**/
        final  int ENEMY_POS_OFFSET = getWindowManager().getDefaultDisplay().getWidth() / ENEMY_NUM ;

        /**game thread**/
        private Thread mainThread = null;
        /**thread loop flag**/
        private boolean isThreadRunning = false;

        private SurfaceHolder surfaceHolder = null;
        private Canvas canvas = null;

        private Context context = null;

        Aircraft aircraft = null;
        /**enemy class**/
        Enemy[] enemies = null;

        /**touch position**/
        public int touchPosX = 600;
        public int touchPosY = 900;

        /**
         * constructor
         *
         * @param context
         */
        public GameView(Context context, int screenWidth, int screenHeight) {
            super(context);
            this.context = context;
            this.paint = new Paint();
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            /**mSurfaceHolder**/
            surfaceHolder = getHolder();
            surfaceHolder.addCallback(this);
            setFocusable(true);
            init();
            //setGameState(GAME_RUNNING);
        }

        private void init() {
            /**background**/
            backgroundBitmap = ReadBitMap(context,R.drawable.map);
            /**The first picture at 0 on the screen, the second picture above the first picture**/
            bgBitposY0 = 0;
            bgBitposY1 = - backgroundBitmap.getHeight();
            backgroundHeight = backgroundBitmap.getHeight();
            Log.e("guojs","ScreenHeight"+screenHeight);
            /**Here the enemy walking animation on a frame**/
            Bitmap[] aliveEnemyFrameBitmaps = new Bitmap[ENEMY_ALIVE_FRAME_NUM];
            aliveEnemyFrameBitmaps[0] = ReadBitMap(context,R.drawable.enemy);
            /**Enemy Death Animation**/
            Bitmap [] deadEnemyFrameBitmaps = new Bitmap[ENEMY_DEAD_FRAME_NUM];
            for(int i =0; i< ENEMY_DEAD_FRAME_NUM; i++) {
                deadEnemyFrameBitmaps[i] = ReadBitMap(context,R.drawable.bomb_enemy_0 + i);
            }
            /**Create an enemy object**/
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
         * Read the image of the local resource
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
            // Read the image of the local resource
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
        /** refresh **/
        public void renderBg() {

            canvas.drawBitmap(backgroundBitmap, 0, bgBitposY0, paint);
            canvas.drawBitmap(backgroundBitmap, 0, bgBitposY1, paint);
            /**refresh aircraft**/
            gameState = aircraft.DrawAircraft(canvas, paint);
            /**refresh enemy**/
            for(int i =0; i< ENEMY_NUM; i++) {
                enemies[i].DrawEnemy(canvas, paint);
            }
        }
        private void updateBg() {
            /** renew backgorund**/
            bgBitposY0 += 10;
            bgBitposY1 += 10;
            if (bgBitposY0 == backgroundHeight) {
                bgBitposY0 = - backgroundHeight;
            }
            if (bgBitposY1 == backgroundHeight) {
                bgBitposY1 = - backgroundHeight;
            }
            /** renew xy **/
            aircraft.UpdateAircraft( touchPosX, touchPosY);
            /**refresh enemy***/
            for(int i =0; i< ENEMY_NUM; i++) {
                enemies[i].UpdateEnemy(screenHeight, ENEMY_NUM, ENEMY_POS_OFFSET);

            }
            //collide
            Collision(aircraft);
            Collision(enemies);
        }

        public void Collision(Aircraft aircraft) {
            //aircraft bullet collide enemy
            for (int i = 0; i < aircraft.BULLET_NUM; i++) {
                for (int j = 0; j < ENEMY_NUM; j++) {
                    if(enemies[j].isAlive &&(aircraft.aircraftBullets[i].bulletX >= enemies[j].enemyX - 10) && (aircraft.aircraftBullets[i].bulletX <= enemies[j].enemyX + 40)
                            && (aircraft.aircraftBullets[i].bulletY >= enemies[j].enemyY -10) && (aircraft.aircraftBullets[i].bulletY<=enemies[j].enemyY + 10)) {
                        enemies[j].isAlive = false;
                        aircraft.aircraftBullets[i].isVisible = false;
                        mp_bit.start();
                        count++;
                    }
                }
            }
        }

        public void Collision(Enemy[] enemies) {
            //enemy or  bullet collide aircraft

            for (int i = 0; i < ENEMY_NUM; i++) {
                if ((aircraft.aircraftX >= enemies[i].enemyX - 40) && (aircraft.aircraftX <= enemies[i].enemyX + 40)
                        && (aircraft.aircraftY >= enemies[i].enemyY - 40) && (aircraft.aircraftY <= enemies[i].enemyY + 40)

                        ) {
                    enemies[i].isAlive = false;
                    aircraft.isAlive = false;

                }
                for (int j = 0; j < enemies[i].BULLET_NUM; j++) {
                    if ((aircraft.aircraftX >= enemies[i].enemyBullets[j].bulletX - 120) && (aircraft.aircraftX <= enemies[i].enemyBullets[j].bulletX + 0)
                            && (aircraft.aircraftY >= enemies[i].enemyBullets[j].bulletY - 15) && (aircraft.aircraftY <= enemies[i].enemyBullets[j].bulletY + 15)

                            ) {
                        enemies[i].enemyBullets[j].isVisible = false;
                        aircraft.isAlive = false;

                    }
                }
            }
        }



        public void UpdateTouchEvent(int x, int y) {
            // renew position
            if (gameState == GAME_RUNNING) {

                touchPosX = x - 77;
                touchPosY = y - 400;
            }

        }

        @Override
        public void run() {
            while (isThreadRunning) {
                //thread lock
                synchronized (surfaceHolder) {
                    /** lock canvas**/
                    canvas =surfaceHolder.lockCanvas();
                    Draw();
                    /**unlock and display**/
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
                                   int arg3) {


        }

        @Override
        public void surfaceCreated(SurfaceHolder arg0) {
            /**start game thread**/
            isThreadRunning = true;
            mainThread = new Thread(this);
            mainThread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder arg0) {
            isThreadRunning = false;
        }
    }
}
