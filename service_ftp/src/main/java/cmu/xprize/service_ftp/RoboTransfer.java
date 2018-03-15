package cmu.xprize.service_ftp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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

            ftpIntent.putExtra("FTP_ADDRESS", (String) bundle.get("FTP_ADDRESS"));
            ftpIntent.putExtra("FTP_USER", (String) bundle.get("FTP_USER"));
            ftpIntent.putExtra("FTP_PW", (String) bundle.get("FTP_PW"));
            ftpIntent.putExtra("FTP_PORT", (int) bundle.get("FTP_PORT"));

            ftpIntent.putExtra("FTP_READ_DIRS", (String[]) bundle.get("FTP_READ_DIRS"));
            ftpIntent.putExtra("FTP_WRITE_DIRS", (String[]) bundle.get("FTP_WRITE_DIRS"));

        }

        startService(ftpIntent);

        finish();
    }
}
