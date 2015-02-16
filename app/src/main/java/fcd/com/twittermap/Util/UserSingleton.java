package fcd.com.twittermap.Util;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by franciscocarodiaz on 14/02/15.
 */
public class UserSingleton {
    private static UserSingleton mInstance;
    private TwitterFactory mFactory;
    private Twitter mTwitter;
    private RequestToken mRequestToken;
    private AccessToken accessToken;
    private HashMap<String, ArrayList<MarkerOptions>> mArrayMarkerPoints;

    private UserSingleton(){
        mArrayMarkerPoints = new HashMap<String, ArrayList<MarkerOptions>>();
    }

    public static synchronized UserSingleton getInstance(){

        if(mInstance == null)
        {
            mInstance = new UserSingleton();
        }
        return mInstance;
    }

    public HashMap<String, ArrayList<MarkerOptions>> getArrayMarkerPoints() {
        return mArrayMarkerPoints;
    }

    public void setArrayMarkerPoints(HashMap<String, ArrayList<MarkerOptions>> mMarkerPoints) {
        this.mArrayMarkerPoints = mArrayMarkerPoints;
    }

    public Twitter getTwitter() {
        return mTwitter;
    }

    public void setTwitter(Twitter mTwitter) {
        this.mTwitter = mTwitter;
    }

    public TwitterFactory getFactory() {
        return mFactory;
    }

    public void setFactory(TwitterFactory mFactory) {
        this.mFactory = mFactory;
    }

    public RequestToken getRequestToken() {
        return mRequestToken;
    }

    public void setRequestToken(RequestToken mRequestToken) {
        this.mRequestToken = mRequestToken;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

}
