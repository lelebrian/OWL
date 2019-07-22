package it.emanuelebriano.owl;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static android.app.Notification.VISIBILITY_PUBLIC;

// https://stackoverflow.com/questions/50567164/custom-notification-sound-not-working-in-oreo

public final class AlarmManager {

    public static int THRESHOLD_VALUE_1 = 160;
    public static int THRESHOLD_DELTA_1 = -15;
    public static int THRESHOLD_VALUE_2 = 200;
    public static int THRESHOLD_DELTA_2 = -20;
    //public static int THRESHOLD_VALUE_3 = 0;
    public static int THRESHOLD_DELTA_3 = -30;

    public static int THRESHOLD_DELTA_UP_FOR_BOLUS = 20;
    public static int THRESHOLD_DELTA_UP_ACTIVE_BOLUS = -25;


    public static int CHECKRESPONSE_null_times = 0;
    public static int CHECKRESPONSE_null_THRESHOLD = 2;

    public static boolean FIRST_START = true;

    // Missing alarms
    public static final boolean MIAOMIAO_MISSING_ALARM = false;
    public static long tLast_MiaoMiao_Received = System.currentTimeMillis();  // Initialization at time = now
    public static double MIAOMIAO_MISSING_SECONDS_THRESHOLD = 10*60 + 10;  // 10 secondi di soglia
    public static double elapsed_MiaoMiao_seconds = -1;

    // Read forecast
    public static String update_message = "A new version is available. Press to update";
    public static int period = 1 * 60000;  // used by Service
    private static int periodsToForecast = 1;
    private static int periods_Elapsed = periodsToForecast;  // Do a first forecast

    // Alarm levels
    public static int ALARM_ZERO = 0;
    public static int ALARM_LOW = 10;
    public static int ALARM_MEDIUM = 20;
    public static int ALARM_HIGH = 30;
    public static int ALARM_MAX = 50;
    public static boolean send_also_Low_alarms = false;

    public static long sensor_Last_Time = 0;
    public static int sensor_Last_Slope = 0;
    public static int sensor_Last_Estimate = 0;
    public static int sensor_Trend = 0;
    public static String sensor_Last_Estimate_Memory_time_check = "";
    public static int sensor_Last_Estimate_Memory_1 = 0;
    public static int sensor_Last_Estimate_Memory_2 = 0;
    public static int sensor_Last_Estimate_Memory_3 = 0;
    public static int sensor_Last_Estimate_Memory_4 = 0;
    public static int sensor_Last_Estimate_Memory_5 = 0;
    public static int sensor_Last_Estimate_Memory_6 = 0;
    public static int sensor_Last_Estimate_Memory_7 = 0;
    public static int sensor_Last_Estimate_Memory_8 = 0;

    public static int last_Alarm_Level = 0;
    public static long last_Alarm_Time = 0;

    public static int threshold_low = 90;
    public static int slope_factor_hour = 60;
    public static int slope_minutes_Step1 = 30;
    public static int slope_minutes_Step2 = 60;


    // VARIABLES FOR STATIC CHANNELS
    static NotificationChannel channel_Low = null;
    static NotificationChannel channel_Medium = null;
    static NotificationChannel channel_High = null;

    static int notificationId_Low = 10;
    static int notificationId_Medium = 20;
    static int notificationId_High = 30;

    static String CHANNEL_ID_Low = "OwlLow";
    static String CHANNEL_ID_Medium = "OwlMedium";
    static String CHANNEL_ID_High = "OwlHigh";

    static Uri alarmSound_Low = null;
    static Uri alarmSound_Medium = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    static Uri alarmSound_High = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

    static long[] pattern_Low = {500};  // 1 vibrazione
    static long[] pattern_Medium = {500, 500, 500};  // 2 vibrazioni
    static long[] pattern_High = {500, 500, 500, 500, 500, 500, 500, 500, 500, 500,
            500, 500, 500, 500, 500, 500, 500, 500, 500, 500,
            500, 500, 500, 500, 500, 500, 500, 500, 500};  // 15 suoni


    private static void createNotificationChannels_if_Null(Context ctx) {

        if (channel_High == null)
            createNotificationChannel_High(ctx);
        if (channel_Medium == null)
            createNotificationChannel_Medium(ctx);
        if (channel_Low == null)
            createNotificationChannel_Low(ctx);
    }

