package com.glucobutler.provider;

import android.net.Uri;


public final class GlucoValues
{
    private GlucoValues() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // URI stuff

	public static final String AUTHORITY = "com.glucobutler.provider.GlucoValues";
	public static final String SCHEME = "content://";

	// note: must not start with /, or UriMatcher will not work
    public static final String PATH_LIST = "gluco-vals";
    public static final String PATH_ITEM = PATH_LIST + "/";
    public static final String PATH_ITEM_PATTERN = PATH_ITEM + "#";

    public static final Uri CONTENT_URI_LIST =
    		Uri.parse(SCHEME + AUTHORITY + "/" + PATH_LIST);
    public static final Uri CONTENT_URI_ITEM_BASE =
    		Uri.parse(SCHEME + AUTHORITY + "/" + PATH_ITEM);
    public static final Uri CONTENT_URI_ITEM_PATTERN =
    		Uri.parse(SCHEME + AUTHORITY + PATH_ITEM_PATTERN);

    public static final int GLUCO_ID_PATH_POSITION = 1;

    ///////////////////////////////////////////////////////////////////////////
    // content type stuff

	public static final String ANDROID_TYPE_LIST = "vnd.android.cursor.dir/";
	public static final String ANDROID_TYPE_ITEM = "vnd.android.cursor.item/";

	public static final String CONTENT_TYPE_SELF = "vnd.glucobutler.gluco-vals";

	public static final String CONTENT_TYPE_LIST = ANDROID_TYPE_LIST + CONTENT_TYPE_SELF;
    public static final String CONTENT_TYPE_ITEM = ANDROID_TYPE_ITEM + CONTENT_TYPE_SELF;

    ///////////////////////////////////////////////////////////////////////////
    // column names

    public static final String COL_NAME_TIMESTAMP = "timestamp";
    public static final String COL_NAME_GLUCO_VAL = "gluco_val";
    public static final String COL_NAME_TIME_OF_DAY = "time_of_day";
    public static final String COL_NAME_EATEN_UNITS = "eaten_units";
    public static final String COL_NAME_FACTOR = "factor";
    public static final String COL_NAME_CORRECTION = "correction";
    public static final String COL_NAME_CASTED_UNITS = "casted_units";
    public static final String COL_NAME_COMMENT = "comment";
    public static final String COL_NAME_UNIT = "unit";
}
