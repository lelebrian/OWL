package it.emanuelebriano.owl;

import android.content.Context;
import android.content.IntentFilter;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static java.lang.System.currentTimeMillis;

//http://www.zoftino.com/scheduling-tasks-with-workmanager-in-android

public class MyWorker extends Worker {

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        UUID me_ID = this.getId();
        String s_me_ID = me_ID.toString();
        String log_message = "";

        try {
            //Constants.logToFile_Worker(2,  "*** MyPeriodicWorker.doWork start (" + s_me_ID + ")");
            log_message = "*** MyWorker.doWork start (" + s_me_ID + ")";

            if (Constants.myOwlBroadcastReceiver == null) {
                // Registra listener
                try {
                    //Constants.logToFile_Worker(2,  "   Registering broadcast receiver (because is null)");
                    log_message += "   //   Registering broadcast receiver (because is null)";

                    Constants.myOwlBroadcastReceiver = new OwlBroadcastReceiver();
                    getApplicationContext().registerReceiver(Constants.myOwlBroadcastReceiver, new IntentFilter("com.eveningoutpost.dexdrip.BgEstimate"));
                }
                catch (Exception e)
                {
                    Log.e(Constants.AppTAG, e.getMessage());
                    Constants.logToFile_Worker(10, log_message);
                    Constants.logToFile_Worker(2,  "   !!!   Registering broadcast receiver, Exception: "
                            + e.getMessage());
                    return Result.FAILURE;
                }
            }
            else {
                Constants.bl_now = currentTimeMillis();
                Constants.bl_last = Constants.myOwlBroadcastReceiver.last_received;
                if (Constants.bl_last == 0) {
                    //Constants.logToFile_Worker(10, "No onReceive yet");
                    log_message += "   //   No onReceive yet";
                } else {
                    Constants.bl_span = (int) ((Constants.bl_now - Constants.bl_last) / 1000);
                    //Constants.logToFile_Worker(10, String.valueOf(Constants.bl_span) + " seconds from last onReceive");
                    log_message += "   //   " + String.valueOf(Constants.bl_span) + " seconds from last onReceive";
                }
            }
        }
        catch(Exception e)
        {
            Log.e( Constants.AppTAG, e.getMessage());
            Constants.logToFile_Worker(10, log_message);
            Constants.logToFile_Worker(10, "   !!!   " + e.getMessage());
            return Result.FAILURE;
        }

        try
        {
            //Constants.logToFile_Worker(10, "Checking workers: ");
            log_message += "   //   Checking workers: ";

            // Check if it is running for real
            List<WorkInfo> lwi = WorkManager.getInstance().getWorkInfosByTag("OWL").get();

            for (WorkInfo wi : lwi) {
                if (wi.getState() != WorkInfo.State.CANCELLED)
                {
                    UUID wi_id = wi.getId();

                    String l_Msg = "   Found a WorkInfo id " + wi_id.toString() + " with status " + wi.getState().toString() + " on " + String.valueOf(lwi.size()) + " WorkInfos with tag OWL";
                    //Constants.logToFile_Worker(10, l_Msg);
                    log_message += "   //" + l_Msg;
                }
            }
        }
        catch(Exception e) {
            Log.e(Constants.AppTAG, e.getMessage());
            Constants.logToFile_Worker(10, log_message);
            Constants.logToFile_Worker(10, "   !!!   Exception listing workers: " + e.getMessage());

            // Exception in this part should not be crucial
            return Result.SUCCESS;
        }

        Constants.logToFile_Worker(10, log_message);
        return Result.SUCCESS;
    }

}