package it.emanuelebriano.owl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.in;

public class OwlBroadcastReceiver extends BroadcastReceiver {

    Map<String, Integer> map_Slopes;

    public long last_received = 0;

    //private static final String TAG = "OwlBroadcastReceiver";
    @Override
    public void onReceive(Context ctx, Intent intent)
    {
        // NEW in v179
        try {
            // *** LOGS the call *** ///
            Constants.AppLogDirect(0, "OwlBroadcastReceiver.onReceive", ctx);

            long now = currentTimeMillis();
            int passed = (int) ((now - last_received) / 1000);

            // *** LOGS the call *** ///
            Constants.AppLogDirect(0, "   " + String.valueOf(passed) + " seconds passed from last notification", ctx);

            last_received = now;

            String action = intent.getAction();
            Constants.AppLogDirect(0, "   Action is " + action, ctx);
            // *** //

            if (action == "com.eveningoutpost.dexdrip.BgEstimate") {
                // Segnala che la lettura è avvenuta
                AlarmManager.tLast_MiaoMiao_Received = System.currentTimeMillis();

                // Builds slope map
                map_Slopes = new HashMap<String, Integer>();
                map_Slopes.put("Flat", 0);
                map_Slopes.put("FortyFiveDown", -1);
                map_Slopes.put("SingleDown", -2);
                map_Slopes.put("DoubleDown", -3);
                map_Slopes.put("FortyFiveUp", 1);
                map_Slopes.put("SingleUp", 2);
                map_Slopes.put("DoubleUp", 3);
                map_Slopes.put("NONE", -1);
                map_Slopes.put("NOT COMPUTABLE", -1);

                StringBuilder sb = new StringBuilder();
                sb.append("Action: " + intent.getAction() + "\n");
                sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
                String log = "   onReceive: " + sb.toString();

                Log.d(Constants.AppTAG, log);
                Constants.AppLogDirect(0, log, ctx);

                try {
                    log = "";
                    String UriExtras = intent.toUri(Intent.URI_INTENT_SCHEME).toString();
                    String[] s_Extras = UriExtras.split(";", -1);
                    Map<String, String> map = new HashMap<String, String>();

                    for (String s_Extra : s_Extras) {
                        // Log

                        String[] s_Key_Value = s_Extra.split("=", -1);
                        if (s_Key_Value.length > 1) {
                            String s_Key = s_Key_Value[0];
                            String s_Value = s_Key_Value[1];
                            log += " - " + s_Key + ": " + s_Value + " - ";

                            map.put(s_Key, s_Value);
                        }
                    }
                    //Constants.WebLog(log);

                    String s_Time = map.get("l.com.eveningoutpost.dexdrip.Extras.Time");
                    String s_BgSlopeName = map.get("S.com.eveningoutpost.dexdrip.Extras.BgSlopeName");
                    int slope = 0;
                    String s_BgEstimate = map.get("d.com.eveningoutpost.dexdrip.Extras.BgEstimate");
                    String s_Raw = map.get("d.com.eveningoutpost.dexdrip.Extras.Raw");

                    String s_BgSlope = map.get("S.com.eveningoutpost.dexdrip.Extras.BgSlope");

                    // Monitor new slopes
                    if (map_Slopes.containsKey(s_BgSlopeName)) {
                        slope = map_Slopes.get(s_BgSlopeName);
                    } else {
                        //sb_Slopes.append(s_BgSlopeName + " - ");
                    }

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    String time = formatter.format(new Date(Long.parseLong(s_Time)));
                    Float f = Float.parseFloat(s_BgEstimate);
                    int estimate = f.intValue();

                    String log_Map = "   Received estimate " + String.valueOf(estimate)
                            + " and slope " + String.valueOf(slope) + " at time " + s_Time;  // NEW. Added time to log

                    //Constants.WebLog(2,log_Map);
                    Constants.AppLogDirect(10, log_Map, ctx);

                    //Constants.WebLog(sb_Slopes.toString());

                    // Now, let's evaluate wether to cast an Alarm
                    AlarmManager.EvaluateSensorAlarm(ctx, time, estimate, slope);

                    Constants.AppLogDirect(0, "   Tries to start background call JSON (*)", ctx);

                    try {

                        // TODO. Test async 25/07/2019
                        BackgroundJson Task = new BackgroundJson(String.valueOf(estimate), String.valueOf(slope), s_Time);
                        Task.execute();

                        //BackgroundJson Task= new BackgroundJson(String.valueOf(estimate), String.valueOf(slope),  s_Time, new JSONObject());
                        //JSONObject j = Task.execute().get();

                        // NEW: Interpretare la risposta
                        //AlarmManager.Check_Response(context, j);

                        //Constants.AppLogDirect(0,"      CheckResponse ok");

                    } catch (Exception e) {
                        Log.e(Constants.AppTAG, e.getMessage());
                        e.printStackTrace();

                        Constants.AppLogDirect(20, "      OwlBroadcastReceiver Exception 1: " + e.getMessage(), ctx);
                    }
                } catch (Exception e) {
                    String m = "      OwlBroadcastReceiver Exception 2: " + e.getMessage();
                    Log.e(Constants.AppTAG, m);
                    Constants.AppLogDirect(20, m, ctx);
                }
            }
        }
        catch (Exception e) {
            String m = "      OwlBroadcastReceiver Exception global: " + e.getMessage();
            Log.e(Constants.AppTAG, m);
            Constants.AppLogDirect(20, m, ctx);
        }
    }
}