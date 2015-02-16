/**
 * Created by franciscocarodiaz on 9/03/15.
 */
package fcd.com.twittermap.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import fcd.com.twittermap.data.TwitterMapContract.*;

/**
 * Manages a local database for weather data.
 */
public class TwitterMapDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "twittermap.db";

    public TwitterMapDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_TWEET_TABLE = "CREATE TABLE " + TwitterEntry.TABLE_NAME + " (" +

                TwitterEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TwitterEntry.COLUMN_CREATED_AT + " TEXT NOT NULL, " +
                TwitterEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                TwitterEntry.COLUMN_SUBTITLE + " TEXT NOT NULL, " +
                TwitterEntry.COLUMN_ICON + " TEXT NOT NULL, " +
                TwitterEntry.COLUMN_LAT + " REAL NOT NULL," +
                TwitterEntry.COLUMN_LON + " REAL NOT NULL," +
                " UNIQUE (" + TwitterEntry._ID + ", " +
                TwitterEntry.COLUMN_CREATED_AT + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_TWEET_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TwitterEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
