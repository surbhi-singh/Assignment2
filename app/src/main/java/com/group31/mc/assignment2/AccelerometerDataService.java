package com.group31.mc.assignment2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;


public class AccelerometerDataService extends Service implements SensorEventListener {
    DatabaseManager db;

    Sensor senAccelerometer;
    SensorManager senSensorManager;
    long lastUpdate;
    float last_x, last_y, last_z;


    public AccelerometerDataService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //initializing sensor variable
        db = new DatabaseManager(this);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this,senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) >= 1000) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                db.insertIntoTable(x, y, z);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

}
