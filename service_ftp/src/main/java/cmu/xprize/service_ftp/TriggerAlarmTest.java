package cmu.xprize.service_ftp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * RoboSync
 * <p>
 * Created by kevindeland on 3/16/18.
 */

public class TriggerAlarmTest extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DEBUG_ALARM", "Did the alarm!!!");
    }
}
