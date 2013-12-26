package com.glucobutler.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.glucobutler.R;
import com.glucobutler.model.GlucoUnit;
import com.glucobutler.model.TimeOfDay;
import com.glucobutler.provider.GlucoValues;
import com.glucobutler.ui.prefs.FactorsPrefActivity;
import com.glucobutler.ui.prefs.GlucoPrefsActivity;

public class EditEntryActivity extends Activity
{
	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR);
			int monthOfYear = cal.get(Calendar.MONTH);
			int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

			return new DatePickerDialog(getActivity(), this, year, monthOfYear, dayOfMonth);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			EditEntryActivity activity = (EditEntryActivity)getActivity();

			Date internalDate = activity.getInternalDate();
			final Calendar cal = Calendar.getInstance();
			cal.setTime(internalDate);

			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, monthOfYear);
			cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			Date date = cal.getTime();

			activity.setFormattedDate(date);
		}
	}

	public static class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);

			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			EditEntryActivity activity = (EditEntryActivity)getActivity();

			Date internalDate = activity.getInternalDate();
			final Calendar cal = Calendar.getInstance();
			cal.setTime(internalDate);

			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Date date = cal.getTime();

			activity.setFormattedTime(date);
		}
	}

	public static final String LOG_TAG = "EditEntryActivity";

	public static final String PREF_KEY_CORRECTION = "pref_correction";
	public static final String PREF_KEY_UNIT = "pref_unit";

	public static final String UNIT_SPINNER_KEY = "unit";
	public static final String TOD_SPINNER_KEY = "tod";

	private final Map<TimeOfDay, Integer> todToSpinnerPos = new HashMap<TimeOfDay, Integer>();

	private Date internalDate = null;
	private GlucoUnit glucoUnit = null;
	private TimeOfDay tod = null;
	private int correctionPref = 0;

    private boolean contentNew;
    private Uri contentUri;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// set layout xml
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_activity_layout);

		// load default vals from prefs
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Map<String, ?> prefsMap = prefs.getAll();

		// correction pref
		String correctionStr = (String)prefsMap.get(PREF_KEY_CORRECTION);
		if (correctionStr != null) {
			try {
				this.correctionPref = Integer.parseInt(correctionStr);
			} catch (Exception e) {
				Log.d(LOG_TAG, "could not parse correction pref: " + correctionStr);
			}
		}

		// unit pref
		String unitStr = (String)prefsMap.get(PREF_KEY_UNIT);
		GlucoUnit unit = GlucoUnit.MG_DL;
		if (unitStr != null) {
			try {
				unit = GlucoUnit.fromInt(Integer.parseInt(unitStr));
			} catch (Exception e) {
				Log.d(LOG_TAG, "could not parse unit pref: " + unitStr);
			}
		}

		// internal date
		internalDate = new Date();

		// allow navi to list with action bar icon
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// content stuff
		initContentAndData();

		// init UI elements/listeners
		// TODO make sure that listeners don't overwrite values from DB at initing
		initDateTime(internalDate);
		initUnitsSpinner(unit);
		initTimeOfDaySpinner(findTimeOfDay(getInternalDate()));
		initChangeListeners();
	}

	protected void initContentAndData()
	{
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
        	contentNew = false;
            contentUri = intent.getData();
        } else {
        	contentNew = true;
            contentUri = GlucoValues.CONTENT_URI_LIST;
        }
        Log.d(LOG_TAG, "action: " + action);
        Log.d(LOG_TAG, "URI: " + contentUri);

		// data stuff
        if (!contentNew) {
			ContentResolver contentResolver = getContentResolver();
			Cursor cursor = contentResolver.query(contentUri, null, null, null, null);
			initByCursor(cursor);
        }
	}

	protected void initByCursor(Cursor cursor)
	{
		cursor.moveToFirst();

		int colIndex = cursor.getColumnIndex(GlucoValues.COL_NAME_TIMESTAMP);
		long timestamp = cursor.getLong(colIndex);
		this.internalDate = new Date(timestamp);

		colIndex = cursor.getColumnIndex(GlucoValues.COL_NAME_GLUCO_VAL);
		int glucoVal = cursor.getInt(colIndex);
		viewValFromInt(R.id.valueInput, glucoVal);

		colIndex = cursor.getColumnIndex(GlucoValues.COL_NAME_TIME_OF_DAY);
		this.tod = TimeOfDay.fromIdStr(cursor.getString(colIndex));

		colIndex = cursor.getColumnIndex(GlucoValues.COL_NAME_EATEN_UNITS);
		int eatenUnits = cursor.getInt(colIndex);
		viewValFromInt(R.id.eatenUnitsInput, eatenUnits);

		colIndex = cursor.getColumnIndex(GlucoValues.COL_NAME_FACTOR);
		float factor = cursor.getFloat(colIndex);
		viewValFromFactor(factor);

		colIndex = cursor.getColumnIndex(GlucoValues.COL_NAME_CORRECTION);
		int correction = cursor.getInt(colIndex);
		viewValFromInt(R.id.correctionInput, correction);

		colIndex = cursor.getColumnIndex(GlucoValues.COL_NAME_CASTED_UNITS);
		int castedUnits = cursor.getInt(colIndex);
		viewValFromInt(R.id.castedUnitsInput, castedUnits);

		colIndex = cursor.getColumnIndex(GlucoValues.COL_NAME_COMMENT);
		String comment = cursor.getString(colIndex);
		viewValFromString(R.id.commentInput, comment);

		colIndex = cursor.getColumnIndex(GlucoValues.COL_NAME_UNIT);
		this.glucoUnit = GlucoUnit.fromIdStr(cursor.getString(colIndex));
	}

	protected void setFormattedDate(Date date)
	{
		java.text.DateFormat dateFormat = DateFormat.getDateFormat(this);
		String formated = dateFormat.format(date);

		TextView timeEdit = (TextView)findViewById(R.id.dateInput);
		timeEdit.setText(formated);

		internalDate = date;
		Log.d(LOG_TAG, "setting internal date: " + date);
	}

	protected void setFormattedTime(Date date)
	{
		java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);
		String formated = timeFormat.format(date);

		TextView timeEdit = (TextView)findViewById(R.id.timeInput);
		timeEdit.setText(formated);

		internalDate = date;
		Log.d(LOG_TAG, "setting internal time: " + date);

		// update TOD
		TimeOfDay newTod = findTimeOfDay(internalDate);
		updateTimeOfDaySpinner(newTod);
	}

	protected void initDateTime(Date initVal)
	{
		// create date input
		View dateEdit = findViewById(R.id.dateInput);
		dateEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				DialogFragment dateDiagFragment = new DatePickerFragment();
				dateDiagFragment.show(ft, "date_dialog");
			}
		});

		// create time input
		View timeEdit = findViewById(R.id.timeInput);
		timeEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				DialogFragment timeDiagFragment = new TimePickerFragment();
				timeDiagFragment.show(ft, "time_dialog");
			}
		});

		// set initial date/time values
		setFormattedDate(initVal);
		setFormattedTime(initVal);
	}

	protected void initUnitsSpinner(GlucoUnit initVal)
	{
		List<Map<String, Object>> unitAdapterData = new ArrayList<Map<String,Object>>();
		final Map<String, GlucoUnit> displayNameToEnum = new HashMap<String, GlucoUnit>();
		final Map<GlucoUnit, Integer> enumToPos = new HashMap<GlucoUnit, Integer>();

		Map<String, Object> mgdlMap = new HashMap<String, Object>();
		String mgdlText = getText(R.string.mg_dl).toString();
		mgdlMap.put(UNIT_SPINNER_KEY, mgdlText);
		unitAdapterData.add(mgdlMap);
		displayNameToEnum.put(mgdlText, GlucoUnit.MG_DL);
		enumToPos.put(GlucoUnit.MG_DL, Integer.valueOf(0));

		Map<String, Object> mmolMap = new HashMap<String, Object>();
		String mmolText = getText(R.string.mmo_l).toString();
		mmolMap.put(UNIT_SPINNER_KEY, mmolText);
		unitAdapterData.add(mmolMap);
		displayNameToEnum.put(mmolText, GlucoUnit.MMOL_L);
		enumToPos.put(GlucoUnit.MMOL_L, Integer.valueOf(1));

		SimpleAdapter unitAdapter = new SimpleAdapter(
				this,
				unitAdapterData,
				android.R.layout.simple_spinner_item,
				new String[]{UNIT_SPINNER_KEY},
				// default sys resource to display items
				new int[] {android.R.id.text1});
		Spinner unitSpinner = (Spinner)findViewById(R.id.unitSpinner);
		unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		unitSpinner.setAdapter(unitAdapter);

		// init val
		unitSpinner.setSelection(enumToPos.get(initVal).intValue());
		// not necessary to set init internal val, as listener is invoked at start
		//this.glucoUnit = initVal;

		unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				final Map<String, String> data =
					(Map<String, String>) parent.getItemAtPosition(pos);

				String displayName = data.get(UNIT_SPINNER_KEY);
				GlucoUnit glucoUnit = displayNameToEnum.get(displayName);

				EditEntryActivity.this.glucoUnit = glucoUnit;
				Log.d(LOG_TAG, "setting gluco unit: " + glucoUnit);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	protected void initTimeOfDaySpinner(TimeOfDay initialTod)
	{
		List<Map<String, Object>> todAdapterData = new ArrayList<Map<String,Object>>();
		final Map<String, TimeOfDay> displayNameToEnum = new HashMap<String, TimeOfDay>();

		int i = 0;
		for (TimeOfDayTrans todt : TimeOfDayTrans.values())
		{
			Map<String, Object> map = new HashMap<String, Object>();
			String text = getText(todt.getTextId()).toString();
			map.put(TOD_SPINNER_KEY, text);
			todAdapterData.add(map);
			displayNameToEnum.put(text, todt.getTimeOfDay());
			todToSpinnerPos.put(todt.getTimeOfDay(), Integer.valueOf(i));
			i++;
		}

		SimpleAdapter todAdapter = new SimpleAdapter(
			this,
			todAdapterData,
			android.R.layout.simple_spinner_item,
			new String[]{TOD_SPINNER_KEY},
			// default sys resource to display items
			new int[] {android.R.id.text1});
		Spinner todSpinner = (Spinner)findViewById(R.id.timeOfDaySpinner);
		todAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		todSpinner.setAdapter(todAdapter);

		// initial val
		todSpinner.setSelection(todToSpinnerPos.get(initialTod));

		todSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				final Map<String, String> data =
					(Map<String, String>) parent.getItemAtPosition(pos);

				String displayName = data.get(TOD_SPINNER_KEY);
				TimeOfDay tod = displayNameToEnum.get(displayName);

				EditEntryActivity.this.tod = tod;

				// update factor
				EditText factorInput = (EditText)findViewById(R.id.factorInput);
				String factorVal = findFactorInPrefs(tod);
				factorInput.setText(factorVal);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	protected void updateTimeOfDaySpinner(TimeOfDay tod)
	{
		Spinner todSpinner = (Spinner)findViewById(R.id.timeOfDaySpinner);
		// happens on start up
		if (!todToSpinnerPos.isEmpty()) {
			todSpinner.setSelection(todToSpinnerPos.get(tod));
		}
	}

	protected void initChangeListeners()
	{
		EditText valueView = (EditText)findViewById(R.id.valueInput);
		valueView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				EditText correctionView = (EditText)findViewById(R.id.correctionInput);
				try {
					int val = Integer.parseInt(s.toString());
					int correctionVal = calcCorrection(val);
					if (correctionVal > 0) {
						// note: another change event is fired here
						correctionView.setText(String.valueOf(correctionVal));
					} else {
						correctionView.setText("");
					}
				} catch (Exception e) {
					Log.d(LOG_TAG, "could not parse gluco val: " + s);
					correctionView.setText("");
				}
			}
		});

		TextWatcher updateCastedWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				calcAndSetCastedUnits();
			}
		};

		EditText eatenView = (EditText)findViewById(R.id.eatenUnitsInput);
		eatenView.addTextChangedListener(updateCastedWatcher);

		EditText factorView = (EditText)findViewById(R.id.factorInput);
		factorView.addTextChangedListener(updateCastedWatcher);

		EditText correctionView = (EditText)findViewById(R.id.correctionInput);
		correctionView.addTextChangedListener(updateCastedWatcher);
	}

	protected TimeOfDay findTimeOfDay(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int hour = cal.get(Calendar.HOUR_OF_DAY);

		if (hour > 0 && hour <= 12) {
			return TimeOfDay.MORNING;
		}
		if (hour <= 16) {
			return TimeOfDay.NOON;
		}
		if (hour <= 21) {
			return TimeOfDay.EVENING;
		}
		return TimeOfDay.NIGHT;
	}

	protected String findFactorInPrefs(TimeOfDay tod)
	{
		SharedPreferences prefs = getSharedPreferences(
				UiConstants.PREFS_FACTORS_NAME,
				MODE_PRIVATE);

		Float factor = FactorsPrefActivity.readFactorPref(prefs, tod);

		if (factor == null) {
			// TODO read next / prev factor
		}

		return FactorsPrefActivity.factorValueToString(factor);
	}

	protected int calcCorrection(int glucoValue)
	{
		if (this.correctionPref == 0) {
			return 0;
		}

		// TODO correction calc for mmo/l
		if (this.glucoUnit == GlucoUnit.MMOL_L) {
			return 0;
		}

		// note: 100 and 140 are mg/dl specific
		if (glucoValue - correctionPref < 100) {
			return 0;
		}

		if (glucoValue <= 140) {
			return 0;
		}

		float tmp = ((float)glucoValue - 100) / correctionPref;
		return Math.round(tmp);
	}

	protected void calcAndSetCastedUnits()
	{
		EditText eatenView = (EditText)findViewById(R.id.eatenUnitsInput);
		EditText correctionView = (EditText)findViewById(R.id.correctionInput);
		EditText castedView = (EditText)findViewById(R.id.castedUnitsInput);

		int eaten = 0;
		try {
			eaten = Integer.parseInt(eatenView.getText().toString());
		} catch (Exception e) {
			// ignore
		}

		if (eaten == 0) {
			castedView.setText("");
			return;
		}

		Float factorObj = viewValAsFactor();
		float factor = factorObj != null
			? factorObj.floatValue()
			: 1.0f;

		int correction = 0;
		try {
			correction = Integer.parseInt(correctionView.getText().toString());
		} catch (Exception e) {
			// ignore
		}

		int casted = Math.round(eaten * factor + correction);
		castedView.setText(String.valueOf(casted));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.edit_activity_menu, menu);

		// hide / show delete item
		MenuItem delItem = menu.findItem(R.id.menu_delete);
		boolean visible = delItem.isVisible();
		if (contentNew && visible) {
			delItem.setVisible(false);
		} else if (!contentNew && !visible) {
			delItem.setVisible(true);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		Intent intent = null;
		switch (item.getItemId())
		{
		case android.R.id.home:
			intent = new Intent(this, ListEntriesActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_settings:
			intent = new Intent(this, GlucoPrefsActivity.class);
			intent.putExtra(
				UiConstants.PREFS_BACK_ACTIVITY_NAME,
				UiConstants.PREFS_BACK_ACTIVITY_EDIT);
			startActivity(intent);
			break;
		case R.id.menu_save:
			save();
			intent = new Intent(this, ListEntriesActivity.class);
			intent.setData(GlucoValues.CONTENT_URI_LIST);
			startActivity(intent);
			break;
		case R.id.menu_delete:
			delete();
			intent = new Intent(this, ListEntriesActivity.class);
			intent.setData(GlucoValues.CONTENT_URI_LIST);
			startActivity(intent);
			break;
		}

		return true;
	}

	protected Date getInternalDate() {
		return internalDate;
	}

	protected void save()
	{
		ContentValues values = new ContentValues();
		values.put(
			GlucoValues.COL_NAME_TIMESTAMP,
			Long.valueOf(internalDate.getTime()));
		values.put(
			GlucoValues.COL_NAME_GLUCO_VAL,
			viewValAsInteger(R.id.valueInput));
		values.put(
			GlucoValues.COL_NAME_TIME_OF_DAY,
			tod.getIdStr());
		values.put(
			GlucoValues.COL_NAME_EATEN_UNITS,
			viewValAsInteger(R.id.eatenUnitsInput));
		values.put(
			GlucoValues.COL_NAME_FACTOR,
			viewValAsFactor());
		values.put(
			GlucoValues.COL_NAME_CORRECTION,
			viewValAsInteger(R.id.correctionInput));
		values.put(
			GlucoValues.COL_NAME_CASTED_UNITS,
			viewValAsInteger(R.id.castedUnitsInput));
		values.put(
			GlucoValues.COL_NAME_COMMENT,
			viewValAsString(R.id.commentInput));
		values.put(
			GlucoValues.COL_NAME_UNIT,
			glucoUnit.getIdStr());

		if (contentNew) {
			getContentResolver().insert(contentUri, values);
		} else {
			getContentResolver().update(contentUri, values, null, null);
		}
	}

	protected void delete()
	{
		if (!contentNew) {
			getContentResolver().delete(contentUri, null, null);
		}
	}

	protected Integer viewValAsInteger(int viewId)
	{
		EditText view = (EditText)findViewById(viewId);
		String text = view.getText().toString();
		try {
			return Integer.valueOf(text);
		} catch (Exception e) {
			return null;
		}
	}

	protected String viewValAsString(int viewId)
	{
		EditText view = (EditText)findViewById(viewId);
		String text = view.getText().toString();
		return text.isEmpty()
			? null
			: text;
	}

	protected Float viewValAsFactor()
	{
		EditText factorView = (EditText)findViewById(R.id.factorInput);
		String factorStr = factorView.getText().toString();
		return FactorsPrefActivity.factorValueFromString(factorStr);
	}

	protected void viewValFromInt(int viewId, int val)
	{
		EditText view = (EditText)findViewById(viewId);
		view.setText(String.valueOf(val));
	}

	protected void viewValFromString(int viewId, String val)
	{
		EditText view = (EditText)findViewById(viewId);
		view.setText(val);
	}

	protected void viewValFromFactor(float val)
	{
		EditText view = (EditText)findViewById(R.id.factorInput);
		view.setText(FactorsPrefActivity.factorValueToString(val));
	}
}
