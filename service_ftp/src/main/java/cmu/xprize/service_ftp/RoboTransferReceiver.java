package cmu.xprize.service_ftp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cmu.xprize.service_ftp.logging.CErrorManager;
import cmu.xprize.service_ftp.logging.CLogManager;
import cmu.xprize.service_ftp.logging.ILogManager;
import cmu.xprize.service_ftp.logging.TLOG_CONST;

/**
 * RoboSync
 * <p>
 * Created by kevindeland on 3/16/18.
 */

public class RoboTransferReceiver extends BroadcastReceiver {

    private static final String TAG = "RoboTransferReceiver";
    private static final String DEBUG_TAG = "DEBUG_LAUNCH";
    private final static long INTERVAL_MINUTE = 60000;

    // logging stuff
    static public ILogManager logManager;
    String FTP_LOG_PATH = Environment.getExternalStorageDirectory() + "/FTP/";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(DEBUG_TAG, TAG);

        // Start logging

        logManager = CLogManager.getInstance();

        String initTime     = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String logFilename  = "FTP_" + initTime;

        logManager.startLogging(FTP_LOG_PATH, logFilename);
        CErrorManager.setLogManager(logManager);

        logManager.postDateTimeStamp(TAG, "onReceive");


        Context appContext = context.getApplicationContext();

        Intent newIntent = new Intent(appContext, WifiFTPBackgroundService.class);
        //newIntent = addExtrasToIntent(newIntent);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent alarmIntent = PendingIntent.getService(context, 0, newIntent, 0);

        long repeatTime;

        int repeatSetting = appContext.getResources().getInteger(R.integer.repeat_time_minutes);
        switch(repeatSetting) {
            case 1:
                repeatTime = INTERVAL_MINUTE;
                break;
            case 15:
                repeatTime = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                break;
            case 30:
                repeatTime = AlarmManager.INTERVAL_HALF_HOUR;
                break;
            case 60:
            default:
                repeatTime = AlarmManager.INTERVAL_HOUR;
                break;
        }

        logManager.postEvent_I(TAG, "RepeatTime:" + String.valueOf(repeatSetting));

        // REVIEW what would cause alarmMgr to be null?
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), repeatTime, alarmIntent);

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
