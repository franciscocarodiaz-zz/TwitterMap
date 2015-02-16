package fcd.com.twittermap;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import fcd.com.twittermap.Service.FetchTweetTask;
import fcd.com.twittermap.Service.TwitterMapService;
import fcd.com.twittermap.Twitter.Search;
import fcd.com.twittermap.Twitter.Searches;
import fcd.com.twittermap.Util.Const;
import fcd.com.twittermap.Util.UserSingleton;
import fcd.com.twittermap.Util.Utility;
import fcd.com.twittermap.data.TwitterMapContract;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends ActionBarActivity implements android.support.v7.widget.SearchView.OnQueryTextListener,FetchTweetTask.Callback, LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private GoogleMap mMap;
    private Searches twits;
    private int mCallFetchTweetTask;
    String mSearch;
    FetchTweetTask mFetchTweetTask;
    Utility mUtility;
    private static final int TWITTER_LOADER = 0;

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;

    private Context mContext;
    private static TwitterFactory mFactory;
    private static Twitter mTwitter;
    private static RequestToken mRequestToken;
    private static AccessToken mAccessToken;

    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
        twits = new Searches();

        mCallFetchTweetTask = 0;
        mContext = this;
        mSharedPreferences = mContext.getSharedPreferences(Const.PREFERENCE_NAME, Const.MODE_PRIVATE);
        mUtility = new Utility(mContext);

        boolean isTwitterLoggedInAlready = isTwitterLoggedInAlready();
        boolean isConnectedTwitter = isConnectedTwitter();

        Log.e(LOG_TAG, "isTwitterLoggedInAlready > " + isTwitterLoggedInAlready);
        Log.e(LOG_TAG, "isConnectedTwitter > " + isConnectedTwitter);
        if (!isTwitterLoggedInAlready) {
            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith(Const.CALLBACK_URL)) {
                String verifier = uri
                        .getQueryParameter(Const.IEXTRA_OAUTH_VERIFIER);

                try {

                    AsyncAccessToken asyncAccessToken = new AsyncAccessToken();
                    asyncAccessToken.execute(verifier);

                } catch (Exception e) {
                    Log.e(LOG_TAG, "Twitter Login Error > " + e.getMessage());
                }
            }
        }


        getSupportLoaderManager().initLoader(TWITTER_LOADER, null, this);

    }

    private void displayTweets(){
        HashMap<String, ArrayList<MarkerOptions>> mMarkerPoints = UserSingleton.getInstance().getArrayMarkerPoints();
        HashMap<String, ArrayList<MarkerOptions>> auxMarkerPoints = UserSingleton.getInstance().getArrayMarkerPoints();
        if(auxMarkerPoints.size()>0){
            Date currentDate = new Date();
            Calendar calendarCurrentTime = Calendar.getInstance();
            calendarCurrentTime.setTime(currentDate);

            for (Map.Entry<String, ArrayList<MarkerOptions>> e : auxMarkerPoints.entrySet())
            {
                if(((ArrayList<MarkerOptions>)e.getValue())!=null){
                    Date tweetTime = TwitterMapContract.getDateFromDb((String) e.getKey());
                    Calendar calendarTweetTime = Calendar.getInstance();
                    calendarTweetTime.setTime(tweetTime);
                    long fechadif= calendarCurrentTime.getTimeInMillis() - calendarTweetTime.getTimeInMillis();
                    long diffSeconds = fechadif / 1000;

                    Log.v(LOG_TAG+":displayTweets", "calendarCurrentTime.getTimeInMillis(): " + calendarCurrentTime.getTimeInMillis());
                    Log.v(LOG_TAG+":displayTweets", "calendarTweetTime.getTimeInMillis(): " + calendarTweetTime.getTimeInMillis());
                    Log.v(LOG_TAG+":displayTweets", "calendarCurrentTime.getTimeInMillis() - tweetTime > diffSeconds: " + diffSeconds);
                    Log.v(LOG_TAG+":displayTweets", "diffSeconds >= Const.TIME_TO_RELOAD >>> " + (diffSeconds >= Const.TIME_TO_RELOAD));

                    if(diffSeconds >= Const.TIME_TO_RELOAD){
                        Log.v(LOG_TAG+":displayTweets", "Removing " + ((ArrayList<MarkerOptions>)e.getValue()).size() + " points in map with time: " + tweetTime);
                        mMarkerPoints.put((String) e.getKey(),null);
                    }
                }
            }
            displayTweetsOnMap(mMarkerPoints);
        }
    }

    private void displayTweetsOnMap(HashMap<String, ArrayList<MarkerOptions>> auxMarkerPoints){
        mMap.clear();
        Log.d(LOG_TAG, "Map restart.");

        double mLatitude=0,mLongitude=0;
        for (ArrayList<MarkerOptions> marketOptionsArray : auxMarkerPoints.values()) {
            if(marketOptionsArray!=null){
                for (MarkerOptions item : marketOptionsArray) {
                    mMap.addMarker(item);
                    mLatitude+=item.getPosition().latitude;
                    mLongitude+=item.getPosition().longitude;
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            new LatLng(item.getPosition().latitude, item.getPosition().longitude)).zoom(0).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }

        }
        if(mLatitude>0){
            // Moving Camera to a Location with animation
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(mLatitude, mLongitude)).zoom(0).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        UserSingleton.getInstance().setArrayMarkerPoints(auxMarkerPoints);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Vbles para el buscador
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        /*
        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onQueryTextSubmit(String searchTerm) {
        Log.i(LOG_TAG, "onQueryTextSubmit: " + searchTerm);

        saveSharedPreferencesStringValueForKey(Const.PREF_QUERY_SEARCH, searchTerm);

        // Check if Internet present
        if (!mUtility.isConnectingToInternet()) {
            // Internet Connection is not present
            AlertDialog alertDialog = mUtility.createMsgWithAlertDialog(getString(R.string.alert_title_con_error), getString(R.string.alert_message_con_error), false);
            alertDialog.setButton(getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog.show();
        }else{
            if(!isTwitterLoggedInAlready()){
                AlertDialog alertDialog = mUtility.createMsgWithAlertDialog(getString(R.string.alert_twitter_title), getString(R.string.alert_twitter_message), false);
                alertDialog.setButton(getString(R.string.alert_ok_connect), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        launchTwitterService();
                    }
                });
                alertDialog.show();
            }else{
                // user already logged into twitter
                mUtility.showMsg(getString(R.string.search_for) + searchTerm);
                updateMap();
            }

        }

        return true;
    }

    private void launchTwitterService(){
        new AsyncLogin().execute();
        /*
        PendingIntent pendingResult = createPendingResult(Const.REQUEST_SERVICE_CODE, new Intent(), 0);
        Intent intent = new Intent(this, TwitterService.class);
        intent.putExtra(Const.PENDING_RESULT_EXTRA, pendingResult);
        mContext.startService(intent);
        */
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //Log.i(LOG_TAG, "onQueryTextChange: " + newText);
        return true;
    }

    private void updateMap() {

        String searchTerm = getSharedPreferencesStringValueForKey(Const.PREF_QUERY_SEARCH);
        if (mUtility.isConnectingToInternet() && !mUtility.isApplicationSentToBackground()) {
            Log.v(LOG_TAG, "Calling TwitterMapService with " + searchTerm + ".");

            new AsyncSearchTimelineInfo().execute(searchTerm);

            saveSharedPreferencesStringValueForKey(Const.PREF_QUERY_SEARCH,"");


        } else {
            Log.v(LOG_TAG, "No network connection available or app in background");
        }
    }

    private class AsyncSearchTimelineInfo extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.v(LOG_TAG, "AsyncSearchTimelineInfo: ADD data in content provider.");
            Vector<ContentValues> cVVector = new Vector<ContentValues>();
            ArrayList<twitter4j.Status> mTweets = new ArrayList<twitter4j.Status>();
            try {

                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(Const.CONSUMER_KEY);
                builder.setOAuthConsumerSecret(Const.CONSUMER_SECRET);

                // Access Token
                String access_token = getSharedPreferencesStringValueForKey(Const.PREF_KEY_TOKEN);
                // Access Token Secret
                String access_token_secret = getSharedPreferencesStringValueForKey(Const.PREF_KEY_SECRET);

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                mTwitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                Query query = new Query(params[0]);
                QueryResult resultQueryResult;
                Date currentDate = new Date();
                String dateStr = TwitterMapContract.getDbDateString(currentDate);
                do {
                    cVVector = new Vector<ContentValues>();
                    resultQueryResult = mTwitter.search(query);
                    List<twitter4j.Status> tweets = resultQueryResult.getTweets();
                    for (twitter4j.Status tweet : tweets) {
                        if (tweet.getGeoLocation() != null) {
                            mTweets.add(tweet);
                            ContentValues tweetValues = new ContentValues();
                            Log.d(LOG_TAG,"date to save: " + dateStr);
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_CREATED_AT, TwitterMapContract.getDbDateString(currentDate));
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_TITLE, tweet.getUser().getScreenName());
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_SUBTITLE, tweet.getText());
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_ICON, tweet.getUser().getBiggerProfileImageURL());
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_LAT, tweet.getGeoLocation().getLatitude());
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_LON, tweet.getGeoLocation().getLongitude());
                            Log.d(LOG_TAG, "New Tweet inserted: " + " @" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                            //getContentResolver().insert(TwitterMapContract.TwitterEntry.CONTENT_URI, tweetValues);
                            cVVector.add(tweetValues);
                        }
                    }
                    if ( cVVector.size() > 0 ) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        getContentResolver().bulkInsert(TwitterMapContract.TwitterEntry.CONTENT_URI,cvArray);
                        callAlarm(dateStr);
                    }
                } while ((query = resultQueryResult.nextQuery()) != null && mTweets.size()<Const.TWITTER_SIZE_LIMIT);

                Log.d(LOG_TAG, "Twitter Service Complete. " + mTweets.size() + " Inserted");

            } catch (TwitterException e) {
                Log.d(LOG_TAG, e.toString());
                Log.d(LOG_TAG, "Twitter Service Complete with Error: " + mTweets.size() + " Inserted");
            }



            return null;
        }
    }

    private void callAlarm(String dateStr){

        Intent alarmIntent = new Intent(getApplicationContext(), TwitterMapService.AlarmReceiver.class);
        alarmIntent.putExtra(TwitterMapService.SEARCH_QUERY_EXTRA, dateStr);
        alarmIntent.putExtra(TwitterMapService.DELETE_DATA_EXTRA, true);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);

        Calendar cur_cal = Calendar.getInstance();
        cur_cal.setTimeInMillis(System.currentTimeMillis());
        cur_cal.add(Calendar.SECOND, Const.TIME_TO_RELOAD);
        AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(), pendingIntent);

    }

    private class AsyncLogin extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                ConfigurationBuilder cb = new ConfigurationBuilder();
                cb.setDebugEnabled(true)
                        .setOAuthConsumerKey(Const.CONSUMER_KEY)
                        .setOAuthConsumerSecret(Const.CONSUMER_SECRET);

                TwitterFactory factory = new TwitterFactory(cb.build());
                UserSingleton.getInstance().setFactory(mFactory);
                mFactory = factory;

                Twitter twitter = mFactory.getInstance();
                UserSingleton.getInstance().setTwitter(twitter);
                mTwitter = twitter;

                RequestToken requestToken = mTwitter.getOAuthRequestToken(Const.CALLBACK_URL);
                UserSingleton.getInstance().setRequestToken(requestToken);
                mRequestToken = requestToken;

                mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(UserSingleton.getInstance().getRequestToken().getAuthenticationURL())));


            }catch (TwitterException e) {
            }catch (Exception e) {
            }

            return null;
        }
    }

    private class AsyncAccessToken extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            if(params.length<0)
                return null;
            String result = "";
            String verifier = params[0];
            try {
                // Get the access token
                AccessToken accessToken = null;
                if(mRequestToken!=null && verifier!=null){
                    accessToken = mTwitter.getOAuthAccessToken(mRequestToken, verifier);
                    UserSingleton.getInstance().setAccessToken(accessToken);
                    mAccessToken = accessToken;

                    saveSharedPreferencesStringValueForKey(Const.PREF_KEY_TOKEN,mAccessToken.getToken());
                    saveSharedPreferencesStringValueForKey(Const.PREF_KEY_SECRET,mAccessToken.getTokenSecret());
                    saveSharedPreferencesBooleanValueForKey(Const.PREF_KEY_TWITTER_LOGIN,true);

                    updateMap();
                }else{
                    Log.e(LOG_TAG,"AsyncAccessToken: Error in accessToken");
                }

            } catch (TwitterException e) {

            }
            return null;
        }
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                //setUpMap();
            }
        }
    }

    /*
     Loader<Cursor> methods
      */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader, id=" + id);
        Log.d(LOG_TAG, "onCreateLoader, args=" + args);
        String sortOrder = TwitterMapContract.TwitterEntry.COLUMN_CREATED_AT + " ASC";
        Uri twitterMapUri = TwitterMapContract.TwitterEntry.buildTwitterMap();

        return new CursorLoader(
                this,
                twitterMapUri,
                Const.TWITTER_ENTRY_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished, data=" + data);

        if (data != null && data.moveToFirst()) {

            int COLUMN_TITLE = data.getColumnIndex(TwitterMapContract.TwitterEntry.COLUMN_TITLE);
            int COLUMN_SUBTITLE = data.getColumnIndex(TwitterMapContract.TwitterEntry.COLUMN_SUBTITLE);
            int COLUMN_ICON = data.getColumnIndex(TwitterMapContract.TwitterEntry.COLUMN_ICON);
            int COLUMN_LAT = data.getColumnIndex(TwitterMapContract.TwitterEntry.COLUMN_LAT);
            int COLUMN_LON = data.getColumnIndex(TwitterMapContract.TwitterEntry.COLUMN_LON);
            int COLUMN_CREATED_AT = data.getColumnIndex(TwitterMapContract.TwitterEntry.COLUMN_CREATED_AT);
            MarkerOptions market = new MarkerOptions();
            ArrayList<MarkerOptions> marketArray = null;
            HashMap<String, ArrayList<MarkerOptions>> auxMarkerPoints = UserSingleton.getInstance().getArrayMarkerPoints();
            Date createdAt = null;
            String createdAtStr = "";

            for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()){
                String title = data.getString(COLUMN_TITLE);
                String subtitle = data.getString(COLUMN_SUBTITLE);
                String icon = data.getString(COLUMN_ICON);
                String lat = data.getString(COLUMN_LAT);
                double latitude = Double.parseDouble(lat);
                String lon = data.getString(COLUMN_LON);
                double longitude = Double.parseDouble(lon);
                createdAtStr = data.getString(COLUMN_CREATED_AT);
                createdAt = TwitterMapContract.getDateFromDb(createdAtStr);

                market = new MarkerOptions().position(new LatLng(latitude, longitude)).title(title);
                market.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));

                if(auxMarkerPoints.containsKey(createdAtStr)){
                    ArrayList<MarkerOptions> auxMarketArray = auxMarkerPoints.get(createdAtStr);
                    if(auxMarketArray!=null){
                        auxMarketArray.add(market);
                    }
                    auxMarkerPoints.put(createdAtStr,auxMarketArray);
                    UserSingleton.getInstance().setArrayMarkerPoints(auxMarkerPoints);
                }else{
                    marketArray = new ArrayList<MarkerOptions>();
                    marketArray.add(market);
                    auxMarkerPoints.put(createdAtStr,marketArray);
                    UserSingleton.getInstance().setArrayMarkerPoints(auxMarkerPoints);
                }
            }
            displayTweets();
        }else{
            displayTweetsOnMap(UserSingleton.getInstance().getArrayMarkerPoints());
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset, data=" + loader);
    }

    @Override
    protected void onDestroy() {
        Log.v(LOG_TAG, "onDestroy");
        super.onDestroy();
        if(mFetchTweetTask != null){
            mFetchTweetTask.cancel(true);
            mFetchTweetTask = null;
        }
    }

    @Override
    protected void onStop() {
        Log.v(LOG_TAG, "onStop");
        super.onStop();
        if(mFetchTweetTask != null){
            mFetchTweetTask.cancel(true);
            mFetchTweetTask = null;
        }

    }

