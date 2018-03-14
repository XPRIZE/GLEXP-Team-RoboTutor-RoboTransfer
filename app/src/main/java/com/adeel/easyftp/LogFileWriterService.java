package com.adeel.easyftp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import cmu.xprize.service_ftp.*;

public class LogFileWriterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor senAccel;

    private String logLocation;

    FileWriter logWriter;

    public static final String FOLDER_NAME = "ftp_transfer"; // TODO how can we make this configgable?
    private static final String LOG_FILE_LOCATION = Environment.getExternalStorageDirectory().getPath() + File.separator + FOLDER_NAME + File.separator;

    private static final String TAG = "LogFileWriterService";

    private Vector<float[]> runningXYZ;
    private int MAX_ONE_LOG_FILE = 200;


    public LogFileWriterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        Log.d(TAG, "will write to " + LOG_FILE_LOCATION);

        // register sensor things
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccel, SensorManager.SENSOR_DELAY_NORMAL);

        String logOutput = getResources().getString(R.string.accel_output);
        logLocation = Environment.getExternalStorageDirectory() + File.separator + logOutput;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.v(TAG, "onSensorChanged");

        Sensor mySensor = sensorEvent.sensor;

        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] xyz = {sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]};


            if(runningXYZ == null) {
                runningXYZ = new Vector<>();
            }

            runningXYZ.add(xyz);

            if(runningXYZ.size() >= MAX_ONE_LOG_FILE) {
                Log.d("WRITE", "beginning to write");
                logData(logLocation, "ACCEL_" + System.currentTimeMillis() + ".txt");

                runningXYZ = null;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // do nothing
    }


    private void logData(String path, String filename) {
        File parentDirectory = new File(path);
        if (!parentDirectory.exists()) {
            Log.d("WRITE", "creating new directory... " + path);
            parentDirectory.mkdir();
        }
        Log.d("WRITE", "writing output to " + filename);
        try {
            logWriter = new FileWriter(path + File.separator + filename, true);
            writeToLog(runningXYZ);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeToLog(Vector<float[]> xyzs) {

        try {

            for(float[] xyz : xyzs ) {
                logWriter.write("" + xyz[0] + ',' + xyz[1] + ',' + xyz[2] + "\n");
            }

            logWriter.flush();
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
