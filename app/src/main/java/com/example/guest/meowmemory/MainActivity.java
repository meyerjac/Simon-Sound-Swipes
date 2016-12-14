package com.example.guest.meowmemory;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.example.guest.meowmemory.util.Android_Gesture_Detector;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private String TAG = MainActivity.class.getSimpleName();
    private ImageView mCat;
    private ImageView mWookie;
    private ImageView mDog;
    private ImageView mShaggy;
    private MediaPlayer cat;
    private MediaPlayer wookie;
    private MediaPlayer dog;
    private MediaPlayer shaggy;
    private MediaPlayer shake;
    private GestureDetector mGestureDetector;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 1000;
    private int numberOfShakes = 0;
    Random rand = new Random();

    private ArrayList<Integer> pattern = new ArrayList<>();
    private Timer timer;
    private Timer timerAfterAnswer;
    private int guess;
    private int patternIndex;
    private int i = 0;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCat = (ImageView) findViewById(R.id.cat);
        mWookie = (ImageView) findViewById(R.id.wookie);
        mDog = (ImageView) findViewById(R.id.dog);
        mShaggy= (ImageView) findViewById(R.id.shaggy);
        context = this;
        cat = MediaPlayer.create(this, R.raw.meow);
        dog = MediaPlayer.create(this, R.raw.bark);
        wookie = MediaPlayer.create(this, R.raw.wookie);
        cat = MediaPlayer.create(this, R.raw.meow);
        shake = MediaPlayer.create(this, R.raw.shake);
        shaggy = MediaPlayer.create(this, R.raw.zoinks);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);

        flash();

        Android_Gesture_Detector custom_gesture_detector = new Android_Gesture_Detector(){
            @Override
            public void onSwipeUp() {
                guess = 1;
                evaluateGuess();
                cat.start();
            }

            public void onSwipeDown() {
                guess = 3;
                evaluateGuess();
                dog.start();
            }

            public void onSwipeRight() {
                guess = 2;
                evaluateGuess();
                wookie.start();
            }

            public void onSwipeLeft() {
                guess = 4;
                evaluateGuess();
                shaggy.start();
            }
        };
        mGestureDetector = new GestureDetector(this,custom_gesture_detector);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
//                    guess = 0;
//                    evaluateGuess();
//                    onShake();

                    last_x = x;
                    last_y = y;
                    last_z = z;

                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
        // Return true if you have consumed the event, false if you haven't.
        // The default implementation always returns false.
    }

    public void onShake() {
        numberOfShakes++;
        Log.d(TAG, "onShake: " + numberOfShakes);
        shake.start();
    }

    public void evaluateGuess(){
        if (guess == pattern.get(patternIndex)) {
            patternIndex++;
        }
        else{
            Log.d(TAG, "You Lose!");
            patternIndex = 0;
            pattern.removeAll(pattern);
        }

        if (patternIndex == pattern.size()) {
            patternIndex = 0;
            timerAfterAnswer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    flash();
                    timerAfterAnswer.cancel();
                    timerAfterAnswer.purge();
                }
            };
            timerAfterAnswer.schedule(timerTask, 0, 1000);
        }
    }

    public void flash(){
        pattern.add(rand.nextInt(3) + 1);
        timer = new Timer();
        TimerTask timerTask = new TimerTask(){
            @Override
            public void run() {
                Log.d(TAG, "move is:" + pattern.get(i));
                playSound(pattern.get(i));
                i++;
                if (i == pattern.size()){
                    i = 0;
                    timer.cancel();
                    timer.purge();
                }
            }
        };

        timer.schedule(timerTask, 0, 1000);
    }

    private void playSound(int soundId){
        if (soundId == 0) {
            shake.start();
        }else if(soundId == 1){
            cat.start();
        }else if(soundId == 2){
            wookie.start();
        }else if(soundId == 3){
            dog.start();
        }else if(soundId == 4){
            shaggy.start();
        }
    }
}
