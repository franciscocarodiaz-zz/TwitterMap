package fcd.com.twittermap.Twitter;
import java.util.ArrayList;
import java.util.List;

import fcd.com.twittermap.Util.Const;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by franciscocarodiaz on 12/02/15.
 */
public class TweetManager {

    private ConfigurationBuilder builder;
    private Twitter twitter;
    private TwitterFactory factory;

    public TweetManager() {
        builder = new ConfigurationBuilder();
        builder.setApplicationOnlyAuthEnabled(true);
        builder.setOAuthConsumerKey(Const.CONSUMER_KEY);
        builder.setOAuthConsumerSecret(Const.CONSUMER_SECRET);
        Configuration configuration = builder.build();
        factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();
    }

    public static ArrayList<String> getTweets(String topic) {

        Twitter twitter = new TwitterFactory().getInstance();
        ArrayList<String> tweetList = new ArrayList<String>();
        try {
            Query query = new Query(topic);
            QueryResult result;
            do {
                result = twitter.search(query);
                List<Status> tweets = result.getTweets();
                for (Status tweet : tweets) {
                    tweetList.add(tweet.getText());
                }
            } while ((query = result.nextQuery()) != null);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
        }
        return tweetList;
    }
}