    private static void createNotificationChannel_Low(Context ctx) {
        try {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "OwlMediumChannel";
                String description = "OWL normal notifications";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                channel_Low = new NotificationChannel(CHANNEL_ID_Low, name, importance);
                channel_Low.setDescription(description);
                channel_Low.enableLights(true);
                channel_Low.setLightColor(Color.BLUE);
                channel_Low.setSound(alarmSound_Low, null);
                channel_Low.setVibrationPattern(pattern_Low);
                channel_Low.enableVibration(true);

                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel_Low);
            }
        }
        catch(Exception e)
        {
            Constants.AppLogDirect(20,"AlarmManager exception: " + e.getMessage());
        }
    }


    private static void createNotificationChannel_Medium(Context ctx) {
        try {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "OwlMediumChannel";
                String description = "OWL normal notifications";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                channel_Medium = new NotificationChannel(CHANNEL_ID_Medium, name, importance);
                channel_Medium.setDescription(description);
                channel_Medium.enableLights(true);
                channel_Medium.setLightColor(Color.BLUE);
                channel_Medium.setSound(alarmSound_Medium, null);
                channel_Medium.setVibrationPattern(pattern_Medium);
                channel_Medium.enableVibration(true);

                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel_Medium);
            }
        }
        catch(Exception e)
        {
            Constants.AppLogDirect(20,"AlarmManager exception: " + e.getMessage());
        }
    }

    private static void createNotificationChannel_High(Context ctx) {
        try {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                CharSequence name = "OWLHighChannel";
                String description =  "OWL High level notifications";
                int importance = NotificationManager.IMPORTANCE_HIGH;

                channel_High = new NotificationChannel(CHANNEL_ID_High, name, importance);
                channel_High.setDescription(description);
                channel_High.enableLights(true);
                channel_High.setLightColor(Color.BLUE);
                channel_High.setSound(alarmSound_High, null);
                channel_High.setVibrationPattern(pattern_High);
                channel_High.enableVibration(true);
                channel_High.setShowBadge(true);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel_High);
            }
        }
        catch(Exception e)
        {
            Constants.AppLogDirect(20,"AlarmManager exception: " + e.getMessage());
        }
    }


    public static void CheckMissingAlarm(Context ctx, double elapsed_MiaoMiao_seconds) {
        if (elapsed_MiaoMiao_seconds > MIAOMIAO_MISSING_SECONDS_THRESHOLD) {
            if (MIAOMIAO_MISSING_ALARM == true)
                CreateNotification(ctx, "Check MiaoMiao signal", "Missing from " +String.valueOf((int)elapsed_MiaoMiao_seconds) + " seconds", ALARM_HIGH);
        }
    }

    public static void EvaluateSensorAlarm(Context ctx, String time, int estimate, int slope)
    {
        //Constants.WebLog(0, "EvaluateSensorAlarm( " + String.valueOf(estimate) + " , " + String.valueOf(slope) + " )");
        // NEW 16072019
        Constants.AppLogDirect(0, "EvaluateSensorAlarm( " + String.valueOf(estimate) + " , " + String.valueOf(slope) + " )");

        // NEW: test
        ReadPreferences(ctx);

        int alarm_level = ALARM_ZERO;

        Constants.AppLogDirect(0, "   Alarm set to zero");

        if (estimate < sensor_Last_Estimate)
        {
            Constants.AppLogDirect(0, "   Estimate " + String.valueOf(estimate) + " is lower then sensor_Last_Estimate " + String.valueOf(sensor_Last_Estimate));
            sensor_Trend = -1;

            // NEW: Corrects slope
            if ((slope == 0) && (sensor_Trend == -1))
            {
                slope = -1;

                Constants.AppLogDirect(0, "   NEW: Slope corrected to -1 since the value is decreasing");
            }
        }
        else {
            Constants.AppLogDirect(0, "   Estimate " + String.valueOf(estimate) + " is NOT lower then sensor_Last_Estimate " + String.valueOf(sensor_Last_Estimate));
            sensor_Trend = +1;
        }


        if (sensor_Last_Estimate_Memory_time_check == time)
        {
            // Double activation
        }
        else {
            // Save last values (add 25/06/2019)
            sensor_Last_Estimate_Memory_8 = sensor_Last_Estimate_Memory_7;
            sensor_Last_Estimate_Memory_7 = sensor_Last_Estimate_Memory_6;
            sensor_Last_Estimate_Memory_6 = sensor_Last_Estimate_Memory_5;   // 30 minutes
            sensor_Last_Estimate_Memory_5 = sensor_Last_Estimate_Memory_4;   // 25 minutes
            sensor_Last_Estimate_Memory_4 = sensor_Last_Estimate_Memory_3;   // 20 minutes
            sensor_Last_Estimate_Memory_3 = sensor_Last_Estimate_Memory_2;   // 15 minutes
            sensor_Last_Estimate_Memory_2 = sensor_Last_Estimate_Memory_1;   // 10 minutes
            sensor_Last_Estimate_Memory_1 = sensor_Last_Estimate;   // 5 minutes
        }
        sensor_Last_Estimate = estimate;
        sensor_Last_Slope = slope;

        // Checks if the value is too low
        if (estimate < threshold_low)
        {
            Constants.AppLogDirect(0, "   Setting alarm level to 50. Estimate " + String.valueOf(estimate) + " is < than " + String.valueOf(threshold_low));
            //cast_alarm = true;
            alarm_level = Math.max( ALARM_MAX , alarm_level );
        }
        else
        {

        }

        // In X minutes
        float future_value_Step1 = estimate + (slope_factor_hour * ((float)slope_minutes_Step1/(float)60) * slope);
        float future_value_Step2 = estimate + (slope_factor_hour * ((float)slope_minutes_Step2/(float)60) * slope);

        String log = "   Future value (step1) is " + String.valueOf(future_value_Step1);
        Constants.AppLogDirect(0, log);

        log = "   Future value (step2) is " + String.valueOf(future_value_Step2);
        Constants.AppLogDirect(0, log);

        // Checks if the value is too low in (step1) minutes
        if (future_value_Step1 < threshold_low)
        {
            alarm_level = Math.max( ALARM_HIGH, alarm_level );

            Constants.AppLogDirect(0, "   Setting alarm level to 30. Future value 1 (" + String.valueOf(future_value_Step1) + ")is < than " + String.valueOf(threshold_low));
        }
        else {
            // Checks if the value is too low in (step1) minutes
            if (future_value_Step2 < threshold_low) {
                alarm_level = Math.max(ALARM_MEDIUM, alarm_level);
                Constants.AppLogDirect(0, "   Setting alarm level to 20 (if not higher). Future value 2 is < than " + String.valueOf(threshold_low));
            }
        }

        sensor_Last_Time = System.currentTimeMillis();

        Constants.AppLogDirect(0, "   EvaluateSensorAlarm with level " + String.valueOf(alarm_level));

        if (alarm_level >= ALARM_HIGH) {
            try {
                CreateNotification(ctx, Get_Last_Summary_Value(ctx),"Verificare valori", alarm_level);
            } catch (Exception e) {
                Constants.AppLogDirect(10, "   AlarmManager exception: " + e.getMessage());
            }
        }
        else {
            try {
                // BUGFIX 19/07/2019
                CreateNotification(ctx, Get_Last_Summary_Value(ctx), "xDrip+ " + String.valueOf(estimate) + " slope " + String.valueOf(slope), alarm_level);
            }
            catch(Exception e)
            {
                Constants.AppLogDirect(20,"   AlarmManager exception: " + e.getMessage());
            }
        }
    }

    public static void NofifyStart(Context ctx)
    {

        if (FIRST_START == true)
        {
            String title = "Test: ";
            //title += ctx.getString(R.string.slopem3) + " - ";
        //    title += "\uD83E\uDC52" + " - ";
        //    title += ctx.getString(R.string.slopem1) + " - ";
        //    title += "\u21ca" + " - ";
        //    title += ctx.getString(R.string.test) + " - ";

            //CreateNotification(ctx, title, "Started", ALARM_ZERO);

            Constants.AppLog(2, title, ctx);

            FIRST_START = false;
        }
    }

    public static String Get_Last_Summary_Value(Context ctx)
    {
        if (sensor_Last_Estimate == 0)
            return "Nessuna lettura.";

        try {

            //String summary_Value = String.valueOf(sensor_Last_Estimate) + " slope " + String.valueOf(sensor_Last_Slope);
            String summary_Value = String.valueOf(sensor_Last_Estimate) + " ";
            if (sensor_Last_Slope == 0)
                summary_Value += ctx.getString(R.string.slope0);
            if (sensor_Last_Slope == -1)
                summary_Value += ctx.getString(R.string.slopem1);
            if (sensor_Last_Slope == -2)
                summary_Value += ctx.getString(R.string.slopem2);
            if (sensor_Last_Slope <= -3)
                summary_Value += ctx.getString(R.string.slopem3);
            if (sensor_Last_Slope == 1)
                summary_Value += ctx.getString(R.string.slope1);
            if (sensor_Last_Slope == 2)
                summary_Value += ctx.getString(R.string.slope2);
            if (sensor_Last_Slope >= 3)
                summary_Value += ctx.getString(R.string.slope3);

            return summary_Value;
        }
        catch (Exception e)
        {
            return "Get_Last_Summary_Value Exception: " + e.getMessage();
        }

    }

    public static void Build_RF_Notification(Context ctx, String risktime, int alert_level, String notes) {
        try {
            String log = "Build_RF_Notification with risktime " + risktime + " and alert_level " + String.valueOf(alert_level);

            Log.i(Constants.AppTAG, log);
            Constants.AppLogDirect(0, log);

            String msg = "Nessuna lettura.";

            if (sensor_Last_Estimate != 0) {
                msg = "xDrip+ " + String.valueOf(sensor_Last_Estimate) + " slope " + String.valueOf(sensor_Last_Slope) + ".";
            }

            if (risktime != "") {
                String[] separated = risktime.split(" ");
                String risk_hour = separated[1];
                String[] hhmmss = risk_hour.split(":");
                String risk_time_HHmm = hhmmss[0] + ":" + hhmmss[1];

                if (notes == "DELTA") {
                    msg += " Rischio FALLING - " + risk_time_HHmm + " " + notes;
                }
                else if(notes == "RISING") {
                    msg += " Rischio RISE - " + risk_time_HHmm + " " + notes;
                }
                else {
                    msg += " Rischio IPO - " + risk_time_HHmm + " " + notes;
                }
            } else {
                msg += " ok";
            }

            CreateNotification(ctx, Get_Last_Summary_Value(ctx), msg, alert_level);
        }
        catch (Exception e)
        {
            CreateNotification(ctx, "Exception", e.getMessage(), alert_level);

            Constants.AppLogDirect(20, "AlarmManager.BuildNotification.Exception: " + e.getMessage());
        }
    }


    public static void CreateNotification(Context ctx, String title, String message, int alert_level) {


        // test. Problems of context ? we need to pass by an async task ?
        Constants.AppLogDirect(10, "AlarmManager.CreateNotification(" + message + ", " + String.valueOf(alert_level) + ")");

        /*
        try {
            Constants.AppLogDirect(10, "AlarmManager.CreateNotification(" + message + ", " + String.valueOf(alert_level) + ")");
        }
        catch (Exception e)
        {

        }
        */

        String d_msg = "";

        // Checks if enough time passed from last notification
        // If the notification was in the last minute AND of an equal or higher level, kills this one
        long l_Now = System.currentTimeMillis();
        long l_OneMinute = 60 * 1000;


        try {


            // NEW 16/07/2019
            ReadPreferences(Constants.owlContext);

            if (l_Now - last_Alarm_Time < l_OneMinute) {
                int i_SecondsPassed = (int) ((l_Now - last_Alarm_Time) / 1000);
                Constants.AppLogDirect(0, "   " + String.valueOf(i_SecondsPassed) + " passed from last notification " +
                        "- less than Threshold");

                if (alert_level < last_Alarm_Level) {
                    Constants.AppLogDirect(10, "   Killing duplicate notification - new level is "
                            + String.valueOf(alert_level) + " while old level is " + String.valueOf(last_Alarm_Level));
                    return;
                } else {
                    if (alert_level == last_Alarm_Level) {
                        if (Constants.KILL_DUPLICATE) {
                            Constants.AppLogDirect(10, "   Killing duplicate same level (KILL_DUPLICATE ON)");

                            // Tracing this for test
                            message = "(D) " + message;

                            return;  // NEW 16/07/2019
                        } else {
                            Constants.AppLogDirect(10, "   Not killing duplicate because is the same alert level (KILL_DUPLICATE OFF)");

                            // Tracing this for test
                            message = "(D) " + message;
                        }
                    } else {
                        Constants.AppLogDirect(10, "   Not killing duplicate because alarm level superior." +
                                "New level is " + String.valueOf(alert_level));

                        // Tracing this for test
                        message = "(D) " + message;
                    }
                }
            }
        }
        catch (Exception e)
        {
            Constants.AppLogDirect(20, "CreateNotification Exception step 0 " + e.getMessage());

            d_msg = " - (D) " + e.getMessage();
        }

        try {

            last_Alarm_Level = alert_level;
            last_Alarm_Time = l_Now;

            Calendar rightNow = Calendar.getInstance(TimeZone.getDefault());
            int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
            int currentMinute = rightNow.get(Calendar.MINUTE);
            String s_Hour = String.valueOf(currentHour);
            if (currentHour < 10)
                s_Hour = "0" + s_Hour;
            String s_Minute = String.valueOf(currentMinute);
            if (currentMinute < 10)
                s_Minute = "0" + s_Minute;
            message = "(" + s_Hour + ":" + s_Minute + ") " + message;


            AlarmManager.createNotificationChannels_if_Null(ctx);

            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(ctx, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);


            // Important LOG
            Constants.AppLogDirect(10,"   sending notification with level " + alert_level);

            // Creo la notifica minima
            NotificationCompat.Builder mBuilder;

            // HELPS DEBUG
            message = message + d_msg;

            if (alert_level >= ALARM_HIGH) {

                mBuilder = new NotificationCompat.Builder(ctx, CHANNEL_ID_High)
                        .setSmallIcon(R.drawable.owl)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setVisibility(VISIBILITY_PUBLIC)
                        // Set the intent that will fire when the user taps the notification
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(false);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(notificationId_High, mBuilder.build());
            }
            else if (alert_level >= ALARM_MEDIUM) {

                mBuilder = new NotificationCompat.Builder(ctx, CHANNEL_ID_Medium)
                        .setSmallIcon(R.drawable.owl)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setVisibility(VISIBILITY_PUBLIC)
                        // Set the intent that will fire when the user taps the notification
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(false);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(notificationId_Medium, mBuilder.build());

            }
            else if ((alert_level >= ALARM_LOW) & (send_also_Low_alarms)) {
                    // Creo la notifica minima
                    mBuilder = new NotificationCompat.Builder(ctx, CHANNEL_ID_Low)
                            .setSmallIcon(R.drawable.owl)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setVisibility(VISIBILITY_PUBLIC)
                            // Set the intent that will fire when the user taps the notification
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(false);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
                    // notificationId is a unique int for each notification that you must define
                    notificationManager.notify(notificationId_Low, mBuilder.build());
                }
                else
                {
                    // JUST DEBUG
                    if (message != "") {
                        // Creo la notifica minima
                        mBuilder = new NotificationCompat.Builder(ctx, CHANNEL_ID_Low)
                                .setSmallIcon(R.drawable.owl)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setVisibility(VISIBILITY_PUBLIC)
                                // Set the intent that will fire when the user taps the notification
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(false);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
                        // notificationId is a unique int for each notification that you must define
                        notificationManager.notify(notificationId_Low, mBuilder.build());
                    }
                }
        }
        catch(Exception e)
        {
            Constants.AppLogDirect(20, "AlarmManager.CreateNotification.Exception: " + e.getMessage());
        }
    }


    public static void CreateUpdateNotification(Context ctx, String message, String url_new)    {
        Constants.AppLogDirect(0,"AlarmManager.CreateUpdateNotification(" + url_new + ")");

        AlarmManager.CreateNotification(ctx, "Update available", message, 20);
        /*

        AlarmManager.createNotificationChannels_if_Null(ctx);
        int alert_level = 10;

        //Uri uri = Uri.parse(url_new);

        Intent intent = new Intent(ctx, updater.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

        long[] pattern = {500,500,500,500,500,500,500,500,500};
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.owl)
                .setContentTitle("Aggiornamento")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(VISIBILITY_PUBLIC)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        if (alert_level >= 2)
        {
            mBuilder
                    .setVibrate(pattern)
                    .setSound(alarmSound);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);

        notificationManager.notify(notificationId_Normal, mBuilder.build());
        */
    }



    public static void ReadForecast(Context ctx) {

        // DEVELOPER DEBUG
        Constants.AppLog(0,"ReadForecast called", ctx);

        String msg = "RF()- last MiaoMiao = " + String.valueOf((int)elapsed_MiaoMiao_seconds) + " s";

        //Constants.WebLog(2,  msg);
        Constants.AppLogDirect(0, msg);

        Log.i(Constants.AppTAG, "ReadForecast() called");

        BackgroundJson Task= new BackgroundJson("", "", "",  new JSONObject());
        try {
            JSONObject j = Task.execute().get();

            Check_Response(ctx, j);
        } catch (Exception e) {
            Log.e(Constants.AppTAG, e.getMessage());
            e.printStackTrace();

            Constants.AppLogDirect(20,"ReadForecast Exception: " + e.getMessage());
        }
    }

    // Here it checks the response from JSON
    public static void Check_Response(Context ctx, JSONObject j)    {

        // DEVELOPER DEBUG
        Constants.AppLog(0,"Check_Response called", ctx);

        if (j == null) {
            CHECKRESPONSE_null_times++;
            Constants.AppLogDirect(10, "CheckResponse called with null jSon. CHECKRESPONSE_null_times " + String.valueOf(CHECKRESPONSE_null_times));

            if (CHECKRESPONSE_null_times <= CHECKRESPONSE_null_THRESHOLD)
                Constants.AppLogDirect(10, "   return;");
                return;
        }
        else {
            CHECKRESPONSE_null_times = 0;
            Constants.AppLogDirect(10, "CheckResponse called with Json NOT null - CHECKRESPONSE_null_times = 0");
        }

        int alert_level = ALARM_ZERO;
        int alert_type = 0;

        JSONArray ja;

        int iporisk = 0;
        String risktime = "";

        try {
            ja = j.getJSONArray("FORECAST");
        }
        catch (Exception e) {
            Log.e(Constants.AppTAG, e.getMessage());
            e.printStackTrace();

            String e_msg = "CheckResponse Exception part1: " + e.getMessage();

            Constants.AppLogDirect(20,e_msg);

            CreateNotification(ctx, "CheckResponse Exception: ", e.getMessage(), 30);
        }

        int last_previsione = 0;

        try {
            ja = j.getJSONArray("FORECAST");

            // DEBUG
            try {
                Constants.AppLog(0, "   Reading JSON FORECAST with length " + String.valueOf(ja.length()), ctx);
            }
            catch(Exception e)
            {}


            // SETTING THRESHOLD DELTA

                int THRESHOLD_DELTA = THRESHOLD_DELTA_1;
                if (sensor_Last_Estimate > THRESHOLD_VALUE_1)
                    THRESHOLD_DELTA = THRESHOLD_DELTA_2;
                if (sensor_Last_Estimate > THRESHOLD_VALUE_2)
                    THRESHOLD_DELTA = THRESHOLD_DELTA_3;

                // DEBUG
                try {
                    Constants.AppLog(0, "   lAST ESTIMATE IS " + String.valueOf(sensor_Last_Estimate) +
                            " therefore using THRESHOLD_DELTA = " + String.valueOf(THRESHOLD_DELTA), ctx);
                    //Constants.AppLog(0, "   Alert level delta is " + String.valueOf(alert_level_delta), ctx);
                }
                catch(Exception e)
                {}

            // ---

            float rate_bolo_max = 0; // in the future
            String risktime_now = "";

            for (int i = 0; i < ja.length(); i++)
            {

                JSONObject jo = ja.getJSONObject(i);

                // Ora si possono analizzare i vari intervalli futuri, ad ogni i 15 minuti
                String risktime_i = jo.getString("DATETIME");

                if (i==0)
                {
                    risktime_now = risktime_i;
                }

                // TODO: Lavorare sul valore LOWER o su IPORISK ?
                iporisk = Integer.parseInt(jo.getString("IPORISK"));
                int previsione = Integer.parseInt(jo.getString("PREVISIONE"));


                if (iporisk >= 1) {
                    // C'è un rischio di ipo, ma fra quanto ?
                    if (i < 2) {
                        // Entro la prossima mezz'ora
                        if (alert_level < ALARM_HIGH) {
                            alert_level = ALARM_HIGH;
                            risktime = risktime_i;
                            alert_type = 0;
                        }
                    } else if (i < 4) {
                        // Entro un'ora
                        if (alert_level < ALARM_MEDIUM) {
                            alert_level = ALARM_MEDIUM;
                            risktime = risktime_i;
                            alert_type = 0;
                        }
                    } else if (i < 8) {
                        // Entro due ore
                        if (alert_level < ALARM_LOW) {
                            alert_level = ALARM_LOW;
                            risktime = risktime_i;
                            alert_type = 0;
                        }
                    } else {
                        // Oltre le due ore
                        // alert_level = Integer.max(alert_level, ALARM_ZERO);
                    }

                    // break; // OCCHIO A QUESTO BREAK !!!
                    // TUTTO DA RIVEDERE CON ALLARME SOLO INCREMENTALE MA CICLO CHE NON SI FERMA

                } else {

                }


                // Checks for delta previsione ("falling")
                if (i == 0)
                {
                    // DEBUG
                    try {
                        Constants.AppLog(0, "      Setting previsione to " + String.valueOf(previsione), ctx);
                    }
                    catch(Exception e)
                    {}

                    last_previsione = previsione;
                }
                else
                {
                    int delta = previsione - last_previsione;

                    // DEBUG
                    try {
                        Constants.AppLog(0, "      Delta is " + String.valueOf(delta), ctx);
                    }
                    catch(Exception e)
                    {}


                    if (delta < THRESHOLD_DELTA)
                    {
                        if (i < 2) {
                            // Entro la mezz'ora !!!!!!
                            if (alert_level < ALARM_HIGH) {
                                alert_level = ALARM_HIGH;
                                risktime = risktime_i;
                                alert_type = 1;
                            }

                        } else if (i < 4) {
                            // Entro un'ora
                            if (alert_level < ALARM_MEDIUM) {
                                alert_level = ALARM_MEDIUM;
                                risktime = risktime_i;
                                alert_type = 1;
                            }
                        } else if (i < 8) {
                            // Entro due ore
                            if (alert_level < ALARM_LOW) {
                                alert_level = ALARM_LOW;
                                risktime = risktime_i;
                                alert_type = 1;
                            }
                        } else {
                            // Oltre le due ore
                            //alert_level_delta = Integer.max(alert_level_delta, ALARM_ZERO);
                        }
                    }

                    last_previsione = previsione;
                }


                float rate_bolo = Float.parseFloat(jo.getString("RATE_BOLO"));
                float rate_base = Float.parseFloat(jo.getString("RATE_BASE"));
                float rate_bolo_plus_base = rate_base + rate_bolo;

                if (rate_bolo_plus_base < rate_bolo_max)   // less is more
                {
                    rate_bolo_max = rate_bolo_plus_base;
                }
            }
            if (iporisk == 0) {
                // does nothing
            }

            //TODO: test this
            if (sensor_Last_Estimate_Memory_5 != 0) {

                int delta_for_rise_alarm = (sensor_Last_Estimate - sensor_Last_Estimate_Memory_5);

                Constants.AppLog(0,"Delta for rise is " + String.valueOf(delta_for_rise_alarm)
                        + "Setup: max bolo rate is " + String.valueOf(rate_bolo_max), ctx);

                if (delta_for_rise_alarm > THRESHOLD_DELTA_UP_FOR_BOLUS) {
                    // estimate rising
                    if (rate_bolo_max < THRESHOLD_DELTA_UP_ACTIVE_BOLUS)  // > perché è negativo, quindi alto è poco
                    {
                        // C'è insulina, attendiamo
                    }
                    else{
                        if (alert_level < ALARM_MEDIUM) {
                            alert_level = ALARM_MEDIUM;
                            risktime = risktime_now;
                            alert_type = 2;
                        }
                    }
                }
            }
            else
            {
                // SETUP phase
                Constants.AppLog(0,"Setup: max bolo rate is " + String.valueOf(rate_bolo_max), ctx);
            }



            // DEBUG
            try {
                Constants.AppLog(0, "   Alert level is " + String.valueOf(alert_level) + " with risktime = " + risktime +
                        " and alert_type = " + String.valueOf(alert_type), ctx);
                //Constants.AppLog(0, "   Alert level delta is " + String.valueOf(alert_level_delta), ctx);
            }
            catch(Exception e)
            {}

            String notes = "";
            if (alert_type == 1)  // Gives alert for delta only if higher severity then the one on iporisk
            {
                notes = "DELTA";
            }
            else if (alert_type == 2)
            {
                notes = "RISING";
            }
            Build_RF_Notification(ctx, risktime, alert_level, notes);

            periods_Elapsed = 0;
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(Constants.AppTAG, e.getMessage());
            e.printStackTrace();

            String e_msg = "CheckResponse Exception part2: " + e.getMessage();

            //Constants.WebLog(10,e_msg);
            Constants.AppLogDirect(20,e_msg);

            CreateNotification(ctx, "CheckResponse Exception", e.getMessage(), 30);
        }
    }


    public static void Notify_Service_Elapsed(Context ctx)    {

        // DEVELOPER DEBUG
        Constants.AppLog(0,"Notify_Service_Elapsed", ctx);

        // Calculates seconds since last MiaoMiao
        long tNow = System.currentTimeMillis();
        long tDelta = tNow - tLast_MiaoMiao_Received;
        double elapsedSeconds = tDelta / 1000.0;
        elapsed_MiaoMiao_seconds = elapsedSeconds;

        // Increases period elapsed counter
        periods_Elapsed++;

        // Checks if there are updates
        String url_new = check_and_update(ctx);
        if (url_new == "")
        {
           if (periods_Elapsed >= periodsToForecast) {
                    ReadForecast(ctx);
                    periods_Elapsed = 0;
                } else {

               Alive(ctx);
           }
        }
        else {
            CreateUpdateNotification(ctx, update_message, url_new);
        }

        // Update interval if preferences changed
        ReadPreferences(ctx);

        CheckMissingAlarm(ctx, elapsed_MiaoMiao_seconds);
    }

    public static String check_and_update(Context ctx)    {
        try {
            PackageInfo pInfo =  ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;
            int new_version = verCode + 1;

            // TODO: Check newer versions
            // TODO: Add update on/off switch in preferences
            String url_new = "http://www.emanuelebriano.it/owl_" + String.valueOf(new_version) + ".apk";

            Constants.AppLogDirect(0, "Checking for new update: " + url_new);

            if (exists(url_new))
            {
                Log.i(Constants.AppTAG,"New version available !");

                return url_new;
            }
            else
            {
                Log.i(Constants.AppTAG,"No new version available");

                return "";
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

            return e.getMessage();

        }
    }

    public static String Get_Version(Context ctx)    {
        try
        {
            PackageInfo pInfo =  ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return pInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            return "Exception when getting version name";
        }
    }

    public static boolean exists(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");

            Constants.AppLogDirect(0, "AlarmManager,exists() response code: " + con.getResponseCode());

            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            Constants.AppLogDirect(20, "AlarmManager.exists("+URLName+") Exception: " + e.getMessage());
            return false;
        }
    }



    public static void Alive(Context ctx)    {
        String msg = "(service alive) - last MiaoMiao = " + String.valueOf((int)elapsed_MiaoMiao_seconds) + " s - d(" + String.valueOf(Constants.WEB_DEBUG_LEVEL) + ")";

        Constants.AppLogDirect(0, msg);

        //Constants.WebLog(2,  msg);
    }

    public static void ReadPreferences(Context ctx)    {

        Constants.AppLogDirect(0, "ReadPreferences called");

        try {
            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(ctx);

            String owl_interval = sharedPref.getString("owl_interval", "5");
            periodsToForecast = Integer.parseInt(owl_interval);
            Log.i(Constants.AppTAG, "PREFERENCE: owl interval set to " + owl_interval);

            String wdl = sharedPref.getString("web_debug_level", "0");
            Constants.WEB_DEBUG_LEVEL = Integer.parseInt( wdl );

            String kd = sharedPref.getString("kill_duplicate", "false");
            Constants.KILL_DUPLICATE = Boolean.parseBoolean(kd);

            Constants.AppLogDirect(0, "KILL_DUPLICATE set to " + String.valueOf(Constants.KILL_DUPLICATE));

            Log.i(Constants.AppTAG, "PREFERENCE: Web debug level set to " + String.valueOf(Constants.WEB_DEBUG_LEVEL));
        }
        catch (Exception e)
        {
            Log.e(Constants.AppTAG, "Exception: " + e.getMessage());
        }
    }


}
