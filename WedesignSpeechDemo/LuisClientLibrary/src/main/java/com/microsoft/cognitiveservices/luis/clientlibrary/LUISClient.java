package com.microsoft.cognitiveservices.luis.clientlibrary;

import com.loopj.android.http.AsyncHttpClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class LUISClient {

    // Endpoint URL
    private String endpointURL = "https://%s.api.cognitive.microsoft.com/luis/v2.0/apps/%s?subscription-key=%s&q=%s&%s";


    private String region;
    private String appId;
    private String appKey;
    private boolean verbose;

    /**
     * Constructs a LUISClient with the corresponding user's App Id and Subscription Key
     *
     * @param appId   a String containing the Application Id
     * @param appKey  a String containing the Subscription Key
     * @param verbose a boolean to choose whether or not to use the verbose version
     * @throws IllegalArgumentException
     */
    public LUISClient(String region, String appId, String appKey, boolean verbose) {
        if (region == null)
            throw new IllegalArgumentException("NULL Region Id");
        if (region.isEmpty())
            throw new IllegalArgumentException("Empty Region Id");
        if (appId == null)
            throw new IllegalArgumentException("NULL Application Id");
        if (appId.isEmpty())
            throw new IllegalArgumentException("Empty Application Id");
        if (Pattern.compile("\\s").matcher(appId).find())
            throw new IllegalArgumentException("Invalid Application Id");
        if (appKey == null)
            throw new IllegalArgumentException("NULL Subscription Key");
        if (appKey.isEmpty())
            throw new IllegalArgumentException("Empty Subscription Key");
        if (Pattern.compile("\\s").matcher(appKey).find())
            throw new IllegalArgumentException("Invalid Subscription Key");

        this.region = region;
        this.appId = appId;
        this.appKey = appKey;
        this.verbose = verbose;
    }

    /**
     * Starts the prediction procedure for the user's text
     *
     * @param text            A String containing the text that needs to be analysed and predicted
     * @param responseHandler A responseHandler object af a class that can contains 2 functions
     *                        onSuccess and onFailure to be executed based on the success or the failure of the prediction
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public void predict(String text, final LUISResponseHandler responseHandler) throws IOException {
        if (text == null)
            throw new IllegalArgumentException("NULL text to predict");
        text = text.trim();
        if (text.isEmpty())
            throw new IllegalArgumentException("Empty text to predict");
        if (responseHandler == null)
            throw new IllegalArgumentException("Null response handler");

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(predictURLGen(text), new LUISJsonHttpResponseHandler(responseHandler));
    }

    /**
     * Generates the url for the predict web request
     *
     * @param text A String containing the text that needs to be analysed and predicted
     * @return A String containing the url for the predict web request
     * @throws IOException
     */
    public String predictURLGen(String text) throws IOException {
        String encodedQuery;
        encodedQuery = URLEncoder.encode(text, "UTF-8");
        String url = String.format(endpointURL, region, appId, appKey, encodedQuery, verbose);
        return url;
    }
}
