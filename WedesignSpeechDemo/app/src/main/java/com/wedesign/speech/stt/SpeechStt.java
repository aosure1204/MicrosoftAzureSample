package com.wedesign.speech.stt;

import android.util.Log;

import com.microsoft.cognitiveservices.speech.CancellationDetails;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;

import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.wedesign.speech.utils.SpeechUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SpeechStt {
    private static final String TAG = "SpeechStt";

    // create config
    private SpeechConfig speechConfig;

    public SpeechStt() {
        try {
            speechConfig = SpeechConfig.fromSubscription(SpeechUtils.SpeechSubscriptionKey, SpeechUtils.SpeechRegion);
        } catch (Exception e) {
            Log.e(TAG, "Exception error", e);
        }
    }

    public void getRecognizedTextFromMicrophone() {
        try {
            // final AudioConfig audioInput = AudioConfig.fromDefaultMicrophoneInput();
            final AudioConfig audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
            final SpeechRecognizer reco = new SpeechRecognizer(speechConfig, audioInput);

            final Future<SpeechRecognitionResult> task = reco.recognizeOnceAsync();
            setOnTaskCompletedListener(task, result -> {
                if (result.getReason() == ResultReason.RecognizedSpeech) {
                    mRecognizeListener.onSuccess(result.getText());
                    Log.d(TAG, "Recognizer returned: " + result.getText());
                } else {
                    String errorDetails = (result.getReason() == ResultReason.Canceled) ? CancellationDetails.fromResult(result).getErrorDetails() : "";
                    String s = "Recognition failed with " + result.getReason() + ". Did you enter your subscription?" + System.lineSeparator() + errorDetails;
                    Log.w(TAG, s);
                    mRecognizeListener.onFail();
                }

                reco.close();
            });
        } catch (Exception ex) {
            Log.e(TAG, "Exception error", ex);
        }
    }

    private static ExecutorService s_executorService;
    static {
        s_executorService = Executors.newCachedThreadPool();
    }

    private <T> void setOnTaskCompletedListener(Future<T> task, OnTaskCompletedListener<T> listener) {
        s_executorService.submit(() -> {
            T result = task.get();
            listener.onCompleted(result);
            return null;
        });
    }

    private interface OnTaskCompletedListener<T> {
        void onCompleted(T taskResult);
    }

    private MicrophoneStream microphoneStream;
    private MicrophoneStream createMicrophoneStream() {
        if (microphoneStream != null) {
            microphoneStream.close();
            microphoneStream = null;
        }

        microphoneStream = new MicrophoneStream();
        return microphoneStream;
    }

    private OnRecognizeListener mRecognizeListener;
    public void setOnRecognizeListener(OnRecognizeListener listener) {
        mRecognizeListener = listener;
    }
    public interface OnRecognizeListener {
        void onSuccess(String recognizerResult);
        void onFail();
    }
}
