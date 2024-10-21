package it.emanuelebriano.owl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

public class BackgroundJson extends AsyncTask<String, String, JSONObject> {



    String bg_Estimate;
    String bg_Slope;
    String s_Time;

    //AlarmManager alarm_manager;

    int minutes_forward = 5;

    // TODO: Test. 25/07/2019. Removed JSONObject
    public BackgroundJson(String a, String b, String t)
    {
        bg_Estimate = a;
        bg_Slope = b;
        s_Time = t;

        //alarm_manager = am;  // NEW
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        Log.i(Constants.AppTAG, "Background Task called");
        //Constants.AppLogDirect(0,"   JSONObject.Background Task called");

        String resp = "";

        Log.i(Constants.AppTAG, "   Background JSON - check 001");

        JSONObject postDataParams = new JSONObject();
        String parsedString = "";

        String path = "http://www.emanuelebriano.it/json_server.php";
        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;

        Log.i(Constants.AppTAG, "   Background JSON - check 001");

        try {
            Log.i(Constants.AppTAG, "   preparing input of the POST");

            java.net.URL url = new URL(path);

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));

            int n = 4 * 3; // 3 hours

            if (bg_Estimate == "") {

                postDataParams.put("APIKey", "Mad");
                postDataParams.put("function", "getCompleteMonitor");
                postDataParams.put("anno", cal.get(Calendar.YEAR));
                postDataParams.put("mese", cal.get(Calendar.MONTH) + 1);
                postDataParams.put("giorno", cal.get(Calendar.DAY_OF_MONTH));
                postDataParams.put("ora", cal.get(Calendar.HOUR_OF_DAY));
                postDataParams.put("minuti", cal.get(Calendar.MINUTE));
                postDataParams.put("numberOfValues", n);
            } else {

                // NEW in 176: Trying to insert all values -  NEW 23/05/2022
                SimpleDateFormat formatter_DATE = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                // Gets the time in a long
                long time = Long.parseLong(s_Time);
                // Adds minutes forward in milliseconds
                time = time + (minutes_forward / 60 / 1000);
                // Converts into a date
                Date d = new Date(time);

                // Formats date for inserts
                SimpleDateFormat formatter_Y = new SimpleDateFormat("yyyy");
                String year = formatter_Y.format(d);
                SimpleDateFormat formatter_M = new SimpleDateFormat("MM");
                String month = formatter_M.format(d);
                SimpleDateFormat formatter_D = new SimpleDateFormat("dd");
                String day = formatter_D.format(d);
                SimpleDateFormat formatter_HH = new SimpleDateFormat("HH");
                String HH = formatter_HH.format(d);
                SimpleDateFormat formatter_MM = new SimpleDateFormat("mm");
                String mm = formatter_MM.format(d);
                /*int i_mm = Integer.parseInt(mm) + minutes_forward;
                if (i_mm > 60)
                {
                    i_mm = i_mm - 60;
                    mm = String.valueOf(i_mm);
                }*/ // ERROR: doesn't work, when minutes close to 60

                postDataParams.put("APIKey", "MadWrite");
                postDataParams.put("function", "insert_BGESTIMATE_BGSLOPE");
                postDataParams.put("anno", year);
                postDataParams.put("mese", month);
                postDataParams.put("giorno", day);
                postDataParams.put("ora", HH);
                postDataParams.put("minuti", mm);  // Forward 5 minutes

                /*postDataParams.put("mese", cal.get(Calendar.MONTH) + 1);
                postDataParams.put("giorno", cal.get(Calendar.DAY_OF_MONTH));
                postDataParams.put("ora", cal.get(Calendar.HOUR_OF_DAY));
                postDataParams.put("minuti", cal.get(Calendar.MINUTE) + minutes_forward);  // Forward 5 minutes
                */
                postDataParams.put("bgestimate", bg_Estimate);
                postDataParams.put("bgslope", bg_Slope);

                postDataParams.put("anno_get", cal.get(Calendar.YEAR));
                postDataParams.put("mese_get", cal.get(Calendar.MONTH) + 1);
                postDataParams.put("giorno_get", cal.get(Calendar.DAY_OF_MONTH));
                postDataParams.put("ora_get", cal.get(Calendar.HOUR_OF_DAY));
                int min_get = cal.get(Calendar.MINUTE)  + minutes_forward;
                if (min_get > 60) min_get = min_get -60;
                postDataParams.put("minuti_get", min_get);  // Forward 5 minutes
                postDataParams.put("numberOfValues", n);
            }
        }
        catch (Exception e) {
            e.printStackTrace();

            String msg = "BackgroundJson Exception 1: " + e.getMessage();
            Log.e(Constants.AppTAG, msg);
            //Constants.AppLogDirect(10,msg);
        }

        Log.i(Constants.AppTAG, "   Background JSON - check 002");

        //Constants.AppLogDirect(0,"      preparing to write this JSON Object " + postDataParams.toString());


        try {

            os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            Log.i(Constants.AppTAG, "   preparing to write " + getPostDataString(postDataParams));
            //Constants.AppLogDirect(0,"      preparing to write " + getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();

            Log.i(Constants.AppTAG, "   response code: " + String.valueOf(responseCode));

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                Log.i(Constants.AppTAG, "      response ok ");
                //Constants.AppLogDirect(0,"      response ok ");

                BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }

                resp = total.toString();
            } else {
                resp = new String("false : " + responseCode);
            }

            Log.i(Constants.AppTAG, "Resp:" + resp);
        }
        catch (Exception e) {
            e.printStackTrace();

            String msg = "!!!   BackgroundJson Exception 2: " + e.getMessage();
            Log.e(Constants.AppTAG, msg);
            //Constants.AppLogDirect(20 ,msg);
        }

        Log.i(Constants.AppTAG, "   Background JSON - check 003 - " + resp);
        //Constants.AppLogDirect(0, "      Background JSON - check 003 - " + resp); // New in 177

        try
        {
            JSONObject jObj = null;
            // try parse the string to a JSON object
            try {
                JSONArray ja = new JSONArray(resp);
                jObj = ja.getJSONObject(0);
            } catch (JSONException e) {
                Log.e(Constants.AppTAG, "Error parsing data " + e.toString());
                //Constants.AppLogDirect(20, "!!! Error parsing data " + e.toString());
            }

            Log.i(Constants.AppTAG, "      Parsing OK");
            //Constants.AppLogDirect(9, "      Parsing OK");

            // return JSON String
            return jObj;
        } catch (Exception e) {
            String msg = "!!!   BackgroundJson Exception 3: " + e.getMessage();
            Log.e(Constants.AppTAG, msg);
            //Constants.AppLogDirect(20,msg);
        }

        //Constants.AppLogDirect(0,"JSONObject.Background Task ended");

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
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }


    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);

        AlarmManager.Check_Response(jsonObject);
    }
}
