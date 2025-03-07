package it.emanuelebriano.owl;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import android.net.Uri;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Constants {

    // TODO. Delete this, may use RAM
    private static String text_logs_collection;

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

    public static int IPORISK_TO_ALARM = 2;


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
            AppLogDirect(level, message, ctx);
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

                logToFile(level, message, String.valueOf(level), ctx);
            }
        }
    }

    public static void AppLogDirect(int level, String message, Context ctx)
    {
        Log.i(AppTAG, "AppLogDirect called()");

        //Writes 1 or 2 files - more levels of detail
        if (level >= 20)
        {
            logToFile(20, message, "exceptions", ctx);  // only important logs
            logToFile(10, message, "10", ctx);  // only important logs
            logToFile(0, message, "0", ctx); // all logs
        }
        else if (level >= 10)
        {
            logToFile(10, message, "10", ctx);  // only important logs
            logToFile(0, message, "0", ctx); // all logs
        }
        else // in any case
        {
            logToFile(level, message, "0", ctx); // all logs
        }
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


    public static void write_Exception_Log_To_Mail(String log)
    {
        String currentTimeString = new SimpleDateFormat("HH:mm").format(new Date());
        text_logs_collection += "\r\n" + currentTimeString + ": " + log;
    }

    /* NEW 08/09/2022
    // TODO: TEST. Add sending of file attachments
    public static void send_log_by_mail(Context ctx) {

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        // set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"emanuele.briano@gmail.com"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Owl logs");
        String s_text = text_logs_collection;
        emailIntent.putExtra(Intent.EXTRA_TEXT, s_text);

        try {
            ctx.startActivity(Intent.createChooser(emailIntent, "Send email..."));

            // Empties the variable
            text_logs_collection = "";
        }
        catch (Exception e)
        {
            logToFile(30, e.getMessage(), "Exception", ctx);
        }
    }

     */

    /* NEW 21/10/2024
    public static void send_log_by_mail(Context ctx) {
        try {

            Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            emailIntent.setType("vnd.android.cursor.dir/email");
            String[] to = {"emanuele.briano@gmail.com"};
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Owl logs");
            String s_text = text_logs_collection;
            emailIntent.putExtra(Intent.EXTRA_TEXT, s_text);

            // Get today's date to filter log files
            String currentDateString = new SimpleDateFormat("yyyyMMdd").format(new Date());

            // Directory where logs are stored
            File directory = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "OWL_LOGS");

            if (directory.exists() && directory.isDirectory()) {
                ArrayList<Uri> uris = new ArrayList<>();

                // Loop through files in the directory
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().startsWith(currentDateString)) {
                            Uri fileUri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".provider", file);
                            uris.add(fileUri);
                        }
                    }
                }

                // Add the log files as attachments
                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            }

            ctx.startActivity(Intent.createChooser(emailIntent, "Send email..."));
            text_logs_collection = ""; // Clear log collection after sending
        } catch (Exception e) {
            logToFile(30, e.getMessage(), "Exception", ctx);

            // Show the exception in a popup dialog
            new AlertDialog.Builder(ctx)
                    .setTitle("Error")
                    .setMessage("Exception occurred: " + e.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
    */

    // NEW 22/10/2024
    public static void send_log_by_mail(final Context ctx) {
        // Show a dialog to choose the period
        new AlertDialog.Builder(ctx)
                .setTitle("Select log period")
                .setItems(new CharSequence[]{"Today", "Today and Yesterday", "1 Week", "All"},
                        (dialog, which) -> {
                            try {
                                String selectedPeriod = "";
                                switch (which) {
                                    case 0:
                                        selectedPeriod = "Today";
                                        break;
                                    case 1:
                                        selectedPeriod = "Today and Yesterday";
                                        break;
                                    case 2:
                                        selectedPeriod = "1 Week";
                                        break;
                                    case 3:
                                        selectedPeriod = "All";
                                        break;
                                }

                                Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                emailIntent.setType("vnd.android.cursor.dir/email");
                                String[] to = {"emanuele.briano@gmail.com"};
                                emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "OWL_LOGS - " + selectedPeriod);
                                emailIntent.putExtra(Intent.EXTRA_TEXT, text_logs_collection);

                                // Get today's date to filter log files
                                String currentDateString = new SimpleDateFormat("yyyyMMdd").format(new Date());

                                // Directory where logs are stored
                                File directory = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "OWL_LOGS");
                                ArrayList<Uri> uris = new ArrayList<>();

                                // Based on selection, filter logs
                                if (directory.exists() && directory.isDirectory()) {
                                    if (which == 0) {
                                        // Today
                                        collectLogs(uris, directory, currentDateString, ctx);
                                    } else if (which == 1) {
                                        // Today and Yesterday
                                        collectLogs(uris, directory, currentDateString, ctx);
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.add(Calendar.DAY_OF_MONTH, -1);
                                        String yesterday = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
                                        collectLogs(uris, directory, yesterday, ctx);
                                    } else if (which == 2) {
                                        // 1 Week
                                        Calendar calendar = Calendar.getInstance();
                                        for (int i = 0; i < 7; i++) {
                                            String date = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
                                            collectLogs(uris, directory, date, ctx);
                                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                                        }
                                    } else if (which == 3) {
                                        // All: Zip the entire folder
                                        File zipFile = zipLogs(directory);
                                        Uri zipUri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".provider", zipFile);
                                        uris.add(zipUri);
                                    }
                                }

                                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                ctx.startActivity(Intent.createChooser(emailIntent, "Send email..."));
                                text_logs_collection = ""; // Clear log collection after sending
                            } catch (Exception e) {
                                logToFile(30, e.getMessage(), "Exception", ctx);

                                // Show the exception in a popup dialog
                                new AlertDialog.Builder(ctx)
                                        .setTitle("Error")
                                        .setMessage("Exception occurred: " + e.getMessage())
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        }).show();
    }

    // Helper method to collect log files based on the date prefix
    private static void collectLogs(ArrayList<Uri> uris, File directory, String datePrefix, Context ctx) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(datePrefix)) {
                    Uri fileUri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".provider", file);
                    uris.add(fileUri);
                }
            }
        }
    }

    // Helper method to zip the log files
    private static File zipLogs(File directory) throws IOException {
        File zipFile = new File(directory.getParent(), "logs.zip");
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        zipOut.putNextEntry(zipEntry);
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = fis.read(bytes)) >= 0) {
                            zipOut.write(bytes, 0, length);
                        }
                    }
                }
            }
        }
        return zipFile;
    }


    // NEW 08/09/2022
    // TODO: TEST
    public static void send_log_files_by_mail(Context ctx) {
    }


    /// ***
    /// Logs to txt file in phone
    /// ***
    private static void logToFile(int level, String data, String level_name, Context context) {

        String owl_Name = "OWL_logs";

        // TODO: TEST
        // NEW 08/09/2022. Writes in variable to be able to email it later
        if (level >= 20) {
            write_Exception_Log_To_Mail(data);
        }

        try {
            Log.d(Constants.AppTAG, "writeToFile()");

            // NEW Android 13
            File directory;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "OWL_LOGS");
            } else {
                directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OWL_LOGS");
            }

            //File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OWL_LOGS");
            //File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + owl_Name);
            //File directory = new File(owlContext.getExternalFilesDir("") + File.separator + owl_Name);

            if(!directory.exists()) {
                directory.mkdir();
                Log.d(Constants.AppTAG,"Created " + directory.getAbsolutePath());
                if(!directory.exists())
                {
                    Log.e(Constants.AppTAG,"WARNING! Directory does not exists !!!!");
                }
            }   // creates directory if necessary
            else {
                Log.d(Constants.AppTAG,directory.getAbsolutePath() + " exists");
            }

            String currentDateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String currentTimeString = new SimpleDateFormat("HH:mm").format(new Date());

            // TODO: copiare anche in level name inferiori (20 -> 10 -> 0)
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

                    write_Exception_Log_To_Mail("File write failed: " + e.toString());
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

                write_Exception_Log_To_Mail("File write failed: " + e.toString());
            }
        }
        catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());

            write_Exception_Log_To_Mail("File write failed: " + e.toString());
        }
    }

    /// ***
    /// Logs to txt file in phone - Worker file
    /// ***
    public static void logToFile_Worker(int level, String data) {
        try {
            //Log.d(Constants.AppTAG, "writeToFile()");

            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OWL_LOGS");
            if(!directory.exists())
            {
                directory.mkdir();
                if(!directory.exists())
                {
                    Log.e(Constants.AppTAG,"WARNING! Directory does not exists !!!! Creation problems");
                }
            }
            else
            {
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
                write_Exception_Log_To_Mail(e.getMessage());
            }
        }
        catch (Exception e) {
            write_Exception_Log_To_Mail(e.getMessage());
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
