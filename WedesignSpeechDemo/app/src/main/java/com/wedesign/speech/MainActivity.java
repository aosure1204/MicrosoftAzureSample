package com.wedesign.speech;

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wedesign.speech.luis.LuisUtils;
import com.wedesign.speech.stt.SpeechStt;
import com.wedesign.speech.tts.SpeechTts;

import com.microsoft.cognitiveservices.luis.clientlibrary.LUISClient;
import com.microsoft.cognitiveservices.luis.clientlibrary.LUISResponse;
import com.microsoft.cognitiveservices.luis.clientlibrary.LUISResponseHandler;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    // a unique number within the application to allow
    // correlating permission request responses with the request.
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 0;


    private ImageButton mBtnBack;
    private TextView mTextShowValue;
    private ImageButton mBtnStart;
    private ImageView mImgShowAnimate;

    // speech to text
    private SpeechStt mSpeechStt;

    // text to speech
    private SpeechTts mSpeechTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnBack = (ImageButton) findViewById(R.id.btn_back);
        mTextShowValue = (TextView) findViewById(R.id.text_show_value);
        mBtnStart = (ImageButton) findViewById(R.id.btn_start);
        mImgShowAnimate = (ImageView) findViewById(R.id.img_show_animate);
        mBtnBack.setOnClickListener(this);
        mBtnStart.setOnClickListener(this);

        // Request permissions needed for speech recognition
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "onCreate: checkSelfPermission result is denied, then requestPermissions");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
//            Log.d(TAG, "onCreate: checkSelfPermission result is granted, then initSTT");
            initSpeechStt();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        Log.d(TAG, "onRequestPermissionsResult: coming");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.d(TAG, "onRequestPermissionsResult: user granted permission");
                    initSpeechStt();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: user denied permission");
//                    Toast.makeText(this, R.string.need_record_audio_permissions, Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void initSpeechStt() {
        mSpeechStt = new SpeechStt();
        mSpeechStt.setOnRecognizeListener(new SpeechStt.OnRecognizeListener() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "step1 Speech To Text: result = " + result);
                cancleAnimate();
                setRecognizedResult(result);
                getIntentTextFromLUIS(result);
            }

            @Override
            public void onFail() {
                String hint = "Recognition failed.";
                Log.d(TAG, "step1 Speech To Text: result = " + hint);
                enableButtons();
                cancleAnimate();
                setRecognizedResult(hint);
                speechByTTS(hint);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                disableButtons();
                startAnimate();
                clearTexts();
                mSpeechStt.getRecognizedTextFromMicrophone();
                break;
            case R.id.btn_back:
                finish();
                break;
            default:
                break;
        }
    }

    private Timer mAnimateRepeat;

    private void startAnimate(){
        mAnimateRepeat = new Timer();
        TimerTask animateTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fadingAnimate();
                        Log.d(TAG, "TimerTask's Thread id: " + Thread.currentThread().getId());
                    }
                });
            }
        };
        mAnimateRepeat.schedule(animateTask, 0, 2000);
    }

    private void cancleAnimate(){
        mAnimateRepeat.cancel();
    }

    private void fadingAnimate() {
        mImgShowAnimate.animate().alpha(1f).setDuration(1000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mImgShowAnimate.animate().alpha(0f).setDuration(1000);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void getIntentTextFromLUIS(String utterance){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    LUISClient client = new LUISClient(LuisUtils.REGION, LuisUtils.APP_ID, LuisUtils.ENDPOINT_KEY, true);
                    client.predict(utterance, new LUISResponseHandler() {
                        @Override
                        public void onSuccess(LUISResponse response) {
                            String resultStr = "intent = " + response.getIntent() + ", entity = " + response.getEntity();
                            Log.d(TAG, "step2 LUIS result = " + resultStr);
                            processLUISResult(response);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "step2 LUIS Exception err :", e);
                            enableButtons();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "step2 LUIS Exception err :", e);
                }
            }
        });
    }

    private void processLUISResult(LUISResponse response) {
        MainActivity.this.runOnUiThread(()->{
            String resultStr;

            String luisIntent = response.getIntent();
            if(LUISResponse.NONE_INTENT.equals(luisIntent)) {
                resultStr = "LUIS doesn't understand your intent.";
            } else {
                resultStr = "LUIS doesn't understand your intent.";
                if(LuisUtils.INTENT_OPEN_APP.equals(luisIntent)) {
                    resultStr = openAppIfExist(response.getType());
                }
            }

            Log.d(TAG, "step3 Text To Speech: " + resultStr);
            setTTSSendText(resultStr);
            speechByTTS(resultStr);
            enableButtons();
        });
    }

    private String openAppIfExist(String type){
        String resultStr = "LUIS doesn't understand your intent.";
        final String[] appArray = LuisUtils.APP_ARRAY;
        for(int i = 0; i < appArray.length; i++) {
            if(appArray[i].equals(type)){
                switch (i) {
                    case 0:
                        boolean musicResult = startAppByPackageName("com.example.shengchuang.mytestmusic");
                        if(musicResult) {
                            resultStr = "Open the music application for you right now.";
                        } else {
                            resultStr = "The specific music application is not installed.";
                        }
                        break;
                    case 1:
                        boolean airResult = startAppByPackageName("com.android.airconditioner");
                        if(airResult) {
                            resultStr = "Open the air conditioning application for you right now.";
                        } else {
                            resultStr = "The specific air conditioning application is not installed.";
                        }
                        break;
                    case 2:
                        boolean naviResult = startAppByPackageName("com.autonavi.amapauto");
                        if(naviResult) {
                            resultStr = "Open the navigation application for you right now.";
                        } else {
                            resultStr = "The specific navigation application is not installed.";
                        }
                        break;
                }
                break;
            }
        }
        return resultStr;
    }

    private boolean startAppByPackageName(String packageName) {
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);

        if(intent==null){
            Toast.makeText(this, "未安装", Toast.LENGTH_LONG).show();
            return false;
        }else{
            startActivityDelay(intent);
            return true;
        }
    }

    private void startActivityDelay(Intent intent){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(intent);
            }
        }, 2000);
    }

    private void speechByTTS(String ttsText) {
        if(ttsText != null && !ttsText.isEmpty()) {
            mSpeechTts = new SpeechTts();
            mSpeechTts.speek(ttsText);
        }
    }

    private void disableButtons(){
        mBtnStart.setEnabled(false);
    }

    private void enableButtons() {
        MainActivity.this.runOnUiThread(() -> {
            mBtnStart.setEnabled(true);
        });
    }

    private void clearTexts() {
        mTextShowValue.setText("");
    }

    private void setRecognizedResult(String recognizedResult) {
        MainActivity.this.runOnUiThread(() -> {
            mTextShowValue.setText(recognizedResult);
        });
    }

    private void setTTSSendText(String ttsSendText) {
        mTextShowValue.setText(ttsSendText);
    }
}
