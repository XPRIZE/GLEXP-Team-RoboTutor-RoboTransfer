package cmu.xprize.service_ftp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * RoboSync
 * <p>
 * Created by kevindeland on 3/16/18.
 */

public class RoboTransferReceiver extends BroadcastReceiver {

    private static final String TAG = "RoboTransferReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.wtf("DEBUG_BROADCAST", "RoboTransferReceiver");

        Context appContext = context.getApplicationContext();

        Intent newIntent = new Intent(appContext, WifiFTPBackgroundService.class);

        // get from bundle
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


            newIntent.putExtra("FTP_ADDRESS", address);
            newIntent.putExtra("FTP_USER", user);
            newIntent.putExtra("FTP_PW", pw);
            newIntent.putExtra("FTP_PORT", port);

            newIntent.putExtra("FTP_READ_DIRS", (String[]) bundle.get("FTP_READ_DIRS"));
            newIntent.putExtra("FTP_WRITE_DIRS", (String[]) bundle.get("FTP_WRITE_DIRS"));

        }


        appContext.startService(newIntent);
    }
}
