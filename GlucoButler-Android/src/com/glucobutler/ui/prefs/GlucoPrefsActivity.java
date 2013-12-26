package com.glucobutler.ui.prefs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;

import com.glucobutler.ui.EditEntryActivity;
import com.glucobutler.ui.ListEntriesActivity;
import com.glucobutler.ui.UiConstants;

public class GlucoPrefsActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// prefs fragment
		getFragmentManager().beginTransaction().replace(
				android.R.id.content,
				new GlucoPrefsFragment()
		).commit();

		// allow to navigate back with action bar
		// DO NOT, force user to use HW back button
		String backActivity = readBackActivity();
		if (backActivity != null)
		{
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		// HACK: persist backActivity to restore it in child activity
		SharedPreferences prefs = getSharedPreferences(
			UiConstants.PREFS_BACK_ACTIVITY_PREF_NAME,
			MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.clear();
		editor.putString(UiConstants.PREFS_BACK_ACTIVITY_NAME, backActivity);
		editor.commit();
	}

	protected String readBackActivity()
	{
		String backActivity = null;
		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				backActivity = (String)extras.get(
					UiConstants.PREFS_BACK_ACTIVITY_NAME);
			}
		}
		return backActivity;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		Intent intent = null;
		switch (item.getItemId())
		{
		case android.R.id.home:
			String backActivity = readBackActivity();
			Class<?> backActivityClass = ListEntriesActivity.class;
			if (UiConstants.PREFS_BACK_ACTIVITY_EDIT.equals(backActivity))
			{
				backActivityClass = EditEntryActivity.class;
			}
			intent = new Intent(this, backActivityClass);
			startActivity(intent);
			break;
		}
		return true;
	}
}
