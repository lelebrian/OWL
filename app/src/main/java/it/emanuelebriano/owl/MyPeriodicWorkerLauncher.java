package it.emanuelebriano.owl;

import android.content.Context;

import androidx.annotation.NonNull;
import android.util.Log;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

//http://www.zoftino.com/scheduling-tasks-with-workmanager-in-android

public class MyPeriodicWorkerLauncher extends Worker {

    public String worker_name = "OWL_Worker";

    public MyPeriodicWorkerLauncher(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public void setName(String n)
    {
        this.worker_name = n;
    }

    @NonNull
    @Override
    public Result doWork() {

        try {

            String periodicWorkerName = getInputData().getString("periodicWorkerName");

            Constants.logToFile_Worker(10, "MyPeriodicWorkerLauncher.dowork() with periodicWorkerName = " + periodicWorkerName);

            PeriodicWorkRequest wr_CheckAlive =
                    new PeriodicWorkRequest.Builder(MyPeriodicWorker.class, 5, TimeUnit.MINUTES)
                            .addTag("OWL")
                            .build();

            // TODO: Check work policy
            WorkManager.getInstance().enqueueUniquePeriodicWork(periodicWorkerName, ExistingPeriodicWorkPolicy.REPLACE, wr_CheckAlive);

            String params = "";
            params += " - NetworkType: " + wr_CheckAlive.getWorkSpec().constraints.getRequiredNetworkType().toString();
            params += " - RequiresBatteryNotLow: " + String.valueOf( wr_CheckAlive.getWorkSpec().constraints.requiresBatteryNotLow() );
            params += " - RequiresCharging: " + String.valueOf( wr_CheckAlive.getWorkSpec().constraints.requiresCharging() );
            params += " - RequiresDeviceIdle: " + String.valueOf( wr_CheckAlive.getWorkSpec().constraints.requiresDeviceIdle() );
            params += " - RequiresStorageNotLow: " + String.valueOf( wr_CheckAlive.getWorkSpec().constraints.requiresStorageNotLow() );

            Constants.logToFile_Worker(10, "Scheduled new PeriodicWorker with name OWL_Worker with params " + params + ". Checking:");

            // Check if it is running for real
            List<WorkInfo> lwi = WorkManager.getInstance().getWorkInfosByTag("OWL").get();
            for (WorkInfo wi : lwi) {
                if (wi.getState() != WorkInfo.State.CANCELLED)
                {

                    UUID wi_id = wi.getId();
                    String l_Msg = "   Found a WorkInfo id " + wi_id.toString() + " with status " + wi.getState().toString() + " on " + String.valueOf(lwi.size()) + " WorkInfos with tag OWL";
                    Constants.logToFile_Worker(10, l_Msg);

                    Log.i(Constants.AppTAG, l_Msg);
                }
            }
        }
        catch(Exception e) {
            Log.e(Constants.AppTAG, e.getMessage());

            Constants.logToFile_Worker(10, "Exception scheduling new MyWorker: " + e.getMessage());
        }

        return Result.success();  // NEW 08/09/2022
    }

}