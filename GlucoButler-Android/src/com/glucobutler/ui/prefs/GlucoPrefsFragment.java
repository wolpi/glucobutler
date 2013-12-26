package com.glucobutler.ui.prefs;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.glucobutler.R;

public class GlucoPrefsFragment extends PreferenceFragment
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}
