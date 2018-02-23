package cmu.xprize.service_ftp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

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
    private FTP_CONST.FtpConfigProfile mFtpProfile;

    // if it's already running, don't start another service
    private boolean isUploading = false;

    private static final String TAG = "WifiFTPService";

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

        mNotifyManager = new MyNotificationManager(this);
        mConnectFTP = new ConnectFTP();

        // Check the build config to determine which FTP we should connect to
        if (BuildConfig.FTP_CONFIG.equals("KEVIN_LOCAL_FTP")) {
            mFtpProfile = FTP_CONST.KEVIN_LOCAL_FTP;

        } else if (BuildConfig.FTP_CONFIG.equals("XPRIZE_FIELD_FTP")) {
            mFtpProfile = FTP_CONST.XPRIZE_FIELD_FTP;
        }


        // STEP 2 a new "CheckConnectionsTask" is run periodically
        timer.scheduleAtFixedRate(new CheckConnectionsTask(), FTP_CONST.BACKGROUND_CHECK_DELAY, FTP_CONST.BACKGROUND_CHECK_PERIOD);


        return super.onStartCommand(intent, flags, startId);
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

            Log.d(TAG, "Running CheckConnectionsTask");

            // STEP 4 checks for wifi connection
            if (isConnectedViaWifi()) {
                Log.d(TAG, "Connected via Wifi");
                String wifiName = getWifiName();
                mNotifyManager.issueNotification(MyNotificationManager.WIFI_CONNECTION, wifiName);

                // STEP 5 checks for FTP connection
                boolean connected = mConnectFTP.connect(mFtpProfile.address, mFtpProfile.user, mFtpProfile.pw, mFtpProfile.port);
                if(connected) {
                    Log.d(TAG, "Connected to FTP");
                    mNotifyManager.issueNotification(MyNotificationManager.FTP_CONNECTION, mFtpProfile.address);

                    // STEP 6 checks for files
                    // TODO could still be null???
                    String logLocation = mFtpProfile.folderPairs.get(0).source;
                    File directory = new File(Environment.getExternalStorageDirectory() + File.separator + logLocation);
                    // create if it doesn't already exist
                    if (!directory.exists()) {
                        boolean success = directory.mkdir();
                        Log.d(TAG, (success ? "success" : "failure") + " creating new directory... " + directory.getName());
                        return;
                    }
                    File[] filesArray = directory.listFiles();
                    if(filesArray == null || filesArray.length ==0) {
                        return;
                    }

                    ArrayList<File> files = new ArrayList<>(filesArray.length);

                    for (File f : filesArray) {
                        files.add(f);
                    }


                    if (files != null && files.size() > 0) {
                        mNotifyManager.issueNotification(MyNotificationManager.FOUND_FILES, String.valueOf(files.size()));
                        // STEP 7 begin transferring files
                        beginFileTransfer(files);
                    }


                }


            } else {
                Log.d(TAG, "No connection");
                mNotifyManager.issueNotification(MyNotificationManager.NO_CONNECTION);
            }

        }
    }

    private void beginFileTransfer(List<File> files) {

        isUploading = true;

        File f = files.remove(0);
        // STEP 8 call an "UploadTask"
        UploadTask asyncTask = new UploadTask(f, files);
        asyncTask.execute();

    }

    class UploadTask extends AsyncTask<Boolean, Integer, Boolean> {

        File f;
        List<File> nextFiles;

        UploadTask(File f, List<File> nextFiles) {this.f = f; this.nextFiles = nextFiles;}

        /**
         * STEP 9 run "doInBackground" method of UploadTask
         * @param booleans
         * @return
         */
        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            Log.w(TAG, "UploadTask.doInBackgrond... " + f.getName());
            mNotifyManager.issueNotification(MyNotificationManager.UPLOADING, f.getName());


            boolean uploaded = mConnectFTP.uploadFile(f, mFtpProfile.folderPairs.get(0).dest);


            return uploaded;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
           // Log.w(TAG, "UploadTask.onProgressUpdate: " + progress[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.w(TAG, "UploadTask.onPostExecute..." + result);

            if(result) {
                mNotifyManager.issueNotification(MyNotificationManager.DONE_UPLOADING, f.getName());
                f.delete();
            }

            if(nextFiles.isEmpty()) {
                isUploading = false;
            } else {
                File nextF = nextFiles.remove(0);
                UploadTask newUpload = new UploadTask(nextF, nextFiles);
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
