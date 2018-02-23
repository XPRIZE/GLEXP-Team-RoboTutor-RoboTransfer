package cmu.xprize.service_ftp;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;
import android.util.Log;



/**
 * MyNotificationManager
 * <p>
 * Created by kevindeland on 2/15/18.
 */

public class MyNotificationManager  {

    private NotificationManager manager;
    private android.support.v4.app.NotificationCompat.Builder mBuilder;


    static final String NO_CONNECTION = "NO_SIGNAL";
    static final String WIFI_CONNECTION = "WIFI";
    static final String FTP_CONNECTION = "FTP";
    static final String FOUND_FILES = "FOUND_FILES";
    static final String UPLOADING = "UPLOADING";
    static final String DONE_UPLOADING = "DONE_UPLOADING";


    private static final String TAG = "MyNotificationManager";

    private class MyNotification {

        int icon;
        String title;
        String text;

        public MyNotification(int icon, String title, String text) {
            this.icon = icon;
            this.title = title;
            this.text = text;
        }

    }

    // NEXT... make it accept a file argument?
    public MyNotificationManager(Context context) {

        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(context);

    }

    /**
     * Issue a notification of the corresponding notification type
     *
     * @param notificationType
     */
    public void issueNotification(String notificationType) {
        issueNotification(notificationType, null);
    }

    /**
     * This notification issuer accepts an extra string.
     *
     * @param notificationType
     * @param extra
     */
    public void issueNotification(String notificationType, String extra) {

        Log.d(TAG, "Issuing notification " + notificationType);

        switch(notificationType) {
            case NO_CONNECTION:

                mBuilder.setSmallIcon(R.drawable.no_connection)
                        .setContentTitle("No network connected!")
                        .setContentText("Wifi connection lost");

                manager.notify(1, mBuilder.build());
                manager.cancel(2);
                manager.cancel(3);

                break;

            case WIFI_CONNECTION:


                // wifi connection
                mBuilder.setSmallIcon(R.drawable.wifi)
                        .setContentTitle("Wifi connected!")
                        .setContentText(String.format("Connected to wifi network %s", extra));

                manager.cancel(1);
                manager.notify(2, mBuilder.build());
                manager.cancel(3);

                break;

            case FTP_CONNECTION:

                // ftp_old connection
                mBuilder.setSmallIcon(R.drawable.ftp)
                        .setContentTitle("FTP connected!")
                        .setContentText(String.format("Connected to FTP server %s", extra));

                manager.cancel(1);
                manager.cancel(2);
                manager.notify(3, mBuilder.build());

                break;

            case FOUND_FILES:

                // files found
                mBuilder.setSmallIcon(R.drawable.folder_open)
                        .setContentTitle(String.format("Found %s log files", extra))
                        .setContentText(String.format("Found %s log files", extra));

                manager.cancel(1);
                manager.cancel(2);
                manager.cancel(3);
                manager.notify(4, mBuilder.build());

            case UPLOADING:

                // file upload
                mBuilder.setSmallIcon(R.drawable.cloud_upload)
                        .setContentTitle(String.format("Uploading file %s", extra))
                        .setContentText(String.format("Uploading file %s", extra));
                manager.notify(extra.hashCode(), mBuilder.build());

                break;

            case DONE_UPLOADING:

                // done uploading
                mBuilder.setSmallIcon(R.drawable.cloud_done)
                        .setContentTitle(String.format("Done uploading file %s", extra))
                        .setContentText(String.format("Done uploading file %s", extra));
                manager.notify(extra.hashCode(), mBuilder.build());

        }
    }

}
