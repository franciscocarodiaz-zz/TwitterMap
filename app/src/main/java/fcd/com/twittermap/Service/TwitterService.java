package fcd.com.twittermap.Service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by franciscocarodiaz on 14/02/15.
 */
public class TwitterService extends IntentService {

    private static final String LOG_TAG = TwitterService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public TwitterService() {
        super("TwitterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        /*
        PendingIntent reply = intent.getParcelableExtra(Const.PENDING_RESULT_EXTRA);
            try {
                ConfigurationBuilder cb = new ConfigurationBuilder();
                cb.setDebugEnabled(true)
                        .setOAuthConsumerKey(Const.CONSUMER_KEY)
                        .setOAuthConsumerSecret(Const.CONSUMER_SECRET);

                TwitterFactory mFactory = new TwitterFactory(cb.build());
                UserSingleton.getInstance().setFactory(mFactory);
                Twitter mTwitter = mFactory.getInstance();
                UserSingleton.getInstance().setTwitter(mTwitter);
                Intent result = new Intent();
                try {
                    RequestToken requestToken = mTwitter.getOAuthRequestToken(Const.CALLBACK_URL);
                    UserSingleton.getInstance().setRequestToken(requestToken);
                    reply.send(this, Const.RESPONSE_RESULT_CODE, result);

                } catch (TwitterException e) {
                    result.putExtra(Const.RESPONSE_RESULT_EXTRA, "Error trying to connect twitter: " + e.getMessage());
                    reply.send(this, Const.RESPONSE_INVALID_REQUEST, result);
                    e.printStackTrace();
                }catch (Exception e) {
                    // could do better by treating the different sax/xml exceptions individually
                    result.putExtra(Const.RESPONSE_RESULT_EXTRA, "Error trying to connect twitter: " + e.getMessage());
                    reply.send(Const.RESPONSE_ERROR_CODE);
                }


            } catch (PendingIntent.CanceledException exc) {
                Log.e(LOG_TAG, "reply cancelled", exc);
            }
        */
    }
}
