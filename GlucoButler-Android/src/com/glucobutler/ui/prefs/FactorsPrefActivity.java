package com.glucobutler.ui.prefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.glucobutler.R;
import com.glucobutler.model.TimeOfDay;
import com.glucobutler.ui.TimeOfDayTrans;
import com.glucobutler.ui.UiConstants;

public class FactorsPrefActivity extends Activity
{
	private final class FactorPrefOnClickListener implements View.OnClickListener
	{
		private final TimeOfDayTrans todt;

		private FactorPrefOnClickListener(TimeOfDayTrans todt)
		{
			this.todt = todt;
		}

		@Override
		public void onClick(View v)
		{
			Activity activity = FactorsPrefActivity.this;

			// dialog builder
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);

			// layout
			LayoutInflater inflater = activity.getLayoutInflater();
			builder.setView(inflater.inflate(R.layout.factors_pref_diag_layout, null));

			// title, why must I handle text paras myself ????
			CharSequence title = getText(R.string.pref_factor_diag_title);
			String titleStr = title.toString();
			if (titleStr.contains("{0}")) {
				String todText = getText(todt.getTextId()).toString();
				titleStr = titleStr.replace("{0}", todText);
			}
			builder.setTitle(titleStr);

			// buttons
			builder.setPositiveButton(
				android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						EditText editView = (EditText)((AlertDialog) dialog).findViewById(
							R.id.pref_factor_diag_input);
						String value = editView.getText().toString();
						saveFactorPref(todt.getTimeOfDay(), value);

						// recreated UI
						createViews();
					}
				});
			builder.setNegativeButton(
				android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

			// create dialog
			AlertDialog dialog = builder.create();

			// finally show dialog
			dialog.show();

			// init value, after showing, or view will be null
			Float factorValue = readFactorPref(todt.getTimeOfDay());
			if (factorValue != null) {
				EditText editView = (EditText)dialog.findViewById(
					R.id.pref_factor_diag_input);
				editView.setText(factorValueToString(factorValue));
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.factors_pref_activity_layout);

		//Log.d(getClass().getSimpleName(), "onCreate()");

		// allow to navigate back with action bar
		// DO NOT, force user to use HW back button
		getActionBar().setDisplayHomeAsUpEnabled(true);

		createViews();
	}

	protected void createViews()
	{
		// TODO factor prefs: use relative w/h and margins

		final ViewGroup container = (ViewGroup)findViewById(R.id.factorsPrefsContainer);

		// clear old views
		container.removeAllViews();

		// create views for each TOD
		for (final TimeOfDayTrans todt : TimeOfDayTrans.values())
		{
			LinearLayout rowLayout = new LinearLayout(container.getContext());
			container.addView(rowLayout);
			rowLayout.setOrientation(LinearLayout.HORIZONTAL);
			LayoutParams rowParams = new LayoutParams();
			rowParams.height = LayoutParams.WRAP_CONTENT;
			rowParams.width = LayoutParams.MATCH_PARENT;
			rowLayout.setLayoutParams(rowParams);

			TextView textView = new TextView(rowLayout.getContext());
			rowLayout.addView(textView);

			// check if there is a value
			Float factor = readFactorPref(todt.getTimeOfDay());
			if (factor != null) {
				// display value
				TextView valueView = new TextView(rowLayout.getContext());
				rowLayout.addView(valueView);
				valueView.setText(factorValueToString(factor));
				valueView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

				LayoutParams valueParams = new LayoutParams();
				valueParams.height = LayoutParams.MATCH_PARENT;
				valueParams.width = 0;
				valueParams.weight = 2;
				valueParams.rightMargin = 25;
				valueView.setLayoutParams(valueParams);
			}

			// border, add to container, not row
	    	Drawable border = getResources().getDrawable(R.drawable.factor_pref_border);
	    	ImageView borderView = new ImageView(container.getContext());
	    	borderView.setImageDrawable(border);
	    	container.addView(borderView);
			LayoutParams borderParams = new LayoutParams();
			borderParams.height = 1;
			borderParams.width = LayoutParams.MATCH_PARENT;
			borderParams.leftMargin = 20;
			borderParams.rightMargin = 20;
			borderView.setLayoutParams(borderParams);

			// w / h
			LayoutParams params = new LayoutParams();
	    	//params.height = 50; // looks bad on some devices
	    	params.width = 0;
	    	params.weight = 1;

	    	// margin
	    	params.topMargin = 5;
	    	params.bottomMargin = 5;
	    	params.leftMargin = 25;
	    	//params.rightMargin = 25;

	    	// note: increasing text size may look bad on some devices
	    	// so just making text bold
	    	textView.setTypeface(textView.getTypeface(), Typeface.BOLD);

	    	// apply layout, set text
	    	textView.setLayoutParams(params);
	    	textView.setGravity(
	    		Gravity.LEFT | Gravity.CENTER_VERTICAL | Gravity.FILL_HORIZONTAL);
			textView.setText(todt.getTextId());

			// on touch listener for visual feedback
			rowLayout.setOnTouchListener(new OnTouchListener() {

				private Drawable background;

				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					//Log.d("row touch", "onTouch(): " + event.getAction());
					switch(event.getAction())
		            {
					case MotionEvent.ACTION_DOWN:
						background = v.getBackground();
						int color = getResources().getColor(R.color.pref_ontouch_bg);
						v.setBackgroundColor(color);
						break;
		            case MotionEvent.ACTION_UP:
		            case MotionEvent.ACTION_CANCEL:
		            	v.setBackgroundDrawable(background);
		            	background = null;
						break;
		            }
					// must be false or diag will not show up
					return false;
				}
			});

			// on click dialog
			rowLayout.setOnClickListener(new FactorPrefOnClickListener(todt));
		}
	}

	protected void saveFactorPref(TimeOfDay tod, String valueStr)
	{
		SharedPreferences prefs = getSharedPreferences(
			UiConstants.PREFS_FACTORS_NAME,
			MODE_PRIVATE);

		Float value = factorValueFromString(valueStr);

		Editor edit = prefs.edit();
		edit.putFloat(tod.getIdStr(), value);
		edit.commit();
	}

	protected Float readFactorPref(TimeOfDay tod)
	{
		SharedPreferences prefs = getSharedPreferences(
				UiConstants.PREFS_FACTORS_NAME,
				MODE_PRIVATE);

		return readFactorPref(prefs, tod);
	}

	public static Float readFactorPref(SharedPreferences prefs, TimeOfDay tod)
	{
		float val = prefs.getFloat(tod.getIdStr(), 0.0f);
		return val == 0.0f
			? null
			: Float.valueOf(val);
	}

	public static Float factorValueFromString(String valueStr)
	{
		// TODO factor from locale specific string
		try {
			return Float.valueOf(valueStr);
		} catch (Exception e) {
			return null;
		}
	}

	public static String factorValueToString(float value)
	{
		// TODO factor to locale specific string
		return String.valueOf(value);
	}

	public static String factorValueToString(Float value)
	{
		// TODO factor to locale specific string
		return value != null
			? value.toString()
			: "";
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		Intent intent = null;
		switch (item.getItemId())
		{
		case android.R.id.home:
			// HACK: read backActivity from prefs
			SharedPreferences prefs = getSharedPreferences(
				UiConstants.PREFS_BACK_ACTIVITY_PREF_NAME,
				MODE_PRIVATE);
			String backActivity = prefs.getString(
				UiConstants.PREFS_BACK_ACTIVITY_NAME,
				null);

			intent = new Intent(this, GlucoPrefsActivity.class);
			intent.putExtra(UiConstants.PREFS_BACK_ACTIVITY_NAME, backActivity);
			startActivity(intent);
			break;
		}
		return true;
	}
}
