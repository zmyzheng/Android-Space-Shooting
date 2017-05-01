package android.lingqing.shoot_plane;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class registerActivity extends AppCompatActivity {

    String username;
    String password;
//    public static final String PREFS_NAME = "MyPrefsFile";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = "";
        password = "";
    }

    public class MyShow implements Runnable {
        String hint;

        public MyShow(String s) {
            this.hint = s;
        }

        @Override
        public void run(){
            Toast.makeText(getApplicationContext(), hint, Toast.LENGTH_SHORT).show();
        }
    }


    Runnable runnable = new Runnable(){
        public void run() {
            String response = null;
            PrintWriter out = null;
            try {
                URL url = new URL("http://54.236.38.109:5000/register");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("username", username);
                postDataParams.put("password", password);

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
            String hint = "";
            try{
                JSONObject jsonObj = new JSONObject(response);
                String result = jsonObj.getString("status");

                if(result.equals("success")){
                    hint = "Sign up success & Go back to sign up";
//                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
//                    startActivity(intent);
                }else{
                    hint = "Sign up fail." + jsonObj.getString("payload");
                }

                runOnUiThread(new MyShow(hint));
            }catch (Exception e){
                Log.e("MYDEBUG", "Exception: " + e.getMessage());
            }

        }
    };
    public void onRegister(View view){
        EditText txt_username = (EditText)findViewById(R.id.editText);
        username = txt_username.getText().toString().trim();
        EditText txt_password = (EditText)findViewById(R.id.editText2);
        password = txt_password.getText().toString().trim();
//
//        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
//        editor.putString("username", username);
//        editor.putString("password", password);
//
//        editor.commit();

        new Thread(runnable).start();
    }
}
