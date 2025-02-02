package it.emanuelebriano.owl;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1001;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1002;
    Intent mServiceIntent;

    private DrawerLayout mDrawerLayout;

    WebView myWebView;

    // Starts the JobIntentService
    private static final int RSS_JOB_ID = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Constants.AppTAG, "Creating MainActivity()");

        Constants.owlContext = getApplicationContext();
        super.onCreate(savedInstanceState);

        Log.i(Constants.AppTAG, "Creating MainActivity() step 10");

        // NEW 20/10/2024 for Android 13 - test!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestStoragePermissionsForTiramisu();
        } else {
            requestLegacyStoragePermissions();
        }
        // end NEW

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

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Log.i(Constants.AppTAG, "   preferencemanager initialised");

        // GETS Version and initialize
        String version = AlarmManager.Get_Version(this);

        if (Constants.IsFirstLaunch()) {
            // ADD FIRST LAUNCH CODE HERE
            Constants.Initialise(version);
            Constants.AppLogDirect(20, "Created new OWL MainActivity version " + version
                    + " randomID " + String.valueOf(Constants.randomID), getApplicationContext());
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
    }

    private void requestStoragePermissionsForTiramisu() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_CODE_NOTIFICATION_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
            }, REQUEST_CODE_STORAGE_PERMISSION);
        }
    }

    private void requestLegacyStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with file writing
            } else {
                // Show a Toast notification for permission denial
                Toast.makeText(this, "Storage permission denied. Cannot save logs.", Toast.LENGTH_LONG).show();

                // Optionally, show a dialog explaining the importance of the permission
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app needs storage permission to save log files.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
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

            try {

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
            } catch (Exception e) {
                Constants.Clear_Logs();
                Constants.Add_AppLog(e.getMessage());

                Log.e(Constants.AppTAG, "Exception: " + e.getMessage());
            }
        }
    };

    protected void onDestroy() {

        Log.e(Constants.AppTAG, "MainActivity.onDestroy");

        super.onDestroy();

        Constants.AppLogDirect(0, "ON_DESTROY", getApplicationContext());

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

        Constants.AppLogDirect(0, "ON_STOP", getApplicationContext());

        Log.e(Constants.AppTAG, "MainActivity.super.onStop() done");
    }

    @Override
    protected void onPause() {

        Log.e(Constants.AppTAG, "MainActivity.onPause()");

        // call the superclass method first
        super.onPause();

        Constants.AppLogDirect(0, "ON_PAUSE", getApplicationContext());

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

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Refresh the menu, especially the Snooze minutes left
        Constants.AppLogDirect(20, "On Prepare Options Menu", getApplicationContext());

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_snooze_0);

        if (item != null) {
            int left = AlarmManager.get_Snooze_Left();
            if (left < 0) {
                item.setTitle("NOTIFY");
            } else {
                item.setTitle("NOTIFY (" + String.valueOf(left) + ")");
            }
        }
        else
        {
            Constants.AppLogDirect(20, "nav_snooze_0 not found in menu", getApplicationContext());
            Constants.AppLogDirect(20, "Item 0 is " + menu.getItem(0).getItemId(), getApplicationContext());
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public void UpdateSnoozeButton()
    {
        try
        {
            invalidateOptionsMenu();
        }
        catch(Exception e)
        {
            Constants.write_Exception_Log_To_Mail(e.getMessage());
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Constants.AppLogDirect(0, "onNavigationItemSelected id = '" + id + "'", getApplicationContext());

        if (id == R.id.nav_mad) {
            Load_Mad();
        } else if (id == R.id.nav_test_read_forecast) {
            //Load_Weblog();
            AlarmManager.ReadForecast(this);
        } else if (id == R.id.nav_update) {
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

        } else if (id == R.id.send_logs_by_mail) {
            Constants.send_log_by_mail(this);
        } else if (id == R.id.nav_snooze_0) {
            Constants.AppLogDirect(0, "Snooze 0 pressed", getApplicationContext());

            // Sets the snooze
            AlarmManager.setSnooze(0, getApplicationContext());

            UpdateSnoozeButton();
        } else if (id == R.id.nav_snooze_15) {

            Constants.AppLogDirect(0, "Snooze 15 pressed", getApplicationContext());

            // Sets the snooze
            AlarmManager.setSnooze(15, getApplicationContext());

            UpdateSnoozeButton();

            new CountDownTimer(15*60*1000, 60*1000) {

                public void onTick(long millisUntilFinished) {
                    UpdateSnoozeButton();
                }

                public void onFinish() {
                    UpdateSnoozeButton();
                }

            }.start();
        } else if (id == R.id.nav_snooze_30) {

            Constants.AppLogDirect(0, "Snooze 30 pressed", getApplicationContext());

            // Sets the snooze
            AlarmManager.setSnooze(30, getApplicationContext());

            UpdateSnoozeButton();

            new CountDownTimer(30*60*1000, 60*1000) {

                public void onTick(long millisUntilFinished) {
                    UpdateSnoozeButton();
                }

                public void onFinish() {
                    UpdateSnoozeButton();
                }

            }.start();
        } else if (id == R.id.nav_snooze_60) {

            Constants.AppLogDirect(0, "Snooze 60 pressed", getApplicationContext());

            // Sets the snooze
            AlarmManager.setSnooze(60, getApplicationContext());

            UpdateSnoozeButton();

            new CountDownTimer(60*60*1000, 60*1000) {

                public void onTick(long millisUntilFinished) {
                    UpdateSnoozeButton();
                }

                public void onFinish() {
                    UpdateSnoozeButton();
                }

            }.start();
        } else if (id == R.id.nav_test_alarm) {

            Constants.AppLogDirect(0, "Testing Alarm", getApplicationContext());

            // Also tests alarm
            AlarmManager.CreateNotification(this, "Test Alarm", "level = 20", 20);
        } else if (id == R.id.nav_test_alarm_high) {
            Constants.AppLogDirect(0, "Testing Alarm High", getApplicationContext());

            // Also tests alarm
            AlarmManager.CreateNotification(this, "Test Alarm High", "High level = 30", 30);
        } else if (id == R.id.nav_test_alarm_max) {
            Constants.AppLogDirect(0, "Testing Alarm Max", getApplicationContext());

            // Also tests alarm
            AlarmManager.CreateNotification(this, "Test Alarm Max", "Max level = 40", 40);
        }
        else if (id == R.id.nav_restart_workers) {

            Constants.AppLogDirect(10, "Restarting workers", getApplicationContext());

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

            Constants.AppLogDirect(10, "Checking for new update: " + url_new, ctx);

            if (exists(url_new)) {
                Constants.AppLogDirect(10, "New version available !", ctx);

                return url_new;
            } else {
                Constants.AppLogDirect(10, "No new version available", ctx);

                return "";
            }
        } catch (PackageManager.NameNotFoundException e) {

            Constants.AppLogDirect(20, "Exception" + e.getMessage(), ctx);

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

            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            return false;
        }
    }

    public void Update(String apk_url, String apk_name) {

        Constants.AppLog(0, "Update(" + apk_url + "," + apk_name + ")", this);

        String url = apk_url;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);

    }


}
