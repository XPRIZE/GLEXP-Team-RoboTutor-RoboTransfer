package com.adeel.easyftp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import cmu.xprize.service_ftp.*;

/**
 * easyFTP
 * <p>
 * Created by kevindeland on 2/15/18.
 */

public class MainActivity extends AppCompatActivity {

    LinearLayout SMainLayout;

    private static final String TAG = "MainActivity";

    String reallyLongFileOutput;

    // CONFIG
    private static final boolean DEBUG_NOTIFICATIONS = false;
    private static final boolean START_SERVICES = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_file_tester);

        SMainLayout = (LinearLayout) findViewById(R.id.SMainLayout);

        if(DEBUG_NOTIFICATIONS) {
            setupNotificationButtons();
        }

        if (START_SERVICES) {
            startServices();
        }
    }

    private void startServices() {

        startService(new Intent(this, WifiFTPBackgroundService.class));

        //startService(new Intent(this, FTPService.class));
        //startService(new Intent(this, LogFileWriterService.class));
    }


    /* Log file creation tester */
    public void createLocalLogFiles(View v) {

        writeLogFile("local", 3);
    }

    /* More log file creation tester */
    public void createRemoteLogFiles(View v) {
        writeLogFile("remote", 3);

    }

    private void writeLogFile(String folder, int count) {
        generateReallyLongString();

        String folderName;
        switch(folder) {

            case "remote":
                folderName = getResources().getString(R.string.remote_output);
                break;

            case "local":
            default:
                folderName = getResources().getString(R.string.local_output);
                break;

        }

        for (int i=0; i < count; i++) {
            try {

                String filename = "FAKE_" + System.currentTimeMillis() + ".txt";
                // create new directory if it doesn't exist
                String logDirectory = Environment.getExternalStorageDirectory() + File.separator + folderName;
                File directory = new File(logDirectory);
                if (!directory.exists()) {
                    boolean success = directory.mkdir();
                    Log.d("WRITE", (success ? "success" : "failure") + "creating new directory... " + directory.getName());
                }
                FileWriter logWriter = new FileWriter(logDirectory + File.separator + filename);

                logWriter.write(reallyLongFileOutput);
                logWriter.flush();
                logWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateReallyLongString() {

        reallyLongFileOutput = "";

        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for(int row = 0; row < 100; row++) {

            for (int col = 0; col < 60; col++ ) {
                reallyLongFileOutput += String.valueOf(charSet.charAt((int) (Math.random() * charSet.length())));

            }
            reallyLongFileOutput += "\n";
        }

    }


    /* Begin notification things */

    MyNotificationManager notifyManager;
    String[] notificationTypes = {"NO_SIGNAL", "WIFI", "FTP", "UPLOADING", "DONE_UPLOADING"};

    private void setupNotificationButtons() {

        notifyManager = new MyNotificationManager(this);

        /* set up a click listener to send a notification */
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                testNotification((String) view.getTag(R.string.notification_tag));
            }
        };

        for (String n: notificationTypes) {
            Button button = new Button(this);
            button.setText(n);
            button.setTag(R.string.notification_tag, n);
            button.setOnClickListener(listener);
            SMainLayout.addView(button);
        }
    }




    private void testNotification(String notification) {
        Log.d(TAG, "Testing notification " + notification);

        notifyManager.issueNotification(notification);
    }
}
