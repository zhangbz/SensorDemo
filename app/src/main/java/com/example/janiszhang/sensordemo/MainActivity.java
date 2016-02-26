package com.example.janiszhang.sensordemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView lightLevel;
    private SensorManager sensorManager;
    private ImageView compassImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lightLevel = (TextView) findViewById(R.id.light_level);
        compassImg = (ImageView) findViewById(R.id.compass_img);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(lightListener, lightSensor,SensorManager.SENSOR_DELAY_NORMAL);

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        sensorManager.registerListener(accelerometerListener,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        Sensor magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(listener ,magneticFieldSensor,SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sensorManager != null) {
            sensorManager.unregisterListener(lightListener);
//            sensorManager.unregisterListener(accelerometerListener);
            sensorManager.unregisterListener(listener);
        }
    }

    //光照传感器listener
    private SensorEventListener lightListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            //Values数组中第一个下标的值就是当前的光照强度
            float value = event.values[0];
            lightLevel.setText("Current light level is " + value + " lx");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    //加速度传感器listener
    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            //加速度可能会是负值,所以要取它们的绝对值
            float xValue = Math.abs(event.values[0]);
            float yValue = Math.abs(event.values[1]);
            float zValue = Math.abs(event.values[2]);

            if(xValue > 15 || yValue > 15 || zValue > 15) {
                //认为用户摇动了手机,触发了摇一摇逻辑
                Toast.makeText(MainActivity.this, "摇一摇", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    //加速度和地磁listener
    private SensorEventListener listener = new SensorEventListener() {

        float[] accelerometerValues = new float[3];
        float[] magneticValues = new float[3];

        private float lastRotateDegree;

        @Override
        public void onSensorChanged(SensorEvent event) {
            //判断当前是加速度传感器还是地磁传感器
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //注意赋值时要调用clone()方法
                accelerometerValues = event.values.clone();
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticValues = event.values.clone();
            }
            float[] R = new float[9];
            float[] values = new float[3];

            //get R 一个长度为9的float数组,包换旋转矩阵的数组
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
            //get values 长度为3,zxy的旋转弧度
            SensorManager.getOrientation(R, values);
//            Log.d("Mainactivity", "value[0] is " + Math.toDegrees(values[0]));//转换成角度
            //降级算出的旋转角度取反,用于旋转指南针背景图
            float rotateDegree = - (float) Math.toDegrees(values[0]);
            if(Math.abs(rotateDegree - lastRotateDegree ) > 1) {
                RotateAnimation animation = new RotateAnimation(lastRotateDegree,rotateDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);//0.5f中的f不能落下
                animation.setFillAfter(true);
                compassImg.startAnimation(animation);
                lastRotateDegree = rotateDegree;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
