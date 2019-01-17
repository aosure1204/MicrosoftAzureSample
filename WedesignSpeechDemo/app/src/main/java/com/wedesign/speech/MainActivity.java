package com.wedesign.speech;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wedesign.speech.luis.LuisUtils;
import com.wedesign.speech.stt.SpeechStt;
import com.wedesign.speech.tts.SpeechTts;

import com.microsoft.cognitiveservices.luis.clientlibrary.LUISClient;
import com.microsoft.cognitiveservices.luis.clientlibrary.LUISResponse;
import com.microsoft.cognitiveservices.luis.clientlibrary.LUISResponseHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    // a unique number within the application to allow
    // correlating permission request responses with the request.
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 0;

    private TextView mRecognizeResult;
    private TextView mLuisResult;
    private TextView mLocalResult;
    private Button mBtnStart;

    // speech to text
    private SpeechStt mSpeechStt;

    // text to speech
    private SpeechTts mSpeechTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecognizeResult = (TextView) findViewById(R.id.recognize_result);
        mLuisResult = (TextView) findViewById(R.id.luis_result);
        mLocalResult = (TextView) findViewById(R.id.local_result);
        mBtnStart = (Button) findViewById(R.id.btn_start);
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
                Log.d(TAG, "onClick: result = " + result);
                setRecognizedResult(result);
                getIntentTextFromLUIS(result);
            }

            @Override
            public void onFail() {
                setRecognizedResult("Recognition failed.");
                enableButtons();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                disableButtons();
                clearTexts();
                mSpeechStt.getRecognizedTextFromMicrophone();
                break;
            default:
                break;
        }
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
                            setLUISResult(resultStr);
                            processLUISResult(response);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            setLUISResult("LUIS error");
                            Log.e(TAG, "Exception err", e);
                            enableButtons();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Exception err", e);
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

            setTTSSendText(resultStr);
            speechLUISResultFromTTS(resultStr);
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
                            resultStr = "Starting the music application for you";
                        } else {
                            resultStr = "The specific music application is not installed";
                        }
                        break;
                    case 1:
                        boolean airResult = startAppByPackageName("com.android.airconditioner");
                        if(airResult) {
                            resultStr = "Starting the air conditioning application for you";
                        } else {
                            resultStr = "The specific air conditioning application is not installed";
                        }
                        break;
                    case 2:
                        boolean naviResult = startAppByPackageName("com.autonavi.amapauto");
                        if(naviResult) {
                            resultStr = "Starting the navigation application for you";
                        } else {
                            resultStr = "The specific navigation application is not installed";
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
            startActivity(intent);
            return true;
        }

    }

    private void speechLUISResultFromTTS(String ttsText) {
        if(ttsText != null && !ttsText.isEmpty()) {
            mSpeechTts = new SpeechTts();
            mSpeechTts.speek(ttsText);
        }
    }

    private void disableButtons(){
        mBtnStart.setEnabled(false);
    }

    private void clearTexts() {
        mRecognizeResult.setText("");
        mLuisResult.setText("");
        mLocalResult.setText("");
    }

    private void enableButtons() {
        MainActivity.this.runOnUiThread(() -> {
            mBtnStart.setEnabled(true);
        });
    }

    private void setRecognizedResult(String recognizedResult) {
        MainActivity.this.runOnUiThread(() -> {
            mRecognizeResult.setText(recognizedResult);
        });
    }

    private void setLUISResult(String luisResult) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLuisResult.setText(luisResult);
            }
        });
    }

    private void setTTSSendText(String ttsSendText) {
        mLocalResult.setText(ttsSendText);
    }
}
