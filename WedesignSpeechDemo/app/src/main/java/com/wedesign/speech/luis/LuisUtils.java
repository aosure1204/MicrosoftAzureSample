package com.wedesign.speech.luis;

public class LuisUtils {

    //
    // Set or get variables needed for HTTP call
    //

    // region
    public static final String REGION = "westus";

    // LUIS App ID:
    //公共应用 ID：HomeAutomation app ID
//    public static final String APP_ID = "a6415108-f7ca-4ac2-aba6-526f9c157115";
    public static final String APP_ID = "5fb8ed9f-235b-4674-bfd7-89f3db19c52b";

    // LUIS Endpoint Key:(You can use the authoring key instead of the endpoint key.
    //The authoring key allows 1000 endpoint queries a month.)
//    public static final String ENDPOINT_KEY = "c236b1ae109b45ca859daad4b44b1387";
    public static final String ENDPOINT_KEY = "c236b1ae109b45ca859daad4b44b1387";

    public static final String INTENT_OPEN_APP = "OPEN_APP";
    public static final String[] APP_ARRAY = {"MUSIC_APP", "AC_APP", "NAVI_APP"};
}
