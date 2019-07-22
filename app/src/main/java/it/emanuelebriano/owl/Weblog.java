package it.emanuelebriano.owl;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

public class Weblog extends AsyncTask<String, String, JSONObject> {

    String msg;

    public Weblog(String message, String b, JSONObject j)
    {
        msg = message;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        // Constants.AppLogDirect( 10, msg );

        String resp;

        try {
            String parsedString = "";

            String path = "http://www.emanuelebriano.it/json_server.php";

            OutputStream os = null;
            InputStream is = null;
            HttpURLConnection conn = null;

            //Log.i(Constants.AppTAG, "   preparing input of the POST");

            //constants
            URL url = new URL(path);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("APIKey", "Mad");
            postDataParams.put("function", "weblog");
            String message = "[" + Constants.randomID + "]"  + msg;
            postDataParams.put("message", message);

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            //Log.i(Constants.AppTAG, "   preparing to write " + getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();

            //Log.i(Constants.AppTAG, "   response code: " + String.valueOf(responseCode));

            if (responseCode == HttpsURLConnection.HTTP_OK)
            {

            } else {
                resp = new String("false : " + responseCode);
            }

            JSONObject jObj = null;

            // return JSON String
            return jObj;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("" +
                        "&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }




}