/**
 * Created by franciscocarodiaz on 9/03/15.
 */
package fcd.com.twittermap.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class TwitterMapProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private TwitterMapDbHelper mOpenHelper;
    private static final int TWITTER_MAP = 100;
    private static final int TWITTER_MAP_WITH_DATE = 101;

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TwitterMapContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, TwitterMapContract.PATH_TWITTER, TWITTER_MAP);
        matcher.addURI(authority, TwitterMapContract.PATH_TWITTER + "/*", TWITTER_MAP_WITH_DATE);
        return matcher;
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new TwitterMapDbHelper(getContext());
        return true;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*"
            case TWITTER_MAP_WITH_DATE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TwitterMapContract.TwitterEntry.TABLE_NAME,
                        projection,
                        TwitterMapContract.TwitterEntry.COLUMN_CREATED_AT + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "tweet"
            case TWITTER_MAP: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TwitterMapContract.TwitterEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }
    @Override
    public String getType(Uri uri) {
// Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TWITTER_MAP_WITH_DATE:
                return TwitterMapContract.TwitterEntry.CONTENT_TYPE;
            case TWITTER_MAP:
                return TwitterMapContract.TwitterEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case TWITTER_MAP: {
                long _id = db.insert(TwitterMapContract.TwitterEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TwitterMapContract.TwitterEntry.buildTweetUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case TWITTER_MAP:
                rowsDeleted = db.delete(
                        TwitterMapContract.TwitterEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
// Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }
    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case TWITTER_MAP:
                rowsUpdated = db.update(TwitterMapContract.TwitterEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TWITTER_MAP:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TwitterMapContract.TwitterEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
