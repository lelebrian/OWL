package it.emanuelebriano.owl;

import android.Manifest;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.NetworkOnMainThreadException;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Intent mServiceIntent;



    private DrawerLayout mDrawerLayout;

    WebView myWebView;

    // Starts the JobIntentService
    private static final int RSS_JOB_ID = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(Constants.AppTAG, "Creating MainActivity()");

        Constants.owlContext = getApplicationContext();

        //Constants.logs.add("Start");

        super.onCreate(savedInstanceState);

        Log.i(Constants.AppTAG, "Creating MainActivity() step 10");

        setContentView(R.layout.activity_main2);

        Log.i(Constants.AppTAG, "Creating MainActivity() step 20");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Load_Mad();

/*
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        */

/*

        try {

            this.mDrawerLayout.addDrawerListener(
                    new DrawerLayout.DrawerListener() {
                        @Override
                        public void onDrawerSlide(View drawerView, float slideOffset) {
                            // Respond when the drawer's position changes
                            Log.i(Constants.AppTAG, "onDrawerSlide");
                        }

                        @Override
                        public void onDrawerOpened(View drawerView) {
                            // Respond when the drawer is opened
                            Log.i(Constants.AppTAG, "onDrawerOpened");
                        }

                        @Override
                        public void onDrawerClosed(View drawerView) {
                            // Respond when the drawer is closed
                            Log.i(Constants.AppTAG, "onDrawerClosed");
                        }

                        @Override
                        public void onDrawerStateChanged(int newState) {
                            // Respond when the drawer motion state changes
                            Log.i(Constants.AppTAG, "onDrawerStateChanged");
                        }
                    }
            );
        }
        catch(Exception e)
        {
            Log.e(Constants.AppTAG, e.getMessage());
        }

        */

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Log.i(Constants.AppTAG, "   preferencemanager initialised");

        // GETS Version and initialize
        String version = AlarmManager.Get_Version(this);

        if (Constants.IsFirstLaunch()) {
            // ADD FIRST LAUNCH CODE HERE
            Constants.Initialise(version);
            Constants.AppLogDirect(20, "Created new OWL MainActivity version " + version
                    + " randomID " + String.valueOf(Constants.randomID));
        }

        Restart_Workers(false);

        Constants.SetActive(true);
        // ---

        final String txtInfo = "Version " + version;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, txtInfo, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Log.i(Constants.AppTAG, "Creating MainActivity() step 90");

        // Aggiornare servizio
        //Intent i_StartOwlService = new Intent(this, OwlService.class);
        //startService(i_StartOwlService);

        Log.i(Constants.AppTAG, "   service started");

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("AddLog"));

        Log.i(Constants.AppTAG, "Trying to schedule job");

        /*
        MyJobService mjs = new MyJobService();
        mjs.schedule(this);
        */
    }

    public void Restart_Workers(boolean force_Stop) {
        Constants.logToFile_Worker(10, "Restart_Workers()");

        if (force_Stop) {
            Constants.logToFile_Worker(10, "STOPPING ALL WORKERS BY REQUEST");
            WorkManager.getInstance().cancelAllWorkByTag("OWL");

            // NEW
            WorkManager.getInstance().pruneWork();
        }

        // CHECK WORKERS
        boolean workers_Running = false;
        // Check if it is running for real
        List<WorkInfo> lwi = null;
        try {
            lwi = WorkManager.getInstance().getWorkInfosByTag("OWL").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        for (WorkInfo wi : lwi) {
            if (wi.getState() != WorkInfo.State.CANCELLED) {
                String s = "";
                UUID wi_id = wi.getId();
                s += wi_id.toString() + "          ";
                s += wi.getState() + "          ";
                s += wi.getOutputData().toString() + "          ";
                Constants.logToFile_Worker(10, "Found worker at MainActivity start: " + s);

                workers_Running = true;
            }
        }

        if (workers_Running == false) {
            Constants.logToFile_Worker(10, "Starting first workers because no worker tagged OWL is running");

            // Eventually Deletes all workers
            //WorkManager.getInstance().pruneWork();

            /* ONE TIME WORK REQUEST
            try {
                OneTimeWorkRequest wr_CheckAlive =
                        new OneTimeWorkRequest.Builder(MyWorker.class)
                                .setInitialDelay(10, TimeUnit.SECONDS)
                                .addTag("OWL")
                                .build();

                //WorkManager.getInstance().enqueue(wr_CheckAlive);
                WorkManager.getInstance().enqueueUniqueWork("OWL_Worker", ExistingWorkPolicy.REPLACE, wr_CheckAlive);
            }
            catch (Exception e)
            {
                Constants.WebLog(10, "Exception starting worker: " + e.getMessage());
            }
            */


            try {
                /*
                Data source_1 = new Data.Builder()
                        .putString("periodicWorkerName", "OWL_PeriodicWorker_1")
                        .build();
                OneTimeWorkRequest wr_CheckAlive_1 =
                        new OneTimeWorkRequest.Builder(MyPeriodicWorkerLauncher.class)
                                .setInputData(source_1)
                                .setInitialDelay(4, TimeUnit.MINUTES)
                                .addTag("OWL")
                                .build();
                WorkManager.getInstance().enqueueUniqueWork("OWL_Worker_1", ExistingWorkPolicy.REPLACE, wr_CheckAlive_1);

                Data source_2 = new Data.Builder()
                        .putString("periodicWorkerName", "OWL_PeriodicWorker_2")
                        .build();
                OneTimeWorkRequest wr_CheckAlive_2 =
                        new OneTimeWorkRequest.Builder(MyPeriodicWorkerLauncher.class)
                                .setInputData(source_2)
                                .setInitialDelay(9, TimeUnit.MINUTES)
                                .addTag("OWL")
                                .build();
                WorkManager.getInstance().enqueueUniqueWork("OWL_Worker_2", ExistingWorkPolicy.REPLACE, wr_CheckAlive_2);

                Data source_3 = new Data.Builder()
                        .putString("periodicWorkerName", "OWL_PeriodicWorker_3")
                        .build();
                OneTimeWorkRequest wr_CheckAlive_3 =
                        new OneTimeWorkRequest.Builder(MyPeriodicWorkerLauncher.class)
                                .setInputData(source_3)
                                .setInitialDelay(14, TimeUnit.MINUTES)
                                .addTag("OWL")
                                .build();
                WorkManager.getInstance().enqueueUniqueWork("OWL_Worker_3", ExistingWorkPolicy.REPLACE, wr_CheckAlive_3);

                Constants.logToFile_Worker(10, "Enqueued 3 start workers. Checking");
                */

                // PERIODIC
                String periodicWorkerName = "OWL_Master";
                PeriodicWorkRequest wr_CheckAlive =
                        new PeriodicWorkRequest.Builder(MyPeriodicWorker.class, 15, TimeUnit.MINUTES)
                                .addTag("OWL")
                                .build();

                // TODO: Check work policy
                WorkManager.getInstance().enqueueUniquePeriodicWork(periodicWorkerName, ExistingPeriodicWorkPolicy.REPLACE, wr_CheckAlive);

                Constants.logToFile_Worker(10, "Scheduled new PeriodicWorker with name OWL_Master. Checking: ");

                // Check if it is running for real
                lwi = null;
                try {
                    lwi = WorkManager.getInstance().getWorkInfosByTag("OWL").get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                for (WorkInfo wi : lwi) {
                    String s = "";
                    UUID wi_id = wi.getId();
                    s += wi_id.toString() + "          ";
                    s += wi.getState() + "          ";
                    s += wi.getOutputData().toString() + "          ";
                    Constants.logToFile_Worker(10, "   Found worker: " + s);
                }
            } catch (Exception e) {
                Constants.logToFile_Worker(20, "Exception starting worker: " + e.getMessage());
            }
        } else {
            Constants.logToFile_Worker(10, "Therefore just recreating MainActivity version " + Constants.VERSION
                    + " randomID " + String.valueOf(Constants.randomID));
        }
    }


    public void Load_Mad() {
        String url = getString(R.string.mad_url);

        myWebView = (WebView) findViewById(R.id.web_view_main);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                Log.i(Constants.AppTAG, "onPageFinished()");

                super.onPageFinished(myWebView, url);

                String id = "start101";
                myWebView.loadUrl("javascript:scrollAnchor(" + id + ");");
                /*
                    super.onPageFinished(myWebView, url);
                    myWebView.loadUrl(url + "#td101");
                    */
            }
        });
        myWebView.loadUrl(url);

        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);
    }


    public void Load_Weblog() {
        String url = getString(R.string.weblog_url);
        myWebView = (WebView) findViewById(R.id.web_view_main);
        myWebView.setWebViewClient(new WebViewClient());

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
        url += "weblog";
        String anno = String.valueOf(cal.get(Calendar.YEAR));
        if (anno.length() == 1) anno = "0" + anno;
        url += anno;
        String mese = String.valueOf(cal.get(Calendar.MONTH) + 1);
        if (mese.length() == 1) mese = "0" + mese;
        url += mese;
        String giorno = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        if (giorno.length() == 1) giorno = "0" + giorno;
        url += giorno;
        url += ".txt";

        Log.i(Constants.AppTAG, "Load_Weblog with url" + url);

        myWebView.loadUrl(url);
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                String action = intent.getAction();
                Log.i(Constants.AppTAG, "MainActivity Broadcast received with action " + action);

                String message = intent.getStringExtra("message");

                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
                String HH = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
                if (HH.length() == 1) HH = "0" + HH;
                String mm = String.valueOf(cal.get(Calendar.MINUTE));
                if (mm.length() == 1) mm = "0" + mm;

                message = HH + ":" + mm + ": " + message;

                Log.i(Constants.AppTAG, "message to AppLog is " + message + ", lastAppLog is " + Constants.lastAppLog);
                if (message != Constants.lastAppLog) {
                    Constants.lastAppLog = message;
                    Constants.Add_AppLog(message);
                } else {
                    Log.e(Constants.AppTAG, "MainActivity: duplicate AppLog message");
                }



/*
                synchronized(mRecyclerView){
                    mAdapter = new MyAdapter(Constants.Get_AppLog());
                    mRecyclerView.setAdapter(mAdapter);
                    //mRecyclerView.notify();
                }
                */

            } catch (Exception e) {
                Constants.Clear_Logs();
                Constants.Add_AppLog(e.getMessage());

                /*
                synchronized(mRecyclerView){
                    mAdapter = new MyAdapter(Constants.Get_AppLog());
                    mRecyclerView.setAdapter(mAdapter);
                    //mRecyclerView.notify();
                }
                */

                Log.e(Constants.AppTAG, "Exception: " + e.getMessage());
            }
        }
    };

    protected void onDestroy() {

        Log.e(Constants.AppTAG, "MainActivity.onDestroy");

        super.onDestroy();

        Constants.AppLogDirect(0, "ON_DESTROY");

        Log.e(Constants.AppTAG, "MainActivity.super.onDestroy() done");
    }

    @Override
    protected void onStop() {

        Log.e(Constants.AppTAG, "MainActivity.onStop");

        Constants.SetActive(false);

        // call the superclass method first
        super.onStop();

        //Log.e(Constants.AppTAG, "Trying to restart");
        //PowerManager pm = (PowerManager) this.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        //pm.reboot("anti-crash");

        Constants.AppLogDirect(0, "ON_STOP");

        Log.e(Constants.AppTAG, "MainActivity.super.onStop() done");
    }

    @Override
    protected void onPause() {

        Log.e(Constants.AppTAG, "MainActivity.onPause()");

        // call the superclass method first
        super.onPause();

        Constants.AppLogDirect(0, "ON_PAUSE");

        Log.e(Constants.AppTAG, "MainActivity.super.onPause() done");
    }

    @Override
    public void onStart() {

        Constants.SetActive(true);

        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.i(Constants.AppTAG, item.getItemId() + " pressed.");

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                Log.i(Constants.AppTAG, "Action settings identified()");

                try {
                    // Settings clicked
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(Constants.AppTAG, "Exception " + e.getMessage());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main2, menu);

        // old
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Log.i(Constants.AppTAG, "onNavigationItemSelected id = '" + id + "'");

        if (id == R.id.nav_mad) {
            Load_Mad();
        } else if (id == R.id.nav_test_read_forecast) {
            //Load_Weblog();
            AlarmManager.ReadForecast(this);
        } else if (id == R.id.nav_update) {

            /*
            String url = check_and_update(this);

            Constants.WebLog(2, "Manual Check for updates - url answered is: " + url);

            if (url == "")
            {
                Snackbar.make(findViewById(R.id.drawer_layout), "No update available", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Constants.WebLog(2, "No updates available");
            }
            else
            {
                try {
                    updater u = new updater();
                    u.Download();
                }
                catch(Exception e)
                {
                    Snackbar.make(findViewById(R.id.drawer_layout), e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    Constants.WebLog(2, "Exception: " + e.getMessage());
                }
            }
            */

            Constants.AppLog(0, "Update called", this);

            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;
            int new_version = verCode + 1;
            String short_Name = "owl_" + String.valueOf(new_version) + ".apk";
            String url_new = "http://www.emanuelebriano.it/" + short_Name;

            Constants.AppLog(0, "Trying to download from " + url_new, this);

            // NEW 01/04/2019
            Update(url_new, short_Name);

            // LAST WORKING - commented 01/04/2019 (broken)
            /*






            // Antother test under
            /*

            MyDownloadTask dt = new MyDownloadTask();
            String resp = "";
            try {
                resp = dt.execute(url_new, short_Name).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(Constants.AppTAG, e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                Log.e(Constants.AppTAG, e.getMessage());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.e(Constants.AppTAG, e.getMessage());
            }

            Log.i(Constants.AppTAG, "Got response: *" + resp + "*");

            if (resp != "ok")
            {
                Log.i(Constants.AppTAG, "Resp is not OK");

                Snackbar.make(findViewById(R.id.drawer_layout), "No update available", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Constants.WebLog(2, "No updates available.");
            }
            else
            {

                Log.i(Constants.AppTAG, "Resp is OK");

                try {
                    updater u = new updater();
                    u.Download();
                }
                catch(Exception e)
                {
                    Snackbar.make(findViewById(R.id.drawer_layout), e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    Constants.WebLog(2, "Exception: " + e.getMessage());
                }
            }
            */
        } else if (id == R.id.send_logs_by_mail) {
            Constants.send_log_by_mail(this);
        } else if (id == R.id.nav_snooze_0) {

            Constants.AppLogDirect(0, "Snooze 0 pressed");

            // Sets the snooze
            AlarmManager.setSnooze(0);
        } else if (id == R.id.nav_snooze_15) {

            Constants.AppLogDirect(0, "Snooze 30 pressed");

            // Sets the snooze
            AlarmManager.setSnooze(15);
        } else if (id == R.id.nav_snooze_30) {

            Constants.AppLogDirect(0, "Snooze 30 pressed");

            // Sets the snooze
            AlarmManager.setSnooze(30);
        } else if (id == R.id.nav_snooze_60) {

            Constants.AppLogDirect(0, "Snooze 60 pressed");

            // Sets the snooze
            AlarmManager.setSnooze(60);
        } else if (id == R.id.nav_test_alarm) {

            Constants.AppLogDirect(0, "Testing Alarm");

            // Also tests alarm
            AlarmManager.CreateNotification(this, "Test Alarm", "level = 20", 20);
        } else if (id == R.id.nav_test_alarm_high) {

            //Constants.WebLog(2, "Testing Alarm High");

            Constants.AppLogDirect(0, "Testing Alarm High");

            // Also tests alarm
            AlarmManager.CreateNotification(this, "Test Alarm High", "High level = 30", 30);
        } else if (id == R.id.nav_restart_workers) {

            Constants.AppLogDirect(10, "Restarting workers");

            Restart_Workers(true);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static String check_and_update(Context ctx) {
        try {

            // TODO: Add log retrieve

            PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;
            int new_version = verCode + 1;

            // TODO: Check newer versions
            // TODO: Add update on/off switch in preferences
            String url_new = "http://www.emanuelebriano.it/owl_" + String.valueOf(new_version) + ".apk";

            Constants.AppLogDirect(10, "Checking for new update: " + url_new);

            if (exists(url_new)) {
                Constants.AppLogDirect(10, "New version available !");

                return url_new;
            } else {
                Constants.AppLogDirect(10, "No new version available");

                return "";
            }
        } catch (PackageManager.NameNotFoundException e) {

            Constants.AppLogDirect(20, "Exception" + e.getMessage());

            return e.getMessage();
        }
    }

    public static boolean exists(String URLName) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");

            Constants.AppLogDirect(0, "MainActivity.exists() response code: " + con.getResponseCode());

            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            Constants.AppLogDirect(20, "MainActivity.exists() Exception: " + e.getMessage());
            return false;
        }
    }

    public void Update(String apk_url, String apk_name) {

        Constants.AppLog(0, "Update(" + apk_url + "," + apk_name + ")", this);

        String url = apk_url;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);

        /*
        new Thread(new Runnable() {
            public void run() {
                try {
                    PackageInfo pInfo = null;
                    try {
                        pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    String version = pInfo.versionName;
                    int verCode = pInfo.versionCode;
                    int new_version = verCode + 1;
                    String apk_name = "owl_" + String.valueOf(new_version) + ".apk";
                    String apk_url = "http://www.emanuelebriano.it/" + apk_name;

                    //Constants.AppLog(0, "   step 0", getApplicationContext());

                    URL url = new URL(apk_url);
                    //Constants.AppLog(0, "   step 0.1", getApplicationContext());

                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    //Constants.AppLog(0, "   step 0.2", getApplicationContext());
                    c.setRequestMethod("GET");
                    //Constants.AppLog(0, "   step 0.3", getApplicationContext());
                    c.setDoOutput(true);
                    //Constants.AppLog(0, "   step 0.4", getApplicationContext());
                    int respcode = c.getResponseCode();
                    //Constants.AppLog(0, "   response code is " + String.valueOf(respcode), getApplicationContext());

                    c.connect();

                    //Constants.AppLog(0, "   step 1", getApplicationContext());

                    String PATH = Environment.getExternalStorageDirectory() + "/download/";
                    File file = new File(PATH);
                    file.mkdirs();
                    File outputFile = new File(file, apk_name);
                    FileOutputStream fos = new FileOutputStream(outputFile);

                    //Constants.AppLog(0, "   step 2", getApplicationContext());

                    InputStream is = c.getInputStream();

                    byte[] buffer = new byte[1024];
                    int len1 = 0;
                    while ((len1 = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len1);
                    }
                    fos.close();
                    is.close();//till here, it works fine - .apk is download to my sdcard in download file

                    //Constants.AppLog(0, "   step 3", getApplicationContext());

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + apk_name)), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    //Constants.AppLog(0, "   step 4", getApplicationContext());
                }
                catch (Exception e) {
                    String type = e.getClass().getCanonicalName();
                    Constants.AppLog(0, "Update " + type + " error! " + e.getMessage(), getApplicationContext());
                    Constants.AppLog(0, "Update error stack trace: " + e.getStackTrace(), getApplicationContext());

                    Toast.makeText(getApplicationContext(), "Update " + type + "error! " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).start();
        */
    }
}
