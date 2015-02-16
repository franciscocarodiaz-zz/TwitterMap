package fcd.com.twittermap.Service;

/**
 * Created by franciscocarodiaz on 16/01/15.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import fcd.com.twittermap.Twitter.Authenticated;
import fcd.com.twittermap.Twitter.Search;
import fcd.com.twittermap.Twitter.SearchResults;
import fcd.com.twittermap.Twitter.Searches;
import fcd.com.twittermap.Twitter.Twitter;
import fcd.com.twittermap.Util.Const;

public class FetchTweetTask extends AsyncTask<String, Search, String> {
    private final String LOG_TAG = FetchTweetTask.class.getSimpleName();
    private final Context mContext;


    public FetchTweetTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        this.cancel(true);
    }

    @Override
    protected void onProgressUpdate(Search... values) {
        Log.d("MyAsyncTask","onProgressUpdate");
        super.onProgressUpdate(values);
        ((Callback)mContext).onProgressUpdate(values[0]);
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onProgressUpdate(Search search);
        public void onPostExecute(Searches searches);
    }

    @Override
    protected String doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }
        String result = null;
        if (isCancelled()) {
            onCancelled();
        } else {
            String searchTerms = params[0];
            result = getSearchStream(searchTerms);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        Searches searches = jsonToSearches(result);

        Searches resultTwits = new Searches();

        // lets write the results to the console as well
        for (Search search : searches) {

            if(search.getGeo()!= null && search.getGeo().getCoordinates()!= null){
                Log.i(LOG_TAG, "lat:"+search.getGeo().getCoordinates().get(0));
                Log.i(LOG_TAG, "long:"+search.getGeo().getCoordinates().get(1));
                Log.i(LOG_TAG, search.getText());

                resultTwits.add(search);
                //publishProgress(search);
            }

        }
        ((Callback) mContext).onPostExecute(resultTwits);
    }

    // converts a string of JSON data into a Twitter object
    private Twitter jsonToTwitter(String result) {
        Twitter twits = null;
        if (result != null && result.length() > 0) {
            try {
                Gson gson = new Gson();
                twits = gson.fromJson(result, Twitter.class);
            } catch (IllegalStateException ex) {
                // just eat the exception
            }
        }
        return twits;
    }

    // converts a string of JSON data into a SearchResults object
    private Searches jsonToSearches(String result) {
        Searches searches = null;
        if (result != null && result.length() > 0) {
            try {
                Gson gson = new Gson();

                SearchResults sr = gson.fromJson(result, SearchResults.class);
                // but only pass the list of tweets found (called statuses)
                searches = sr.getStatuses();
            } catch (IllegalStateException ex) {
                // just eat the exception for now, but you'll need to add some handling here
            }
        }
        return searches;
    }

    // convert a JSON authentication object into an Authenticated object
    private Authenticated jsonToAuthenticated(String rawAuthorization) {
        Authenticated auth = null;
        if (rawAuthorization != null && rawAuthorization.length() > 0) {
            try {
                Gson gson = new Gson();
                auth = gson.fromJson(rawAuthorization, Authenticated.class);
            } catch (IllegalStateException ex) {
                // just eat the exception
            }
        }
        return auth;
    }

    private String getStream(String searchTerms) {
        String results = null;

        // Step 1: Encode consumer key and secret
        try {
            // URL encode the consumer key and secret
            String urlApiKey = URLEncoder.encode(Const.CONSUMER_KEY, "UTF-8");
            String urlApiSecret = URLEncoder.encode(Const.CONSUMER_SECRET, "UTF-8");

            // Concatenate the encoded consumer key, a colon character, and the
            // encoded consumer secret
            String combined = urlApiKey + ":" + urlApiSecret;

            // Base64 encode the string
            String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

            // Step 2: Obtain a bearer token
            HttpPost httpPost = new HttpPost(Const.TwitterTokenURL);
            httpPost.setHeader("Authorization", "Basic " + base64Encoded);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
            String rawAuthorization = ServiceHandler.getResponseBody(httpPost);
            Authenticated auth = jsonToAuthenticated(rawAuthorization);

            // Applications should verify that the value associated with the
            // token_type key of the returned object is bearer
            if (auth != null && auth.token_type.equals("bearer")) {
/*
                // Step 2: Obtain a bearer token
                httpPost = new HttpPost(Const.TwitterStreamURL+searchTerms);
                httpPost.setHeader("Authorization", "Bearer " + auth.access_token);
                httpPost.setHeader("Content-Type", "application/json");
                results = ServiceHandler.getResponseBody(httpPost);
*/
/*
                // Step 3: Authenticate API requests with bearer token
                HttpGet httpGet = new HttpGet(Const.TwitterStreamURL+searchTerms);
                Log.d(LOG_TAG,Const.TwitterStreamURL+searchTerms);
                // construct a normal HTTPS request and include an Authorization
                // header with the value of Bearer <>
                httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
                httpGet.setHeader("Content-Type", "application/json");
                // update the results with the body of the response
                results = getResponseBody(httpGet);
*/
/*

                ServiceHandler sh = new ServiceHandler();
                results = sh.makeServiceCall(Const.TwitterStreamURL+searchTerms, ServiceHandler.POST,auth.access_token);
                Log.d("Response: ", "> " + results);
*/
/*
                httpPost = new HttpPost(Const.TwitterStreamURL+"?track="+searchTerms);
                base64Encoded = Base64.encodeToString(auth.access_token.getBytes(), Base64.NO_WRAP);
                httpPost.setHeader("Authorization", "Basic " + base64Encoded);
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
                results = getResponseBody(httpPost);
*/

                // Step 3: Authenticate API requests with bearer token
                HttpGet httpGet = new HttpGet(Const.TwitterSearchURL + searchTerms);
                // construct a normal HTTPS request and include an Authorization
                // header with the value of Bearer <>
                httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
                httpGet.setHeader("Content-Type", "application/json");
                // update the results with the body of the response
                results = getResponseBody(httpGet);

            }
        } catch (UnsupportedEncodingException ex) {
        } catch (IllegalStateException ex1) {
        }
        return results;
    }

    private String getResponseBody(HttpRequestBase request) {
        StringBuilder sb = new StringBuilder();
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String reason = response.getStatusLine().getReasonPhrase();

            if (statusCode == 200) {

                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();

                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sb.append(line);
                }
            } else {
                sb.append(reason);
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "UnsupportedEncodingException:" + e.toString());
            Log.d(LOG_TAG,"UnsupportedEncodingException:" + e);
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            Log.e(LOG_TAG, "ClientProtocolException:"+e.toString());
            Log.d(LOG_TAG, "ClientProtocolException:" + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException:"+e.toString());
            Log.d(LOG_TAG, "IOException:" + e);
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String getSearchStream(String searchTerm) {
        String results = null;
        try {
            String encodedUrl = URLEncoder.encode(searchTerm, "UTF-8");
            //String encodedUrlLocations = URLEncoder.encode("&locations=-180,-90,180,90", "UTF-8");
            //results = getStream(Const.TwitterStreamURL + encodedUrl + encodedUrlLocations);
            results = getStream(encodedUrl);
        } catch (UnsupportedEncodingException ex) {
        } catch (IllegalStateException ex1) {
        }
        return results;
    }
    /**
     * Read tweets

    private void readTweets(){
        Log.d("HelloTwitter","Twitter4JActivity:readTweets()");
        ConfigurationBuilder cb=new ConfigurationBuilder();
        try {
            cb.setDebugEnabled(true).setOAuthConsumerKey(mConsumerKey).setOAuthConsumerSecret(mConsumerSecret).setOAuthAccessToken(mAccessKey).setOAuthAccessTokenSecret(mAccessSecret);
            TwitterFactory tf=new TwitterFactory(cb.build());
            Twitter twitter=tf.getInstance();
            Paging paging=new Paging(1,5);
            List<Status> statuses=twitter.getUserTimeline("uwaad_test",paging);
            mTweets.clear();
            for (    Status s : statuses) {
                mTweets.add(s.getText());
            }
            mTweets.notifyDataSetChanged();
        }
        catch (  TwitterException e) {
            e.printStackTrace();
        }
    }
     */

}
