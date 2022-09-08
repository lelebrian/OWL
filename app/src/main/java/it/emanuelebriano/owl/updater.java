package it.emanuelebriano.owl;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import static android.content.Context.DOWNLOAD_SERVICE;



public class updater {

    Context ctx;
    Activity act;

    public updater(Activity a) {
        this.act = a;
        this.ctx = a;
    }

    public void Download()
    {
        //Constants.WebLog(2, "updater.Download() called");
        Constants.AppLogDirect(0, "updated.Download() called");

        String url_new = "http://www.emanuelebriano.it/owl.apk";
        String filename = "owl.apk";

        Log.i(Constants.AppTAG, "update called 20");

        String version = Constants.VERSION;
        int verCode = Integer.parseInt(version);
        int new_version = verCode + 1;

        filename = "owl_" + String.valueOf(new_version) + ".apk";
        url_new = "http://www.emanuelebriano.it/" + filename;

        //Constants.WebLog(2, "   new url is " + url_new);
        Constants.AppLogDirect(0, "   new url is " + url_new);

        Log.i(Constants.AppTAG, "update called 30");

        if (ctx.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission","You have permission");
            //Constants.WebLog(2, "Permissions to write are OK");
            Constants.AppLogDirect(0, "Permissions to write are OK");
        }
        else
        {
            //Constants.WebLog(2, "Requesting permissions to write");
            Constants.AppLogDirect(0, "Requesting permissions to write");

            ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        try {

            Uri uri = Uri.parse(url_new);
            DownloadManager.Request r = new DownloadManager.Request(uri);

            Constants.AppLog(0, "   ... download step 20", ctx);

            // This put the download in the same Download dir the browser uses
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

            Log.i(Constants.AppTAG, "Download step 30");
            Constants.AppLog(0, "   ... download step 30", ctx);

            // When downloading music and videos they will be listed in the player
            // (Seems to be available since Honeycomb only)
            r.allowScanningByMediaScanner();

            Log.i(Constants.AppTAG, "Download step 40");
            Constants.AppLog(0, "   ... download step 40", ctx);

            // Notify user when download is completed
            // (Seems to be available since Honeycomb only)
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            Log.i(Constants.AppTAG, "Download step 50");
            Constants.AppLog(0, "   ... download step 50", ctx);

            DownloadManager dm = null;
            try {
                // Start download
                dm = (DownloadManager) ctx.getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(r);

                Log.i(Constants.AppTAG, "Download step 55");
                Constants.AppLog(0, "   ... download step 55", ctx);
            } catch (Exception e) {
                Log.e(Constants.AppTAG, e.getMessage());

                //Constants.WebLog(2, "Exception: " + e.getMessage());
                Constants.AppLogDirect(0, "Exception: " + e.getMessage());
            }

            Log.i(Constants.AppTAG, "Download step 60");
            Constants.AppLog(0, "   ... download step 60", ctx);
        }
        catch (Exception e)
        {
            //Constants.WebLog(2, "Exception: " +e.getMessage());
            Constants.AppLogDirect(20, "Download() Exception: " + e.getMessage());
        }
    }
}


/*
public class updater extends Activity {

    Context ctx;

    public updater(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void Download()
    {
        Constants.WebLog(2, "updater.Download() called");

        String url_new = "http://www.emanuelebriano.it/owl.apk";
        String filename = "owl.apk";

        Log.i(Constants.AppTAG, "update called 20");

        String version = Constants.VERSION;
        int verCode = Integer.parseInt(version);
        int new_version = verCode + 1;

        filename = "owl_" + String.valueOf(new_version) + ".apk";
        url_new = "http://www.emanuelebriano.it/" + filename;

        Constants.WebLog(2, "   new url is " + url_new);

        Log.i(Constants.AppTAG, "update called 30");

        if (ctx.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission","You have permission");
            Constants.WebLog(2, "Permissions to write are OK");
        }
        else
        {
            Constants.WebLog(2, "Requesting permissions to write");

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        try {


            Uri uri = Uri.parse(url_new);
            DownloadManager.Request r = new DownloadManager.Request(uri);

            Log.i(Constants.AppTAG, "Download step 20");

            // This put the download in the same Download dir the browser uses
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

            Log.i(Constants.AppTAG, "Download step 30");

            // When downloading music and videos they will be listed in the player
            // (Seems to be available since Honeycomb only)
            r.allowScanningByMediaScanner();

            Log.i(Constants.AppTAG, "Download step 40");

            // Notify user when download is completed
            // (Seems to be available since Honeycomb only)
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            Log.i(Constants.AppTAG, "Download step 50");

            DownloadManager dm = null;
            try {
                // Start download
                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(r);

                Log.i(Constants.AppTAG, "Download step 55");
            } catch (Exception e) {
                Log.e(Constants.AppTAG, e.getMessage());
            }

            Log.i(Constants.AppTAG, "Download step 60");
        }
        catch (Exception e)
        {
            Constants.WebLog(2, "Exception: " +e.getMessage());
        }
    }
}

*/