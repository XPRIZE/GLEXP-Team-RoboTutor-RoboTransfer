package cmu.xprize.service_ftp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * RoboSync
 * <p>
 * Created by kevindeland on 3/15/18.
 */

public class RoboTransfer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent ftpIntent = new Intent(this, WifiFTPBackgroundService.class);

        // get from bundle
        Bundle bundle = getIntent().getExtras();

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


            ftpIntent.putExtra("FTP_ADDRESS", address);
            ftpIntent.putExtra("FTP_USER", user);
            ftpIntent.putExtra("FTP_PW", pw);
            ftpIntent.putExtra("FTP_PORT", port);

            ftpIntent.putExtra("FTP_READ_DIRS", (String[]) bundle.get("FTP_READ_DIRS"));
            ftpIntent.putExtra("FTP_WRITE_DIRS", (String[]) bundle.get("FTP_WRITE_DIRS"));

        }

        startService(ftpIntent);

        finish();
    }
}
