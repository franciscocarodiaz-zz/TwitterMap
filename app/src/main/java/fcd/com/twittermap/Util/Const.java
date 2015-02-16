package fcd.com.twittermap.Util;


import fcd.com.twittermap.data.TwitterMapContract.TwitterEntry;

public class Const {

    public static final int TIME_TO_RELOAD = 15;
    public static final int TWITTER_SIZE_LIMIT = 10;
    public static final int MODE_PRIVATE = 0;

    // HTTP Request type in ServiceHandler
    public static final int CONNECTION_TYPE_HTTP_GET = 0;
    public static final int CONNECTION_TYPE_HTTP_POST = 1;
    public static final int CONNECTION_TYPE_HTTP_PROXY = 2;
    public static final int CONNECTION_TYPE_ASYNCTIMELINE = 3;


    // Pending Request Code
    public static final int RESPONSE_RESULT_CODE = 0;
    public static final int RESPONSE_INVALID_REQUEST = 1;
    public static final int RESPONSE_ERROR_CODE = 2;
    public static final int REQUEST_SERVICE_CODE = 0;

    public static final String PENDING_RESULT_EXTRA = "pending_result";
    public static final String RESPONSE_URL_EXTRA = "url";
    public static final String RESPONSE_RESULT_EXTRA = "url";

    public static final String CONSUMER_KEY = "XXXXXX";
    public static final String CONSUMER_SECRET = "XXXXXX";

    public final static String TwitterTokenURL = "https://api.twitter.com/oauth2/token";
    public final static String TwitterSearchURL = "https://api.twitter.com/1.1/search/tweets.json?count=300&q=";
    //public final static String TwitterStreamURL = "https://api.twitter.com/1.1/statuses/user_timeline.json?exclude_replies=true&count=10&screen_name=";
    public final static String TwitterStreamURL = "https://stream.twitter.com/1.1/statuses/filter.json";

    // Share Pref Value Type
    public static final int TYPE_SharePrefValue_STR = 0;
    public static final int TYPE_SharePrefValue_INT = 1;
    public static final int TYPE_SharePrefValue_BOOL = 2;

    public static String PREFERENCE_NAME = "twitter_oauth";
    public static final String PREF_KEY_SECRET = "oauth_token_secret";
    public static final String PREF_KEY_TOKEN = "oauth_token";
    public static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    public static final String PREF_QUERY_SEARCH = "query_search";

    public static final String  OAUTH_CALLBACK_SCHEME = "x-oauthflow-twitter";
    public static final String  OAUTH_CALLBACK_HOST = "twittermap";
    public static final String  CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
    //public static final String CALLBACK_URL = "https://github.com/";

    public static final String IEXTRA_AUTH_URL = "auth_url";
    public static final String IEXTRA_OAUTH_VERIFIER = "oauth_verifier";
    public static final String IEXTRA_OAUTH_TOKEN = "oauth_token";

    // TwitterEntry Obj
    public static final String TWITTER_ENTRY_COLUMN_CREATED_AT = "twitter_created_at";
    public static final String TWITTER_ENTRY_COLUMN_ID = "twitter_id";
    public static final String TWITTER_ENTRY_COLUMN_TITLE = "twitter_title";
    public static final String TWITTER_ENTRY_COLUMN_SUBTITLE = "twitter_subtitle";
    public static final String TWITTER_ENTRY_COLUMN_ICON = "twitter_icon";
    public static final String TWITTER_ENTRY_COLUMN_LAT = "twitter_lat";
    public static final String TWITTER_ENTRY_COLUMN_LON = "twitter_lon";

    public static final String[] TWITTER_ENTRY_COLUMNS = {
            TwitterEntry.TABLE_NAME + "." + TwitterEntry._ID,
            TwitterEntry.COLUMN_CREATED_AT,
            TwitterEntry.COLUMN_TITLE,
            TwitterEntry.COLUMN_SUBTITLE,
            TwitterEntry.COLUMN_ICON,
            TwitterEntry.COLUMN_LAT,
            TwitterEntry.COLUMN_LON
    };
}
