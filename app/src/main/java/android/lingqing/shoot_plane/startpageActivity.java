package android.lingqing.shoot_plane;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class startpageActivity extends AppCompatActivity {
    String username;

    Context context = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startpage);
    }

    public void startShooting(View view) {
//        Intent intent = new Intent(this, MyHistoryActivity.class);
//        startActivity(intent);

        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    public void openMyHistory(View view) {
        Intent intent = new Intent(this, MyHistoryActivity.class);
        startActivity(intent);
    }

    public void openRanking(View view) {
        Intent intent = new Intent(this, RankingHistory.class);
        startActivity(intent);
    }


    public static final String PREFS_NAME = "MyPrefsFile";



    public void sendRecord(final int score) {

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
                    postDataParams.put("score", score);

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


    }
}
