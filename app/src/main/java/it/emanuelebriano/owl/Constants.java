package it.emanuelebriano.owl;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public final class Constants {

    // turn on to write Web Log, off for faster app
    private static boolean WEBLOG_GLOBAL_ON = false;

    public static OwlBroadcastReceiver myOwlBroadcastReceiver = null;
    public static long bl_now;
    public static long bl_last;
    public static int bl_span;
    public static int bl_SPAN_SECONDS_RESTART = 11 * 60;

    public static boolean KILL_DUPLICATE = false;

    static final int min = 0;
    static final int max = 1000000;
    public static int randomID = 0;
    public static String VERSION = "0";

    static boolean active = false;

    public static boolean IsFirstLaunch()
    {
        if (randomID == 0)
            return true;
        else
            return false;
    }

    public static void Initialise(String version)
    {
        WebLog(10, "Constants.Initialise");
        Log.e(AppTAG, "Constants.Initialise");

        randomID = new Random().nextInt((max - min) + 1) + min;

        VERSION = version;
    }

    static public void SetActive(boolean a)
    {
        active = a;
    }

    public static Context owlContext;

    public static final String AppTAG = "it.emanuelebriano.OWL";


    private static final ArrayList<String> logs = new ArrayList<String>();
    public static String lastAppLog = "";

    public static int WEB_DEBUG_LEVEL = 0;
    public static int APP_DEBUG_LEVEL = 2;


    // Defines a custom Intent action
    public static final String BROADCAST_ACTION =
            "com.example.android.threadsample.BROADCAST";
    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS =
            "com.example.android.threadsample.STATUS";


    public static ArrayList<String> Get_AppLog()
    {
        try {
            if (logs.size() > 1)
            {
                for (int i = logs.size() - 1; i >= 1; i--) {
                    if (logs.get(i) == logs.get(i - 1)) {
                        logs.remove(i);
                    }
                }
            }
        }
        catch(Exception e)
        {
            Log.e(Constants.AppTAG, "Get_AppLog: " + e.getMessage());
        }

        return logs;
    }

    public static void Add_AppLog(String message)
    {
        logs.add(message);
    }


    public static void Clear_Logs()
    {
        logs.clear();
    }

    // LEVEL
    // 10 for exception
    // 2 for normal important message
    // 0 for debug only
    private static void WebLog(int level, String message)
    {
        if (WEBLOG_GLOBAL_ON) {
            Log.i(Constants.AppTAG, "Weblog() called");

            message = "(" + level + ") " + message;

            if (level >= WEB_DEBUG_LEVEL) {
                Weblog Task = new Weblog(message, "", new JSONObject());
                try {
                    JSONObject j = Task.execute().get();
                } catch (Exception e) {
                    Log.e(Constants.AppTAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static void AppLog(int level, String message, Context ctx)
    {
        boolean use_New = true;

        if (use_New)
        {
            AppLogDirect(level, message);
        }
        else {
            Log.i(AppTAG, "AppLog called()");

            // Sometimes, also traces it to the app
            if (level >= APP_DEBUG_LEVEL) {
                Intent intent = new Intent("AddLog");
                intent.putExtra("message", message);
                LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
            }

            if (WEB_DEBUG_LEVEL == 0) {
                ToastLog(message, ctx);

                logToFile(level, message, String.valueOf(level));
            }
        }
    }

    public static void AppLogDirect(int level, String message)
    {
        Log.i(AppTAG, "AppLogDirect called()");

        //Writes 1 or 2 files - more levels of detail
        if (level >= 20)
        {
            logToFile(level, message, "exceptions");  // only important logs
        }
        else if (level >= 10)
        {
            logToFile(level, message, "10");  // only important logs
        }

        // in any case
        logToFile(level, message, "0"); // all logs
    }

    /// ***
    /// Logs at screen using Toast
    /// ***
    public static void ToastLog(String message, Context ctx)
    {
        Log.i(AppTAG, "ToastLog called()");

        if (WEB_DEBUG_LEVEL == 0)
        {
            Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
        }
    }


    /// ***
    /// Logs to txt file in phone
    /// ***
    private static void logToFile(int level, String data, String level_name) {

        String owl_Name = "OWL_logs";

        try {
            Log.d(Constants.AppTAG, "writeToFile()");

            File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + owl_Name);
            if(!directory.exists())
            {
                directory.mkdir();
                Log.d(Constants.AppTAG,"Created " + directory.getAbsolutePath());
                if(!directory.exists())
                {
                    Log.e(Constants.AppTAG,"WARNING! Directory does not exists !!!!");
                }
            }
            else
            {
                Log.d(Constants.AppTAG,directory.getAbsolutePath() + " exists");
            }
            String currentDateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String currentTimeString = new SimpleDateFormat("HH:mm").format(new Date());
            String filename = currentDateString + "_" + level_name + ".txt";
            File newFile = new File(directory, filename);
            if(!newFile.exists()){
                Log.e(Constants.AppTAG, "new file does not exist and is being re-created");
                try {
                    Log.d(Constants.AppTAG,"Creating file " + filename);
                    newFile.createNewFile();
                    Log.d(Constants.AppTAG,"Created file " + filename);
                } catch (IOException e) {
                    Log.e(Constants.AppTAG,e.getMessage());
                }
            }
            else
            {
                Log.i(Constants.AppTAG, "new file exists");

            }

            try  {
                FileOutputStream fOut = new  FileOutputStream( newFile, true ); // NECESSARY CONSTRUCTOR TO APPEND
                OutputStreamWriter outputWriter=new OutputStreamWriter(fOut);
                outputWriter.append(currentTimeString + ": (" + String.valueOf(level) + ") " + data);
                outputWriter.append("\n\r");
                outputWriter.close();

                //display file saved message
                Log.d(Constants.AppTAG,"File saved successfully!");

            }catch (Exception e){
                Log.e(Constants.AppTAG, e.getMessage());
            }
        }
        catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /// ***
    /// Logs to txt file in phone - Worker file
    /// ***
    public static void logToFile_Worker(int level, String data) {
        try {
            //Log.d(Constants.AppTAG, "writeToFile()");

            File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "OWL_logs");
            if(!directory.exists())
            {
                directory.mkdir();
//                Log.d(Constants.AppTAG,"Created " + directory.getAbsolutePath());
                if(!directory.exists())
                {
                    Log.e(Constants.AppTAG,"WARNING! Directory does not exists !!!! Creation problems");
                }
            }
            else
            {
                //Log.d(Constants.AppTAG,directory.getAbsolutePath() + " exists");
            }
            String currentDateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String currentTimeString = new SimpleDateFormat("HH:mm").format(new Date());
            String filename = currentDateString + "_worker.txt";
            File newFile = new File(directory, filename);
            if(!newFile.exists()){
                //Log.d(Constants.AppTAG, "new file does not exist and is being re-created");
                try {
                    //Log.d(Constants.AppTAG,"Creating file " + filename);
                    newFile.createNewFile();
                    //Log.d(Constants.AppTAG,"Created file " + filename);
                } catch (IOException e) {
                    Log.e(Constants.AppTAG,e.getMessage());
                }
            }
            else
            {
                //Log.i(Constants.AppTAG, "new file exists");
            }

            try  {
                FileOutputStream fOut = new  FileOutputStream( newFile, true ); // NECESSARY CONSTRUCTOR TO APPEND
                OutputStreamWriter outputWriter=new OutputStreamWriter(fOut);
                outputWriter.append(currentTimeString + ": (" + String.valueOf(level) + ") " + data);
                outputWriter.append("\n\r");
                outputWriter.close();

                //Log.d(Constants.AppTAG,"File saved successfully!");

            }catch (Exception e){
                Log.e(Constants.AppTAG, e.getMessage());
            }
        }
        catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
