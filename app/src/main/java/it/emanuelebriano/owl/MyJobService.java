package it.emanuelebriano.owl;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//https://blog.klinkerapps.com/android-o-background-services/

public class MyJobService extends JobService {
    private static final int JOB_ID = 1;
    private static final int ONE_MIN = 60 * 1000;

    Activity mainAct;

    public static void schedule(Activity mainAct) {

        Context context = mainAct;

        Constants.AppLogDirect(10, "scheduling MyJobService job");

        Log.d(Constants.AppTAG, "scheduling job");

        ComponentName component = new ComponentName(context, MyJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, component)
                // schedule it to run any time between 3 - 5 minutes
                .setMinimumLatency(1 * ONE_MIN)
                .setOverrideDeadline(2 * ONE_MIN);

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        doMyWork();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // whether or not you would like JobScheduler to automatically retry your failed job.
        return false;
    }

    private void doMyWork() {
        // I am on the main thread, so if you need to do background work,
        // be sure to start up an AsyncTask, Thread, or IntentService!

        Log.d(Constants.AppTAG, "doMyWork called");

        Constants.AppLogDirect(10, "doMyWork called");

        if (Constants.active == false)
        {
            Constants.AppLogDirect(10, "   trying to restart MainActivity");

            Intent intent = new Intent(mainAct, MainActivity.class);
            startActivity(intent);
        }
        else
        {
            Constants.AppLogDirect(0, "   MainActivity active");
        }
    }
}