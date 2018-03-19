package cmu.xprize.service_ftp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * RoboSync
 * <p>
 * Created by kevindeland on 3/16/18.
 */

public class ScheduleAlarmTest extends BroadcastReceiver {

    private final static int INTERVAL_MINUTE = 60000;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("DEBUG_ALARM", "ScheduleAlarm");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());


        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        Intent newIntent = new Intent(context, TriggerAlarmTest.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, newIntent, 0);

        Log.d("DEBUG_ALARM", "Setting alarm " + newIntent.getComponent());

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), INTERVAL_MINUTE, alarmIntent);
    }
}
