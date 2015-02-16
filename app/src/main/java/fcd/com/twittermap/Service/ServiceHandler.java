package fcd.com.twittermap.Service;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import fcd.com.twittermap.Twitter.Authenticated;
import fcd.com.twittermap.Twitter.SearchResults;
import fcd.com.twittermap.Twitter.Searches;
import fcd.com.twittermap.Util.Const;
import fcd.com.twittermap.Util.UserSingleton;
import fcd.com.twittermap.Util.Utility;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class ServiceHandler {

	static String response = null;
	public final static int GET = 1;
	public final static int POST = 2;
    static private final String LOG_TAG = ServiceHandler.class.getSimpleName();
    private static Context mContext;
    private static Utility mUtility;

    public ServiceHandler() {

	}

    public ServiceHandler(Context context) {
        this.mContext = context;
        this.mUtility = new Utility(mContext);
    }

	/*
	 * Making service call
	 * @url - url to make request
	 * @method - http request method
	 * */
	public String makeServiceCall(String url, int method,String token) {
		return this.makeServiceCall(url, method, null,token);
	}

	/*
	 * Making service call
	 * @url - url to make request
	 * @method - http request method
	 * @params - http request params
	 * */
	public String makeServiceCall(String url, int method,
			List<NameValuePair> params,String token) {
		try {
			// http client
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpEntity httpEntity = null;
			HttpResponse httpResponse = null;


            // Checking http request method type
			if (method == POST) {
				HttpPost httpPost = new HttpPost(url);
				// adding post params

				if (params != null) {
					httpPost.setEntity(new UrlEncodedFormEntity(params));
				}


                // add header
                httpPost.setHeader("Authorization", "Bearer " + token);
                httpPost.setHeader("Content-Type", "application/json");
                httpResponse = httpClient.execute(httpPost);



			} else if (method == GET) {
				// appending params to url

				if (params != null) {
					String paramString = URLEncodedUtils
							.format(params, "utf-8");
					url += "?" + paramString;
				}
				HttpGet httpGet = new HttpGet(url);
                httpGet.setHeader("Authorization", "Bearer " + token);
                httpGet.setHeader("Content-Type", "application/json");
				httpResponse = httpClient.execute(httpGet);

			}
			httpEntity = httpResponse.getEntity();
			response = EntityUtils.toString(httpEntity);

		} catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "UnsupportedEncodingException:" + e.getMessage());
		} catch (ClientProtocolException e) {
            Log.d(LOG_TAG, "ClientProtocolException:" + e.getMessage());
		} catch (IOException e) {
            Log.d(LOG_TAG, "IOException:" + e.getMessage());
		}
		
		return response;

	}

    public static String getSearchStream(String searchTerm) {

        String results = null;
        try {
            String encodedUrl = URLEncoder.encode(searchTerm, "UTF-8");
            results = getStream(encodedUrl,Const.CONNECTION_TYPE_ASYNCTIMELINE);
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "UnsupportedEncodingException:"+e.getMessage());
        } catch (IllegalStateException e) {
            Log.d(LOG_TAG, "IllegalStateException:"+e.getMessage());
        }
        return results;
    }

    public static String getStream(String searchTerms, int connection_type) {
        String results = null;

        try {
            String urlApiKey = URLEncoder.encode(Const.CONSUMER_KEY, "UTF-8");
            String urlApiSecret = URLEncoder.encode(Const.CONSUMER_SECRET, "UTF-8");
            String combined = urlApiKey + ":" + urlApiSecret;
            String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

            HttpPost httpPost = new HttpPost(Const.TwitterTokenURL);
            httpPost.setHeader("Authorization", "Basic " + base64Encoded);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
            String rawAuthorization = getResponseBody(httpPost);
            Authenticated auth = jsonToAuthenticated(rawAuthorization);

            if (auth != null && auth.token_type.equals("bearer")) {

                switch (connection_type){
                    case Const.CONNECTION_TYPE_ASYNCTIMELINE:
                        ArrayList<twitter4j.Status> mTweets = new ArrayList<twitter4j.Status>();
                        Twitter twitter = UserSingleton.getInstance().getTwitter();
                        try {
                            Query query = new Query(searchTerms);
                            QueryResult resultQueryResult;
                            do {
                                resultQueryResult = twitter.search(query);
                                List<twitter4j.Status> tweets = resultQueryResult.getTweets();

                                for (twitter4j.Status tweet : tweets) {
                                    if(tweet.getGeoLocation()!=null){
                                        mTweets.add(tweet);
                                        //mTimelines.add(tweet.getUser().getName() + " : " +tweet.getText());
                                        Log.d(LOG_TAG, "Coordinate: " + tweet.getGeoLocation().getLatitude() + " : " +
                                                tweet.getGeoLocation().getLongitude());
                                    }
                                    //System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                                }
                            } while ((query = resultQueryResult.nextQuery()) != null);


                        } catch (TwitterException e) {
                            Log.d(LOG_TAG, e.toString());
                            //result = e.getErrorMessage();
                        }

                        //results = getResponseBody(httpGet);
                        break;
                    case Const.CONNECTION_TYPE_HTTP_PROXY:
                        /*
                        HttpPost httpPostReq = new HttpPost(Const.TwitterStreamURL+"?track="+searchTerms);
                        httpPostReq.setHeader("Authorization", "Bearer " + auth.access_token);
                        httpPostReq.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                        httpPostReq.setEntity(new StringEntity("grant_type=client_credentials"));

                        DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
                        try {
                            HttpHost proxy = new HttpHost("stream.twitter.com",8080,"https");
                            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                            httpClient.getCredentialsProvider().setCredentials(
                                    AuthScope.ANY,
                                    new UsernamePasswordCredentials(
                                            mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_TOKEN),
                                            mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_SECRET)));
                            urlApiKey = URLEncoder.encode(mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_TOKEN), "UTF-8");
                            urlApiSecret = URLEncoder.encode(mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_SECRET), "UTF-8");
                            combined = urlApiKey + ":" + urlApiSecret;
                            base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);
                            httpPostReq.setHeader("Authorization", "Bearer " + base64Encoded);

                            CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(Const.CONSUMER_KEY, Const.CONSUMER_SECRET);
                            String token = mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_TOKEN);
                            String tokenSecret = mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_SECRET);
                            consumer.setTokenWithSecret(token, tokenSecret);
                            consumer.sign(httpPostReq);
                            HttpResponse response = httpClient.execute(httpPostReq);
                            int statusCode = response.getStatusLine().getStatusCode();
                            String reason = response.getStatusLine().getReasonPhrase();
                            StringBuilder sb = new StringBuilder();
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
                            results = sb.toString();

                        } catch (IOException e) {
                            Log.e(LOG_TAG, "ERROR AT IOException ,ERROR TYPE " + e.getMessage());
                            e.printStackTrace();
                        } catch (OAuthExpectationFailedException e) {
                            e.printStackTrace();
                        } catch (OAuthCommunicationException e) {
                            e.printStackTrace();
                        } catch (OAuthMessageSignerException e) {
                            e.printStackTrace();
                        }

                        */

                        /*
                        DefaultHttpClient client = new DefaultHttpClient();
                        HttpHost proxy = new HttpHost("stream.twitter.com");
                        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);


                        HttpPost post = new HttpPost(Const.TwitterStreamURL+"?track="+searchTerms);
                        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
                        urlApiKey = URLEncoder.encode(mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_TOKEN), "UTF-8");
                        urlApiSecret = URLEncoder.encode(mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_SECRET), "UTF-8");
                        client.getCredentialsProvider().setCredentials(AuthScope.ANY,
                                new UsernamePasswordCredentials(mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_TOKEN), mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_SECRET)));
                        combined = urlApiKey + ":" + urlApiSecret;
                        base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);
                        post.setHeader("Authorization", "Bearer " + base64Encoded);

                        CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(Const.CONSUMER_KEY, Const.CONSUMER_SECRET);
                        String token = mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_TOKEN);
                        String tokenSecret = mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_SECRET);
                        consumer.setTokenWithSecret(token, tokenSecret);

                        try {
                            consumer.sign(post);
                            results = getResponseBody(post);
                        } catch (OAuthMessageSignerException e) {
                            Log.i(LOG_TAG, "ERROR AT OAUTH, OAuthMessageSignerException ,ERROR TYPE " + e);
                        } catch (OAuthExpectationFailedException e) {
                            Log.i(LOG_TAG, "ERROR AT OAUTH,  OAuthExpectationFailedException   ,ERROR TYPE " + e);
                        } catch (OAuthCommunicationException e) {
                            Log.i(LOG_TAG, "ERROR AT OAUTH,  OAuthCommunicationException   ,ERROR TYPE " + e);
                        }
                        */



                        break;
                    case Const.CONNECTION_TYPE_HTTP_GET:
                        // Using TwitterSearchURL
                        HttpGet httpGet = new HttpGet(Const.TwitterSearchURL + searchTerms);
                        httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
                        httpGet.setHeader("Content-Type", "application/json");
                        results = getResponseBody(httpGet);
                        break;
                    case Const.CONNECTION_TYPE_HTTP_POST:
                        /*
                        // Using TwitterStreamURL
                        HttpURLConnection urlConnection = null;
                        BufferedReader reader = null;
                        OutputStreamWriter request = null;
                        String parameters = "track="+searchTerms;
                        try {
                            final String QUERY_PARAM = "track";
                            Uri builtUri = Uri.parse(Const.TwitterStreamURL).buildUpon()
                                    .appendQueryParameter(QUERY_PARAM, searchTerms)
                                    .build();

                            URL url = new URL(builtUri.toString());
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setDoOutput(true);
                            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            urlApiKey = URLEncoder.encode(mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_TOKEN), "UTF-8");
                            urlApiSecret = URLEncoder.encode(mUtility.getSharedPreferencesStringValueForKey(Const.PREF_KEY_SECRET), "UTF-8");
                            combined = urlApiKey + ":" + urlApiSecret;
                            base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);
                            urlConnection.setRequestProperty("Authorization", "Basic " + base64Encoded);
                            urlConnection.setRequestMethod("POST");
                            urlConnection.setRequestProperty("version", "HTTP/1.1");
                            urlConnection.setRequestProperty("host", "stream.twitter.com");
                            urlConnection.setRequestProperty("user-agent", "Boost ASIO");
                            urlConnection.connect();
                            InputStream inputStream = urlConnection.getInputStream();
                            StringBuffer buffer = new StringBuffer();
                            if (inputStream == null) {
                                return null;
                            }
                            reader = new BufferedReader(new InputStreamReader(inputStream));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                buffer.append(line + "\n");
                            }
                            if (buffer.length() == 0) {
                                return null;
                            }
                            results = buffer.toString();


                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            Log.e(LOG_TAG, "Error ", e);
                            return null;
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                            Log.e(LOG_TAG, "Error ", e);
                            return null;
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(LOG_TAG, "Error ", e);
                            return null;
                        } finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (final IOException e) {
                                    Log.e(LOG_TAG, "Error closing stream", e);
                                }
                            }
                        }
                        break;
                    */
                }





            }
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "UnsupportedEncodingException:"+e.getMessage());
        } catch (IllegalStateException e) {
            Log.d(LOG_TAG, "IllegalStateException:"+e.getMessage());
        }
        return results;
    }

    public static ArrayList<twitter4j.Status> getTweets(String searchTerms) {

                        ArrayList<twitter4j.Status> mTweets = new ArrayList<twitter4j.Status>();
                        Twitter twitter = UserSingleton.getInstance().getTwitter();
                        try {
                            Query query = new Query(searchTerms);
                            QueryResult resultQueryResult;
                            do {
                                resultQueryResult = twitter.search(query);
                                List<twitter4j.Status> tweets = resultQueryResult.getTweets();

                                for (twitter4j.Status tweet : tweets) {
                                    if(tweet.getGeoLocation()!=null){
                                        mTweets.add(tweet);
                                        Log.d(LOG_TAG, "Coordinate: " + tweet.getGeoLocation().getLatitude() + " : " +
                                                tweet.getGeoLocation().getLongitude());
                                    }
                                }
                            } while ((query = resultQueryResult.nextQuery()) != null);


                        } catch (TwitterException e) {
                            Log.d(LOG_TAG, e.toString());
                            //result = e.getErrorMessage();
                        }

        return mTweets;
    }

    public static Searches jsonToSearchesWithGson(String result) {
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

    public static Searches jsonToSearches(String result) {
        Searches searches = null;
        /*
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";
        final String OWM_LIST = "list";
        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        if (result != null && result.length() > 0) {

        try {
            JSONObject forecastJson = new JSONObject(result);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);
            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);
// Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());
            for(int i = 0; i < weatherArray.length(); i++) {
// These are the values that will be collected.
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;
                double high;
                double low;
                String description;
                int weatherId;
// Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);
// The date/time is returned as a long. We need to convert that
// into something human-readable, since most people won't read "1400356800" as
// "this saturday".
                dateTime = dayForecast.getLong(OWM_DATETIME);
                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);
// Description is in a child array called "weather", which is 1 element long.
// That element also contains a weather code.
                JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);
// Temperatures are in a child object called "temp". Try not to name variables
// "temp" when working with temperature. It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);
                ContentValues weatherValues = new ContentValues();
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);
                cVVector.add(weatherValues);
            }
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                this.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,
                        cvArray);
            }
            Log.d(LOG_TAG, "Sunshine Service Complete. " + cVVector.size() + " Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        }
        */
        return searches;
    }

    public static Authenticated jsonToAuthenticated(String rawAuthorization) {
        Authenticated auth = null;
        if (rawAuthorization != null && rawAuthorization.length() > 0) {
            try {
                Gson gson = new Gson();
                auth = gson.fromJson(rawAuthorization, Authenticated.class);
            } catch (IllegalStateException e) {
                Log.d(LOG_TAG, "IllegalStateException:"+e.getMessage());
            }
        }
        return auth;
    }

    static public String getResponseBody(HttpRequestBase request) {
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
            Log.e(LOG_TAG, "e:" + e.toString());
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            Log.e(LOG_TAG, "e:"+e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "e:"+e.toString());
            e.printStackTrace();
        }
        return sb.toString();
    }
}
