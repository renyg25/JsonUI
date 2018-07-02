package com.billy.jsonui;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String SERVERURL = "http://www.djhub.net/api/top?type=downloads";

    private TextView textView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        textView = (TextView) findViewById(R.id.textView);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        parseJsonUsingGson();
    }

    private void byAsyncTask(){
        HttpAsncTask task = new HttpAsncTask();
        task.execute(SERVERURL);
    }

    private void byThread(){

        textView.setText("Requesting...");
        progressDialog.show();

        final Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                final Message message = msg;

                //Toast.makeText(MainActivity.Current, "Handler Msg " + message.obj, Toast.LENGTH_SHORT).show();

                try
                {
                    progressDialog.hide();
                    textView.setText("Handler");
                }catch (Exception e){
                    System.out.print(e.getStackTrace());
                }


                //textView.setText(message.obj.toString());
//                MainActivity.Current.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.Current, "Handler Msg " + message.obj, Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                String urlString = "http://www.djhub.net/api/top?type=downloads";
                URL url = null;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }


                HttpURLConnection urlConnection = null;
                try {

                    Thread.sleep(3000);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    String json = "";
                    String line;
                    while((line = br.readLine()) != null){
                        json += line;
                    }

                    br.close();

                    System.out.println(json);

                    //textView.setText("Done");
//                    final String js = json;
//                    Current.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            textView.setText(js);
//                        }
//                    });


                }catch (Exception e){
                    System.err.println(e.getStackTrace());
                }finally {
                    urlConnection.disconnect();


                    Message msg = new Message();
                    msg.obj = "Json from Msg";
                    handler.sendMessage(msg);

                }

            }
        }).start();


    }

    private String getData(String url){
        if (url == null || url.length() == 0)
            return url;

        URL requestUrl;
        try {
            requestUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();

            return null;
        }

        String json = "";
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            String line = null;
            while((line = br.readLine()) != null){
                json += line;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (connection != null)
                connection.disconnect();
        }

        return  json;
    }

    public void onClick(View view) throws IOException {
        Toast.makeText(this, "Clicked", Toast.LENGTH_LONG).show();
        //byThread();

        byAsyncTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<MusicItem> parseJson(String json){
        List<MusicItem> items = new ArrayList<>();

        try {
            InputStream input = new ByteArrayInputStream(json.getBytes());
            JsonReader reader = new JsonReader(new InputStreamReader(input));

            reader.beginArray();
            while (reader.hasNext()){
                MusicItem item = new MusicItem();

                reader.beginObject();
                while (reader.hasNext()){
                    String name = reader.nextName();
                    if (name.equals("_id"))
                        item.setId(reader.nextString());
                    else if(name.equals("count"))
                        item.setCount(reader.nextInt());
                    else  if(name.equals("date"))
                        item.setDate(reader.nextString());
                    else  if (name.equals("name"))
                        item.setName(reader.nextString());
                    else if(name.equals("dir"))
                        item.setDir(reader.nextString());
                    else if (name.equals("urlKey"))
                        item.setUrlKey(reader.nextString());
                    else if(name.equals("url"))
                        item.setUrl(reader.nextString());
                    else
                        reader.skipValue();
                }
                reader.endObject();

                items.add(item);
            }

            reader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return items;
    }

    private void parseJsonUsingGson(){
        String json = "{\n" +
                "   \"users\":[  \n" +
                "   {  \n" +
                "      \"id\":\"1\",\n" +
                "      \"phone\":\"111222333\",\n" +
                "      \"image\":\"2014-12-13-08-08-29.jpg\",\n" +
                "      \"name\":\"john\",\n" +
                "      \"surname\": \"doe\"\n" +
                "   },\n" +
                "   {  \n" +
                "      \"id\":\"2\",\n" +
                "      \"phone\":\"111222444\",\n" +
                "      \"image\":\"2014-12-13-08-09-29.jpg\",\n" +
                "      \"name\":\"anon\",\n" +
                "      \"surname\": \"nona\"\n" +
                "   },\n" +
                "   {  \n" +
                "      \n" +
                "      \"id\":\"3\",\n" +
                "      \"phone\":\"111222555\",\n" +
                "      \"image\":\"2014-12-13-08-10-29.jpg\",\n" +
                "      \"name\":\"filan\",\n" +
                "      \"surname\": \"fisteku\"\n" +
                "   }\n" +
                "]\n" +
                "}";


        UserJO userJO = new Gson().fromJson(json, UserJO.class);

    }

    private class HttpAsncTask extends AsyncTask<String, Double, List<MusicItem>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected List<MusicItem> doInBackground(String... strings) {
            String url = strings[0];
            String json = getData(url);
            List<MusicItem> items = parseJson(json);
            return  items;
        }

        @Override
        protected void onPostExecute(List<MusicItem> value) {
            super.onPostExecute(value);

            progressDialog.dismiss();
            //textView.setText(value);
        }
    }

}
