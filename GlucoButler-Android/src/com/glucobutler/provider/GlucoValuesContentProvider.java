package com.glucobutler.provider;

import java.util.Date;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class GlucoValuesContentProvider extends ContentProvider
{
	public static final String LOG_TAG = "GlucoValuesContentProvider";

	private static final int URI_CODE_ROOT = 0;
	private static final int URI_CODE_LIST = 1;
	private static final int URI_CODE_SINGLE = 2;

	/**
	 * Used to determine if user wants a list of values or a single value.
	 */
	private static final UriMatcher uriMatcher;

	static {
		uriMatcher = new UriMatcher(URI_CODE_ROOT);

		uriMatcher.addURI(
			GlucoValues.AUTHORITY,
			GlucoValues.PATH_LIST,
			URI_CODE_LIST);
		uriMatcher.addURI(
			GlucoValues.AUTHORITY,
			GlucoValues.PATH_ITEM_PATTERN,
			URI_CODE_SINGLE);
	}

	private GlucoOpenHelper openHelper;

	@Override
	public boolean onCreate()
	{
		Log.d(LOG_TAG, "onCreate()");

		openHelper = new GlucoOpenHelper(getContext());

		return true;
	}

	@Override
	public String getType(Uri uri)
	{
		Log.d(LOG_TAG, "getType()");

		switch (uriMatcher.match(uri)) {
        case URI_CODE_LIST:
        	return GlucoValues.ANDROID_TYPE_LIST;

        case URI_CODE_SINGLE:
        	return GlucoValues.ANDROID_TYPE_ITEM;

        default:
        	return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		Log.d(LOG_TAG, "insert()");

		if (uriMatcher.match(uri) != URI_CODE_LIST) {
            throw new IllegalArgumentException("Invalid URI " + uri);
        }

		// set non-ui columns, like modifiedOn
		Long modifiedTimestamp = Long.valueOf(new Date().getTime());
		values.put(GlucoOpenHelper.COL_MODIFIED_ON, modifiedTimestamp);

		SQLiteDatabase db = openHelper.getWritableDatabase();

		long rowId = db.insert(
			GlucoOpenHelper.TABLE_GLUCO_ENTRY,
			GlucoValues.COL_NAME_GLUCO_VAL,
			values);

        if (rowId > 0) {
            Uri newUri = ContentUris.withAppendedId(GlucoValues.CONTENT_URI_ITEM_BASE, rowId);

            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }

        throw new SQLException("Failed to insert!!! " + uri);
	}

	@Override
	public Cursor query(
		Uri uri,
		String[] projection,
		String selection,
		String[] selectionArgs,
		String sortOrder)
	{
		Log.d(LOG_TAG, "query()");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		switch (uriMatcher.match(uri)) {
        case URI_CODE_LIST:
        	Log.d(LOG_TAG, "queriing list");
    		return db.query(
				GlucoOpenHelper.TABLE_GLUCO_ENTRY,
				projection,
				selection,
				selectionArgs,
				null,
				null,
				GlucoValues.COL_NAME_TIMESTAMP + " DESC");

        case URI_CODE_SINGLE:
    		String id = findIdInUri(uri);
        	Log.d(LOG_TAG, "queriing single val with id: " + id);
        	Cursor cursor = db.query(
				GlucoOpenHelper.TABLE_GLUCO_ENTRY,
				null,
				//GlucoValues.COL_NAME_TIMESTAMP + " = ?",
				"_id = ?",
				new String[]{id},
				null,
				null,
				null);
        	Log.d(LOG_TAG, "num of elems in single query: " + cursor.getCount());
        	return cursor;

        default:
		}
		return null;
	}

	@Override
	public int update(
		Uri uri,
		ContentValues values,
		String selection,
		String[] selectionArgs)
	{
		if (uriMatcher.match(uri) != URI_CODE_SINGLE) {
            throw new IllegalArgumentException("Invalid URI " + uri);
        }

		String id = findIdInUri(uri);
    	Log.d(LOG_TAG, "updating id: " + id);

    	// add modified date
    	Date modifiedDate = new Date();
    	values.put(
    		GlucoOpenHelper.COL_MODIFIED_ON,
    		modifiedDate.getTime());

    	SQLiteDatabase db = openHelper.getWritableDatabase();
    	return db.update(
			GlucoOpenHelper.TABLE_GLUCO_ENTRY,
			values,
			"_id = ?",
			new String[]{id});
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		if (uriMatcher.match(uri) != URI_CODE_SINGLE) {
            throw new IllegalArgumentException("Invalid URI " + uri);
        }

		String id = findIdInUri(uri);
    	Log.d(LOG_TAG, "deleting id: " + id);

		// TODO what about sync? really delete or just mark?

    	SQLiteDatabase db = openHelper.getWritableDatabase();

    	return db.delete(
			GlucoOpenHelper.TABLE_GLUCO_ENTRY,
			"_id = ?",
			new String[]{id});
	}

	protected String findIdInUri(Uri uri)
	{
		return uri.getPathSegments().get(GlucoValues.GLUCO_ID_PATH_POSITION);
	}
}
