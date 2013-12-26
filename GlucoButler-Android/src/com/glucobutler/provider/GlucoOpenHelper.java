package com.glucobutler.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GlucoOpenHelper extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "gluco_butler";

	public static final String TABLE_GLUCO_ENTRY = "gluco_entry";

	public static final String COL_CREATED_ON   = GlucoValues.COL_NAME_TIMESTAMP;
	public static final String COL_MODIFIED_ON  = "modified_on";
	public static final String COL_ENC_KEY      = "enc_key";
	public static final String COL_VALUE        = GlucoValues.COL_NAME_GLUCO_VAL;
	public static final String COL_TIME_OF_DAY  = GlucoValues.COL_NAME_TIME_OF_DAY;
	public static final String COL_EATEN_UNITS  = GlucoValues.COL_NAME_EATEN_UNITS;
	public static final String COL_FACTOR       = GlucoValues.COL_NAME_FACTOR;
	public static final String COL_CORRECTION   = GlucoValues.COL_NAME_CORRECTION;
	public static final String COL_CASTED_UNITS = GlucoValues.COL_NAME_CASTED_UNITS;
	public static final String COL_COMMENT      = GlucoValues.COL_NAME_COMMENT;
	public static final String COL_UNIT         = GlucoValues.COL_NAME_UNIT;

//	private static final String CREATE_TABLE_GLUCO_ENTRY =
//		"CREATE TABLE " + TABLE_GLUCO_ENTRY + " ("
//			+ COL_CREATED_ON   + " INTEGER PRIMARY KEY NOT NULL, "
//			+ COL_MODIFIED_ON  + " INTEGER NOT NULL, " // must be inited with created_on
//			//+ COL_ENC_KEY      + " INTEGER NOT NULL REFERENCES " + TABLE_KEYS + "." + COL_KEY_ID + ", "
//			+ COL_VALUE        + " TEXT, "
//			+ COL_TIME_OF_DAY  + " TEXT, "
//			+ COL_EATEN_UNITS  + " TEXT, "
//			+ COL_FACTOR       + " TEXT, "
//			+ COL_CORRECTION   + " TEXT, "
//			+ COL_CASTED_UNITS + " TEXT, "
//			+ COL_COMMENT      + " TEXT, "
//			+ COL_UNIT         + " TEXT"
//		+ "); ";

	private static final String CREATE_TABLE_GLUCO_ENTRY =
		"CREATE TABLE " + TABLE_GLUCO_ENTRY + " ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_CREATED_ON   + " INTEGER NOT NULL, "
			+ COL_MODIFIED_ON  + " INTEGER NOT NULL, " // must be inited with created_on
			//+ COL_ENC_KEY      + " INTEGER NOT NULL REFERENCES " + TABLE_KEYS + "." + COL_KEY_ID + ", "
			+ COL_VALUE        + " INTEGER, "
			+ COL_TIME_OF_DAY  + " INTEGER, "
			+ COL_EATEN_UNITS  + " INTEGER, "
			+ COL_FACTOR       + " FLOAT, "
			+ COL_CORRECTION   + " INTEGER, "
			+ COL_CASTED_UNITS + " INTEGER, "
			+ COL_COMMENT      + " TEXT, "
			+ COL_UNIT         + " INTEGER"
		+ "); ";

	private static String CREATE_DATABASE =
			/*CREATE_TABLE_KEYS
			+ */CREATE_TABLE_GLUCO_ENTRY;

	public GlucoOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(CREATE_DATABASE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
}
