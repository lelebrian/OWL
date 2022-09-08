package it.emanuelebriano.owl;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import java.util.Timer;


public class OwlService extends IntentService {

    // TEST
    BroadcastReceiver br = new OwlBroadcastReceiver();

    int notificationId = 7373;
    String CHANNEL_ID = "Owl_service";


    private static Timer timer = new Timer();

    public OwlService() {
        super("OwlService");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Constants.AppLogDirect(10, "OwlService.onStartCommand");

        Log.i(Constants.AppTAG, "OwlService onstartCommand()");

        super.onStartCommand(intent, flags, startId);
        //startTimer();

        AlarmManager.ReadPreferences(this);

        AlarmManager.NofifyStart(this);


        return START_STICKY;
    }



    // CHECK THIS
    // https://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // This is what the service does
        Constants.AppLogDirect(10, "OwlService.onHandle");

            try {
                Log.i(Constants.AppTAG , "OWL service onHandle");

                //timer.scheduleAtFixedRate(new mainTask(), 0, period);

                while(true)
                {
                    AlarmManager.Notify_Service_Elapsed(this);

                    Thread.sleep(AlarmManager.period);
                }
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
    }

    @Override
    public void onCreate() {
        Constants.AppLogDirect(10, "OwlService.onCreate!");

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_ALL_APPS);
        this.registerReceiver(br, filter);

        super.onCreate();
    }

    @Override
    public void onDestroy() {

        Log.e(Constants.AppTAG, "OwlService.onDestroy!");
        Constants.AppLogDirect(10, "OwlService.onDestroy!");

        super.onDestroy();
        // Unregister service
        //this.unregisterReceiver(br);

        Log.i(Constants.AppTAG, "super.onDestroy() done");

        AlarmManager.CreateNotification(this, "Notification", "Service stopped", 100);
    }
}
