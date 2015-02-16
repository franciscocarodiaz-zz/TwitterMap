package fcd.com.twittermap.Service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import fcd.com.twittermap.Util.Const;
import fcd.com.twittermap.Util.UserSingleton;
import fcd.com.twittermap.data.TwitterMapContract;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by franciscocarodiaz on 21/01/15.
 */
public class TwitterMapService extends IntentService {

    public static final String SEARCH_QUERY_EXTRA = "dqe";
    public static final String DELETE_DATA_EXTRA = "delete_data";
    private static final String LOG_TAG = TwitterMapService.class.getSimpleName();
    private Context mServiceContext;
    private ServiceHandler mServiceHandler;

    public TwitterMapService() {
        super("TwitterMapService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mServiceContext = getApplicationContext();
        mServiceHandler = new ServiceHandler(mServiceContext);

        Log.d(LOG_TAG, "onHandleIntent running");

        Boolean deleteData = intent.getBooleanExtra(DELETE_DATA_EXTRA, false);

        if (deleteData) {
            Log.v(LOG_TAG, "onHandleIntent: DELETE data in content provider.");
            String searchTerms = intent.getStringExtra(SEARCH_QUERY_EXTRA);

            this.getContentResolver().delete(
                    TwitterMapContract.TwitterEntry.CONTENT_URI,
                    Const.TWITTER_ENTRY_COLUMN_CREATED_AT +"='"+searchTerms+"'",
                    null
            );

        }

        String searchTerms = intent.getStringExtra(SEARCH_QUERY_EXTRA);
        if (!searchTerms.equals("") && !deleteData ) {
            Log.v(LOG_TAG, "onHandleIntent: ADD data in content provider.");
            //ArrayList<Status> tweets = mServiceHandler.getTweets(searchTerms);
            Twitter twitter = UserSingleton.getInstance().getTwitter();
            Vector<ContentValues> cVVector = new Vector<ContentValues>();
            ArrayList<twitter4j.Status> mTweets = new ArrayList<Status>();
            try {

                Query query = new Query(searchTerms);
                QueryResult resultQueryResult;
                Date currentDate = new Date();
                do {
                    resultQueryResult = twitter.search(query);
                    List<Status> tweets = resultQueryResult.getTweets();
                    for (twitter4j.Status tweet : tweets) {
                        if (tweet.getGeoLocation() != null) {
                            mTweets.add(tweet);
                            ContentValues tweetValues = new ContentValues();
                            String dateStr = TwitterMapContract.getDbDateString(currentDate);
                            Log.d(LOG_TAG,"date to save: " + dateStr);
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_CREATED_AT, TwitterMapContract.getDbDateString(currentDate));
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_TITLE, tweet.getUser().getScreenName());
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_SUBTITLE, tweet.getText());
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_ICON, tweet.getUser().getBiggerProfileImageURL());
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_LAT, tweet.getGeoLocation().getLatitude());
                            tweetValues.put(Const.TWITTER_ENTRY_COLUMN_LON, tweet.getGeoLocation().getLongitude());
                            Log.d(LOG_TAG, "New Tweet inserted: " + " @" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                            this.getContentResolver().insert(TwitterMapContract.TwitterEntry.CONTENT_URI, tweetValues);
                            cVVector.add(tweetValues);
                        }
                    }
                } while ((query = resultQueryResult.nextQuery()) != null && cVVector.size()<Const.TWITTER_SIZE_LIMIT);

                Log.d(LOG_TAG, "Twitter Service Complete. " + cVVector.size() + " Inserted");

            } catch (TwitterException e) {
                Log.d(LOG_TAG, e.toString());
                Log.d(LOG_TAG, "Twitter Service Complete with Error: " + cVVector.size() + " Inserted");
            }

            /*
            String result = mServiceHandler.getSearchStream(searchTerms);
            Log.v(LOG_TAG, "result: " + result);
            Searches searches = mServiceHandler.jsonToSearchesWithGson(result);
            //Searches searches = jsonToSearches(result);

            Searches resultTwits = new Searches();
            Log.v(LOG_TAG, "onHandleIntent: searches=" + searches.size());
            if(searches.size()>0){
                Vector<ContentValues> cVVector = new Vector<ContentValues>(searches.size());
                for (Search search : searches) {
                    if(search.getGeo()!= null && search.getGeo().getCoordinates()!= null){
                        resultTwits.add(search);
                        ContentValues tweetValues = new ContentValues();
                        tweetValues.put(Const.TWITTER_ENTRY_COLUMN_CREATED_AT, search.getDateCreated());
                        tweetValues.put(Const.TWITTER_ENTRY_COLUMN_TITLE, search.getUser().getScreenName());
                        tweetValues.put(Const.TWITTER_ENTRY_COLUMN_SUBTITLE, search.getText());
                        tweetValues.put(Const.TWITTER_ENTRY_COLUMN_ICON, search.getUser().getProfileImageUrl());
                        tweetValues.put(Const.TWITTER_ENTRY_COLUMN_LAT, search.getGeo().getCoordinates().get(0));
                        tweetValues.put(Const.TWITTER_ENTRY_COLUMN_LON, search.getGeo().getCoordinates().get(1));
                        cVVector.add(tweetValues);
                        Log.d(LOG_TAG,"tweet inserted: " + search.toString());
                    }
                }
                if ( cVVector.size() > 0 ) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    this.getContentResolver().bulkInsert(TwitterMapContract.TwitterEntry.CONTENT_URI,cvArray);
                }
                Log.d(LOG_TAG, "Twitter Service Complete. " + cVVector.size() + " Inserted");
                */
            /*
            Intent intentShowAlertsActivity = new Intent(this, MainActivity.class);
            Bundle b = new Bundle();
            b.putSerializable("searches", resultTwits);
            intentShowAlertsActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentShowAlertsActivity.putExtra("searchesbundle", b);
            startActivity(intentShowAlertsActivity);
            */
        }

    }






    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "AlarmReceiver running");
            Intent sendIntent = new Intent(context, TwitterMapService.class);
            sendIntent.putExtra(TwitterMapService.SEARCH_QUERY_EXTRA, intent.getStringExtra(TwitterMapService.SEARCH_QUERY_EXTRA));
            sendIntent.putExtra(TwitterMapService.DELETE_DATA_EXTRA, intent.getBooleanExtra(TwitterMapService.DELETE_DATA_EXTRA, false));
            context.startService(sendIntent);
        }
    }
}
