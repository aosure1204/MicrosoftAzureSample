package com.isure.luissample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.cognitiveservices.luis.clientlibrary.LUISClient;
import com.microsoft.cognitiveservices.luis.clientlibrary.LUISResponse;
import com.microsoft.cognitiveservices.luis.clientlibrary.LUISResponseHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";

    //
    // Set or get variables needed for HTTP call
    //

    // region
    private static final String REGION = "westus";

    // LUIS App ID:
    //公共应用 ID：HomeAutomation app ID
    private static final String APP_ID = "a6415108-f7ca-4ac2-aba6-526f9c157115";

    // LUIS Endpoint Key:(You can use the authoring key instead of the endpoint key.
    //The authoring key allows 1000 endpoint queries a month.)
    private static final String ENDPOINT_KEY = "c236b1ae109b45ca859daad4b44b1387";


    // LUIS utterance:(User text to analyze)
    //
    private static final String UTTERANCE = "有没有好地方找到好健康餐厅附近";


    Button mBtn;
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textview);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:
                try {
                    LUISClient client = new LUISClient(REGION, APP_ID, ENDPOINT_KEY, true);
                    client.predict(UTTERANCE, new LUISResponseHandler() {
                        @Override
                        public void onSuccess(LUISResponse response) {
                            processResponse(response);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Exception err", e);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Exception err", e);
                }
                break;
                default:

                    break;
        }
    }

    private void processResponse(LUISResponse response) {
        String resultStr = "intent = " + response.getIntent() + ", entity = " + response.getEntity();
        mTextView.setText(resultStr);
    }

}
