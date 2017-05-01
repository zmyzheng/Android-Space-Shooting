package edu.columbia.ee.elen4901.spaceshooting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyHistoryActivity extends AppCompatActivity {

    SimpleAdapter adapter;
    List<Map<String, Object>> adapterdata;

    public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_history);

        ListView list = (ListView) findViewById(R.id.listViewMyHistory);
        adapterdata = new ArrayList<>();
        adapter = new SimpleAdapter(this,
                adapterdata,
                R.layout.vlist,
                new String[]{"score","info"},
                new int[]{R.id.score,R.id.info});
        list.setAdapter(adapter);
        new Thread(runnable).start();
    }

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            String response = null;
            try {

                SharedPreferences editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String user = editor.getString("username", "DEFAULT");

                URL url = new URL("http://54.236.38.109:5000/get_person_record?username=" + user );
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = NetworkHandler.convertStreamToString(in);

            } catch (Exception e) {
                Log.e("MYDEBUG", "Exception: " + e.getMessage());
            }

            try{
                JSONObject jsonObj = new JSONObject(response);
                JSONArray payload = jsonObj.getJSONArray("payload");
                for (int i = 0; i < payload.length(); i++) {
                    JSONObject p = payload.getJSONObject(i);
                    String score = p.getString("score");
                    String username = p.getString("username");
                    String time = p.getString("time");

                    Map<String, Object> map = new HashMap<>();
                    map.put("score", score);
                    map.put("info", username+" on "+time);
                    adapterdata.add(map);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }catch (Exception e){
                Log.e("MYDEBUG", "Exception: " + e.getMessage());
            }

        }
    };
}

