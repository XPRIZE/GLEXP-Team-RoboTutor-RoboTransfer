package cmu.xprize.service_ftp;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * MyNotificationManager
 * <p>
 * Created by kevindeland on 2/15/18.
 */
public class WifiFTPBackgroundService extends Service {


    private static Timer timer = new Timer();
    private MyNotificationManager mNotifyManager;
    private ConnectFTP mConnectFTP;

    private boolean isStarted = false;

    // if it's already running, don't start another service
    private boolean isUploading = false;

    private static final String TAG = "WifiFTPService";
    private static final String DEBUG_TAG = "DEBUG_LAUNCH:FTP";

    private static final String SCHEDULE_TYPE = "alarm";
    static final long BACKGROUND_CHECK_DELAY = 0;
    static final long BACKGROUND_CHECK_PERIOD = 5000;

    String address;
    String user;
    String pw;
    int port;

    String[] in_dir;
    String[] out_dir;



    public WifiFTPBackgroundService() {
    }

    /**
     * STEP 1 Called when Service is started.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(DEBUG_TAG, "WifiFTPBackgroundService");

        if (!isStarted) {

            mNotifyManager = new MyNotificationManager(this);
            mConnectFTP = new ConnectFTP();

            // set defaults
            address = getResources().getString(R.string.address);
            user = getResources().getString(R.string.user);
            pw = getResources().getString(R.string.pw);
            port = getResources().getInteger(R.integer.port);

            in_dir = getResources().getStringArray(R.array.in_dir);
            out_dir = getResources().getStringArray(R.array.out_dir);

            // retrievExtrasFromIntent(intent);


            // STEP 2 a new "CheckConnectionsTask" is run periodically
            switch (SCHEDULE_TYPE) {
                case "timer":
                    timer.scheduleAtFixedRate(new CheckConnectionsTask(), BACKGROUND_CHECK_DELAY, BACKGROUND_CHECK_PERIOD);
                    break;

                case "alarm":

                    timer.schedule(new CheckConnectionsTask(), BACKGROUND_CHECK_DELAY);

                    break;
            }

        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * @deprecated FTP info can no longer be passed from Intent
     * @param intent {@link Intent} which started the service
     */
    void retrievExtrasFromIntent(Intent intent) {
        // get from bundle
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            address = bundle.getString("FTP_ADDRESS", address);
            user = bundle.getString("FTP_USER", user);
            pw = bundle.getString("FTP_PW", pw);
            port = bundle.getInt("FTP_PORT", port);

            String[] temp_in_dir = bundle.getStringArray("FTP_READ_DIRS");
            in_dir = temp_in_dir != null ? temp_in_dir : in_dir;

            String[] temp_out_dir = bundle.getStringArray("FTP_WRITE_DIRS");
            out_dir = temp_out_dir != null ? temp_out_dir : out_dir;
        }
    }

    /**
     * STEP 3 the "run" method of the CheckConnections Task is called
     */
    private class CheckConnectionsTask extends TimerTask {

        @Override
        public void run() {

            // if already transferring files, don't start another
            if(isUploading) {
                return;
            }

            Log.d(DEBUG_TAG, "Running CheckConnectionsTask");

            // STEP 4 checks for wifi connection
            if (isConnectedViaWifi()) {
                Log.d(DEBUG_TAG, "Connected via Wifi");
                String wifiName = getWifiName();
                mNotifyManager.issueNotification(MyNotificationManager.WIFI_CONNECTION, wifiName);

                // STEP 5 checks for FTP connection
                boolean connected = mConnectFTP.connect(address, user, pw, port);
                if(connected) {
                    Log.d(DEBUG_TAG, "Connected to FTP");
                    mNotifyManager.issueNotification(MyNotificationManager.FTP_CONNECTION, address);

                    ArrayList<FilePair> filePairs = new ArrayList<>();

                    // STEP 6 checks for files
                    for (int i=0; i < in_dir.length; i++) {

                        String inLocation = in_dir[i];

                        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + inLocation);
                        Log.d(DEBUG_TAG, "Checking for files in " + directory.getAbsolutePath());
                        // create if it doesn't already exist
                        if (!directory.exists()) {
                            boolean success = directory.mkdir();
                            Log.d(DEBUG_TAG, (success ? "success" : "failure") + " creating new directory... " + directory.getName());
                            return;
                        }

                        File[] filesArray = directory.listFiles();
                        if(filesArray == null) {
                            Log.wtf(DEBUG_TAG,"Please grant STORAGE permissions to service_ftp");
                            break;
                        }
                        Log.d(DEBUG_TAG, "Found " + filesArray.length + " files");

                        for (File f : filesArray) {
                            // don't add directories, just files...
                            if (f.isFile()) {
                                filePairs.add(new FilePair(f, out_dir[i]));
                            }
                        }

                    }


                    if (filePairs != null && filePairs.size() > 0) {
                        mNotifyManager.issueNotification(MyNotificationManager.FOUND_FILES, String.valueOf(filePairs.size()));
                        // STEP 7 begin transferring files
                        beginFileTransfer(filePairs);
                    }


                }


            } else {
                Log.d(DEBUG_TAG, "No connection");
                mNotifyManager.issueNotification(MyNotificationManager.NO_CONNECTION);
            }

        }
    }

    private void beginFileTransfer(List<FilePair> filePairs) {

        isUploading = true;

        FilePair fp = filePairs.remove(0);
        // STEP 8 call an "UploadTask"
        UploadTask asyncTask = new UploadTask(fp, filePairs);
        asyncTask.execute();

    }

    class UploadTask extends AsyncTask<Boolean, Integer, Boolean> {

        FilePair fp;
        List<FilePair> nextFilePairs;

        UploadTask(FilePair fp, List<FilePair> nextFilePairs) {this.fp = fp; this.nextFilePairs = nextFilePairs;}

        /**
         * STEP 9 run "doInBackground" method of UploadTask
         * @param booleans
         * @return
         */
        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            Log.w(DEBUG_TAG, "UploadTask.doInBackgrond...  writing " + fp.f.getName() + " to " + fp.out_dir);
            mNotifyManager.issueNotification(MyNotificationManager.UPLOADING, fp.f.getName());


            boolean uploaded = mConnectFTP.uploadFile(fp.f, fp.out_dir);


            return uploaded;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
           // Log.w(DEBUG_TAG, "UploadTask.onProgressUpdate: " + progress[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.w(DEBUG_TAG, "UploadTask.onPostExecute..." + result);

            if(result) {
                mNotifyManager.issueNotification(MyNotificationManager.DONE_UPLOADING, fp.f.getName());
                fp.f.delete();
            }

            if(nextFilePairs.isEmpty()) {
                isUploading = false;
            } else {
                FilePair nextFp = nextFilePairs.remove(0);
                UploadTask newUpload = new UploadTask(nextFp, nextFilePairs);
                newUpload.execute();
            }

        }
    }

    // checkForWifi
    // see archive/WifiService.[isConnectedViaWifi()|getWifiName()]

    /**
     * Checks whether Wifi is connected
     * https://stackoverflow.com/questions/5888502/how-to-detect-when-wifi-connection-has-been-established-in-android
     *
     * @return
     */
    private boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager == null) return false;
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    /**
     * gets wifi name if connected
     * https://stackoverflow.com/questions/3531940/how-to-get-name-of-wifi-network-out-of-android-using-android-api
     *
     * @return
     */
    private String getWifiName() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (manager == null) {
            return null;
        }
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if(wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if(state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.getSSID();
                }
            }
        }
        return null;
    }

    // uploadFiles
    // see archive/FTPService.UploadTask.doInBackground() {ftp_old.uploadFile}

    // deleteFiles
    // see archive/FTPService.UploadTask.doInBackground() {f.delete}

    // for all things ftp_old...
    // see library/easyFTP.java
    //// constructor            √
    //// setWorkingDirectory
    //// connect                √
    //// uploadFile

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
