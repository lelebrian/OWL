package it.emanuelebriano.owl;

import android.os.AsyncTask;

import androidx.core.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class MyDownloadTask extends AsyncTask<String, String, String>
{

    String myFilename;
    String myShortName;

    protected void onPreExecute() {
        //display progress dialog.

    }

    protected String doInBackground(String... params) {

        this.myFilename = params[0];
        this.myShortName = params[1];

        Log.i(Constants.AppTAG, "MyDownloadTask.doInBackground with myFileName " + myFilename);

        URL url = null;
        try {
            url = new URL(myFilename);
        } catch (MalformedURLException e) {
            e.printStackTrace();

            Log.e(Constants.AppTAG, e.getMessage());
        }

        HttpURLConnection con = null;
        try {
            Log.i(Constants.AppTAG, "   opening connection");

            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();

            Log.e(Constants.AppTAG, e.getMessage());
        }
        con.setDoOutput(true);
        try {


            String responseMsg = con.getResponseMessage();
            int response = con.getResponseCode();

            Log.i(Constants.AppTAG, "   response message: " + responseMsg);

            return responseMsg;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "null";
    }

    protected void onPostExecute(String result) {
        //display progress dialog.
        super.onPostExecute(result);


        // TODO: bring this to main activity

        /*

        if (Constants.owlContext.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission","You have permission");
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        Uri uri = Uri.parse(myFilename);
        DownloadManager.Request r = new DownloadManager.Request(uri);

        Log.i(Constants.AppTAG, "Download step 20");

        // This put the download in the same Download dir the browser uses
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, myShortName);

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
            dm = (DownloadManager) Constants.owlContext.getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(r);

            Log.i(Constants.AppTAG, "Download step 55");
        }
        catch (Exception e)
        {
            Log.e(Constants.AppTAG, e.getMessage());
        }

        Log.i(Constants.AppTAG, "Download step 60");

        */

    }

}