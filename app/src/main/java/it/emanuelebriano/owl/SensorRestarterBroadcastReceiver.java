package it.emanuelebriano.owl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Constants.AppTAG, "Service Stops! Oooooooooooooppppssssss!!!!");
        context.startService(new Intent(context, OwlService.class));;
    }

}
