package it.emanuelebriano.owl;

import android.content.Context;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import android.util.Log;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static java.lang.System.currentTimeMillis;

//http://www.zoftino.com/scheduling-tasks-with-workmanager-in-android

public class MyPeriodicWorker extends Worker {

    public long DELAY_MINUTES = 5;

    String log_message = "";

    public MyPeriodicWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        UUID me_ID = this.getId();
        String s_me_ID = me_ID.toString();

        try {
            //Constants.logToFile_Worker(2,  "*** MyPeriodicWorker.doWork start (" + s_me_ID + ")");
            log_message = "*** MyPeriodicWorker.doWork start (" + s_me_ID + ")";

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
                    return Result.failure();  // NEW 08/09/2022
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
            return Result.failure();  // NEW 08/09/2022
        }

        // NEW: launching two Adepts
        try {
            log_message += "   //   Starting two Adepts in 5 and 10 minutes.";

            OneTimeWorkRequest wr_CheckAlive_1 =
                    new OneTimeWorkRequest.Builder(MyWorker.class)
                            .setInitialDelay(1 * DELAY_MINUTES, TimeUnit.MINUTES)
                            .addTag("OWL")
                            .build();
            WorkManager.getInstance().enqueueUniqueWork("OWL_Adept_1", ExistingWorkPolicy.REPLACE, wr_CheckAlive_1);

            OneTimeWorkRequest wr_CheckAlive_2 =
                    new OneTimeWorkRequest.Builder(MyWorker.class)
                            .setInitialDelay(2 * DELAY_MINUTES, TimeUnit.MINUTES)
                            .addTag("OWL")
                            .build();
            WorkManager.getInstance().enqueueUniqueWork("OWL_Adept_2", ExistingWorkPolicy.REPLACE, wr_CheckAlive_2);
        }
        catch(Exception e) {
            Log.e(Constants.AppTAG, e.getMessage());
            Constants.logToFile_Worker(10, log_message);
            Constants.logToFile_Worker(10, "   !!!   Exception listing workers: " + e.getMessage());

            // Exception in this part should not be crucial
            return Result.success();  // NEW 08/09/2022
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

                    //Log.i(Constants.AppTAG, l_Msg);
                }
            }
        }
        catch(Exception e) {
            Log.e(Constants.AppTAG, e.getMessage());
            Constants.logToFile_Worker(10, log_message);
            Constants.logToFile_Worker(10, "   !!!   Exception listing workers: " + e.getMessage());

            // Exception in this part should not be crucial
            return Result.success();  // NEW 08/09/2022
        }

        Constants.logToFile_Worker(10, log_message);
        return Result.success();  // NEW 08/09/2022
    }

}