// Util methods

    /**
     * Check if the account is authorized
     *
     * @return
     */
    public boolean isConnectedTwitter() {
        return mSharedPreferences.getString(Const.PREF_KEY_TOKEN, null) != null;
    }

    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     * */
    public boolean isTwitterLoggedInAlready() {
        return mSharedPreferences.getBoolean(Const.PREF_KEY_TWITTER_LOGIN, false);
    }

    /**
     * Check the value for exact key
     *
     * @return
     */
    public String getSharedPreferencesStringValueForKey(String key) {
        return mSharedPreferences.getString(key, "");
    }

    /**
     * Save String in Shared Preferences
     * */
    public boolean saveSharedPreferencesStringValueForKey(String key, String value) {
        return saveSharedPreferencesValueForKey(Const.TYPE_SharePrefValue_STR, key, value, 0, null);
    }

    /**
     * Save Int in Shared Preferences
     * */
    public boolean saveSharedPreferencesIntValueForKey(String key, Integer value) {
        return saveSharedPreferencesValueForKey(Const.TYPE_SharePrefValue_INT, key, null, value, null);
    }

    /**
     * Save Boolean in Shared Preferences
     * */
    public boolean saveSharedPreferencesBooleanValueForKey(String key, Boolean value) {
        return saveSharedPreferencesValueForKey(Const.TYPE_SharePrefValue_BOOL, key, null, 0, value);
    }

    public boolean saveSharedPreferencesValueForKey(int type, String key, String valueStr, int valueInt, Boolean valueBool) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        switch (type){
            case Const.TYPE_SharePrefValue_STR:
                editor.putString(key, valueStr);
                break;
            case Const.TYPE_SharePrefValue_INT:
                editor.putInt(key, valueInt);
                break;
            case Const.TYPE_SharePrefValue_BOOL:
                editor.putBoolean(key, valueBool);
                break;
            default:break;
        }
        return editor.commit();
    }

    // Unused methods

    Searches searches;
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(LOG_TAG, "Inside onNewIntent");
        searches = (Searches) getIntent().getBundleExtra("searchesbundle").get("searches");
        Log.e(LOG_TAG, "size of alertArray in ShowAlertsActivity = " + searches.size());
    }

    // FetchResultTask methods

    @Override
    public void onProgressUpdate(Search tweet) {
        Log.d(LOG_TAG, "onProgressUpdate from Main Activity:" + tweet.toString());
    }

    @Override
    public void onPostExecute(Searches tweets) {
        Log.d(LOG_TAG, "onPostExecute from Main Activity, tweets:" + tweets.size());
        mCallFetchTweetTask++;
        if (tweets.size() > 0) {
            // lets write the results to the console as well
            for (Search tweet : tweets) {

                if(tweet.getGeo()!= null && tweet.getGeo().getCoordinates()!= null){
                    if(!twits.contains(tweet))
                        twits.add(tweet);
                    double latitude = Double.parseDouble(tweet.getGeo().getCoordinates().get(0));
                    double longitude = Double.parseDouble(tweet.getGeo().getCoordinates().get(1));
                    String title = tweet.getUser().getName();

                    MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title(title);
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));

                    //mMap.addMarker(marker);

                    //mMarkerPoints.put(new Date(),marker);
                    displayTweets();
                    //displayTweetOnMap(latitude, longitude, title);

                }

            }
            //updateMap(mSearch);
        }

        if(twits.size()<10){
            if(mCallFetchTweetTask<3){
                updateMap();
            }else{
                mUtility.showMsg("No tweets received for " + mSearch);
            }
        }else{
            mCallFetchTweetTask=0;
        }

    }

    // AsynctTask to test

    private ArrayList<String> mTimelines = new ArrayList<String>();
    private class AsyncTimeline extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            Twitter twitter = UserSingleton.getInstance().getTwitter();
            String result = null;
            try {
                String searchTerm = getSharedPreferencesStringValueForKey(Const.PREF_QUERY_SEARCH);
                Query query = new Query(searchTerm);
                QueryResult resultQueryResult;
                do {
                    resultQueryResult = twitter.search(query);
                    List<twitter4j.Status> tweets = resultQueryResult.getTweets();
                    for (twitter4j.Status tweet : tweets) {
                        if (tweet.getGeoLocation() != null) {
                            mTimelines.add(tweet.getUser().getName() + " : " +
                                    tweet.getText());
                            Log.d(LOG_TAG, "Coordinate: " + tweet.getGeoLocation().getLatitude() + " : " +
                                    tweet.getGeoLocation().getLongitude());
                        }
                        //System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                    }
                } while ((query = resultQueryResult.nextQuery()) != null);

                /*
                List<twitter4j.Status> statuses = twitter.search(query); // getUserTimeline(mSearch,paging);
                for (twitter4j.Status status : statuses) {

                    if(status.getGeoLocation()!=null){
                        mTimelines.add(status.getUser().getName() + " : " +
                                status.getText());
                        Log.d(LOG_TAG, "Coordinate: " + status.getGeoLocation().getLatitude() + " : " +
                                status.getGeoLocation().getLongitude());
                    }

                }
                */

            } catch (TwitterException e) {
                Log.d(LOG_TAG, e.toString());
                result = e.getErrorMessage();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s == null) {
                // Do something with mTimelines
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.REQUEST_SERVICE_CODE) {
            switch (resultCode) {
                case Const.RESPONSE_INVALID_REQUEST:
                    mUtility.showMsg(data.getStringExtra(Const.RESPONSE_RESULT_EXTRA));
                    break;
                case Const.RESPONSE_ERROR_CODE:
                    mUtility.showMsg(data.getStringExtra(Const.RESPONSE_RESULT_EXTRA));
                    break;
                case Const.RESPONSE_RESULT_CODE:
                    this.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(UserSingleton.getInstance().getRequestToken().getAuthenticationURL())));
                    //finish();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    }

