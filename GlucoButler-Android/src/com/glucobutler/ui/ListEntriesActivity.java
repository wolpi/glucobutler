package com.glucobutler.ui;

import java.util.Date;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.glucobutler.R;
import com.glucobutler.model.TimeOfDay;
import com.glucobutler.provider.GlucoValues;
import com.glucobutler.ui.prefs.GlucoPrefsActivity;

public class ListEntriesActivity extends Activity
{
    private static final String[] PROJECTION = new String[] {
    	"_id",
    	GlucoValues.COL_NAME_TIME_OF_DAY,
        GlucoValues.COL_NAME_TIMESTAMP,
        GlucoValues.COL_NAME_GLUCO_VAL
    };

    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        setContentView(R.layout.list_activity_layout);
        final ListView listView = (ListView) findViewById(R.id.list_view_gluco_entries);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(GlucoValues.CONTENT_URI_LIST);
        }

        final Cursor cursor = getContentResolver().query(
        	intent.getData(),
        	PROJECTION,
        	null,
        	null,
        	null);

        Log.d(getClass().getSimpleName(), "****** num of elems in cursor: " + cursor.getCount());

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(new CursorAdapter(getBaseContext(), cursor, true) {

					@Override
					public View newView(Context context, Cursor cursor, ViewGroup viewGroup)
					{
						LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
						View rowView = inflater.inflate(
							R.layout.list_activity_element_layout,
							viewGroup,
							false);
						return rowView;
					}

					@Override
					public void bindView(View view, Context context, final Cursor cursor)
					{
						// TOD
						int todId = cursor.getInt(1);
						TimeOfDay tod = TimeOfDay.fromInt(todId);
						TimeOfDayTrans todt = TimeOfDayTrans.fromTod(tod);

						TextView todView = (TextView) view.findViewById(
							R.id.gluco_val_list_tod);
						todView.setText(todt.getTextId());

						// time stamp
						long ts = cursor.getLong(2);
						Date timeStamp = new Date(ts);
						java.text.DateFormat dateFormat = DateFormat.getDateFormat(context);
						java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);

						TextView dateView = (TextView) view.findViewById(
							R.id.gluco_val_list_date);
						dateView.setText(dateFormat.format(timeStamp));

						TextView timeView = (TextView) view.findViewById(
							R.id.gluco_val_list_time);
				        timeView.setText(timeFormat.format(timeStamp));

				        // gluco val
				        int glucoVal = cursor.getInt(3);
				        TextView valView = (TextView) view.findViewById(
				        	R.id.gluco_val_list_val);
				        valView.setText(String.valueOf(glucoVal));

				        // debug id
						final int id = cursor.getInt(0);
						Log.d(getClass().getSimpleName(), "list entry id: " + id);

						// on touch listener: visual feedback, start edit activity
						view.setOnTouchListener(new OnTouchListener() {

							private Drawable background;

							@Override
							public boolean onTouch(View v, MotionEvent event)
							{
								switch(event.getAction())
					            {
								case MotionEvent.ACTION_DOWN:
									background = v.getBackground();
									int color = getResources().getColor(R.color.pref_ontouch_bg);
									v.setBackgroundColor(color);

									Intent intent = new Intent(
										ListEntriesActivity.this,
										EditEntryActivity.class);
									Uri uri = ContentUris.withAppendedId(
										GlucoValues.CONTENT_URI_ITEM_BASE,
										id);
									intent.setData(uri);
									intent.setAction(Intent.ACTION_EDIT);
									startActivity(intent);

									break;
					            case MotionEvent.ACTION_UP:
					            	v.setBackgroundDrawable(background);
					            	background = null;
									break;
					            }
								return false;
							}
						});
					}
				});
            }
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.list_activity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		Intent intent = null;
		switch (item.getItemId())
		{
		case R.id.menu_settings:
			intent = new Intent(this, GlucoPrefsActivity.class);
			intent.putExtra(
				UiConstants.PREFS_BACK_ACTIVITY_NAME,
				UiConstants.PREFS_BACK_ACTIVITY_LIST);
			startActivity(intent);
			break;
		case R.id.menu_new:
			intent = new Intent(this, EditEntryActivity.class);
			startActivity(intent);
			break;
		}

		return true;
	}
}
