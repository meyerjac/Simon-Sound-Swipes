package com.example.guest.meowmemory;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.guest.meowmemory.ui.RulesActivity;
import com.example.guest.meowmemory.util.Android_Gesture_Detector;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    private String TAG = MainActivity.class.getSimpleName();
    private Button mNewGame;
    private Button mRules;
    private ImageView mRedLight;
    private ImageView mGreenLight;
    private MediaPlayer cat;
    private MediaPlayer wookie;
    private MediaPlayer dog;
    private MediaPlayer sponge;
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
    private boolean simonSpeaking = true;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNewGame = (Button) findViewById(R.id.newGame);
        mGreenLight = (ImageView) findViewById(R.id.green);
        mRedLight = (ImageView) findViewById(R.id.red);
        mRules = (Button) findViewById(R.id.rules);

        cat = MediaPlayer.create(this, R.raw.meow);
        dog = MediaPlayer.create(this, R.raw.bark);
        wookie = MediaPlayer.create(this, R.raw.chewy);
        shake = MediaPlayer.create(this, R.raw.shake);
        sponge = MediaPlayer.create(this, R.raw.sponge);

//        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mSensorManager.registerListener(this, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);

        context = this;
        mNewGame.setOnClickListener(this);
        mRules.setOnClickListener(this);

        Android_Gesture_Detector custom_gesture_detector = new Android_Gesture_Detector() {
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
                sponge.start();
            }
        };
        mGestureDetector = new GestureDetector(this, custom_gesture_detector);

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
                    guess = 0;
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
        if (!simonSpeaking) {
            mGestureDetector.onTouchEvent(event);
            return super.onTouchEvent(event);
        } else {
            return false;
        }
    }

    public void onShake() {
        numberOfShakes++;
        Log.d(TAG, "onShake: " + numberOfShakes);
        shake.start();
    }

    public void evaluateGuess() {
        if (guess == pattern.get(patternIndex)) {
            patternIndex++;
        } else {
            Log.d(TAG, "You Lose!");
            patternIndex = 0;
            timer.cancel();
            timer.purge();

            pattern.removeAll(pattern);
        }

        if (patternIndex == pattern.size()) {
            patternIndex = 0;
            timerAfterAnswer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    startSequence();
                    timerAfterAnswer.cancel();
                    timerAfterAnswer.purge();
                }
            };
            timerAfterAnswer.schedule(timerTask, 1000, 1);
        }
    }

    public void startSequence() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRedLight.setVisibility(View.VISIBLE);
                mGreenLight.setVisibility(View.INVISIBLE);
            }
        });
        pattern.add(rand.nextInt(4) + 1);
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "move is:" + pattern.get(i));
                playSound(pattern.get(i));
                i++;
                if (i == pattern.size()) {
                    i = 0;
                    simonSpeaking = false;
                    resetLight();
                    timer.cancel();
                    timer.purge();
                }
            }
        };

        timer.schedule(timerTask, 1000, 1200);
        simonSpeaking = true;
    }

    private void playSound(int soundId) {
        if (soundId == 0) {
            shake.start();
        } else if (soundId == 1) {
            cat.start();
        } else if (soundId == 2) {
            wookie.start();
        } else if (soundId == 3) {
            dog.start();
        } else if (soundId == 4) {
            sponge.start();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mNewGame) {
            Log.d(TAG, "onClick: ");
            startSequence();
        } if (view == mRules) {
            Intent intent = new Intent(MainActivity.this, RulesActivity.class);
            startActivity(intent);
        }
    }

    private void resetLight(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGreenLight.setVisibility(View.VISIBLE);
                mRedLight.setVisibility(View.INVISIBLE);
            }
        });
    }
}
