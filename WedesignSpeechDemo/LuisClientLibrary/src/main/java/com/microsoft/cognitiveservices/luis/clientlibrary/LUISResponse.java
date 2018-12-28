package com.microsoft.cognitiveservices.luis.clientlibrary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes the response structure, and is the main point
 * to access the response sent by LUIS after prediction
 */
public class LUISResponse {
    public static final String NONE_INTENT = "None";

    private String intent;
    private String entity;

    public LUISResponse(JSONObject JSONresponse) {
        if (JSONresponse == null)
            throw new NullPointerException("NULL JSON response");

        JSONObject topScoringIntentObject = JSONresponse.optJSONObject("topScoringIntent");
        intent = topScoringIntentObject.optString("intent");
        if(NONE_INTENT.equals(intent)){
            return;
        }

        JSONArray entities = JSONresponse.optJSONArray("entities");
        for(int i = 0; i < entities.length(); i++) {
            JSONObject entityObject = entities.optJSONObject(i);
            String type = entityObject.optString("type");
            if(!("KEYWORD".equals(type))) {
                return;
            }
            entity = entityObject.optString("entity");
        }
    }


    public String getIntent() {
        return intent;
    }

    public String getEntity() {
        return entity;
    }

}