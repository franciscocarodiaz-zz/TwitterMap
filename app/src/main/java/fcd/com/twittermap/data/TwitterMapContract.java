/**
 * Created by franciscocarodiaz on 9/03/15.
 */
package fcd.com.twittermap.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fcd.com.twittermap.Util.Const;

/**
 * Defines table and column names for the weather database.
 */
public class TwitterMapContract {

    public static final String CONTENT_AUTHORITY = "fcd.com.twittermap";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TWITTER = "twitter";

    /* Inner class that defines the table contents of the weather table */
    public static final class TwitterEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TWITTER).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_TWITTER;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_TWITTER;
        public static final String TABLE_NAME = "TwitterMap";
        public static final String COLUMN_ID = Const.TWITTER_ENTRY_COLUMN_ID;
        public static final String COLUMN_CREATED_AT = Const.TWITTER_ENTRY_COLUMN_CREATED_AT;
        public static final String COLUMN_TITLE = Const.TWITTER_ENTRY_COLUMN_TITLE;
        public static final String COLUMN_SUBTITLE = Const.TWITTER_ENTRY_COLUMN_SUBTITLE;
        public static final String COLUMN_ICON = Const.TWITTER_ENTRY_COLUMN_ICON;
        public static final String COLUMN_LAT = Const.TWITTER_ENTRY_COLUMN_LAT;
        public static final String COLUMN_LON = Const.TWITTER_ENTRY_COLUMN_LON;

        public static Uri buildTweetUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTwitterMap() {
            return CONTENT_URI.buildUpon().build();
        }

        public static Uri buildTwitterMapWithDate(String date) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_CREATED_AT, date).build();
        }

    }

    public static final String DATE_FORMAT = "yyyyMMddHHmmss.FFF";


    /**
     * Converts Date class to a string representation, used for easy comparison and database lookup.
     * @param date The input date
     * @return a DB-friendly representation of the date, using the format defined in DATE_FORMAT.
     */

    public static String getDbDateString(Date date){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }


    /**
     * Converts a dateText to a long Unix time representation
     * @param dateText the input date string
     * @return the Date object
     */

    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dbDateFormat.parse(dateText);
        } catch ( ParseException e ) {
            e.printStackTrace();
            return null;
        }
    }
}
