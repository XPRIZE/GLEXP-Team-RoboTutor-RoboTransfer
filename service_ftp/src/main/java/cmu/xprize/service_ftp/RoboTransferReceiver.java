package cmu.xprize.service_ftp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;

/**
 * RoboSync
 * <p>
 * Created by kevindeland on 3/16/18.
 */

public class RoboTransferReceiver extends BroadcastReceiver {

    private static final String TAG = "RoboTransferReceiver";
    private final static int INTERVAL_MINUTE = 60000;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.wtf("DEBUG_BROADCAST", "RoboTransferReceiver");

        Context appContext = context.getApplicationContext();

        Intent newIntent = new Intent(appContext, WifiFTPBackgroundService.class);
        //newIntent = addExtrasToIntent(newIntent);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent alarmIntent = PendingIntent.getService(context, 0, newIntent, 0);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), INTERVAL_MINUTE, alarmIntent);

    }

    /**
     * @deprecated FTP info should be defined in RoboTransfer
     * @param intent
     * @return
     */
    Intent addExtrasToIntent(Intent intent) {

        // if possible, get from bundle
        Bundle bundle = intent.getExtras();
        if(bundle != null ) {

            String address = bundle.getString("FTP_ADDRESS");
            Log.d("DEBUG_TRANSFER", address);


            String user = bundle.getString("FTP_USER");
            user = (user != null) ? user : "";
            Log.d("DEBUG_TRANSFER", user);

            String pw = bundle.getString("FTP_PW");
            pw = pw != null ? pw : "";
            Log.d("DEBUG_TRANSFER", pw);
            int port = bundle.getInt("FTP_PORT");
            Log.d("DEBUG_TRANSFER", "" + port);


            intent.putExtra("FTP_ADDRESS", address);
            intent.putExtra("FTP_USER", user);
            intent.putExtra("FTP_PW", pw);
            intent.putExtra("FTP_PORT", port);
            intent.putExtra("FTP_PORT", port);

            intent.putExtra("FTP_READ_DIRS", (String[]) bundle.get("FTP_READ_DIRS"));
            intent.putExtra("FTP_WRITE_DIRS", (String[]) bundle.get("FTP_WRITE_DIRS"));

        }

        return intent;
    }


}
