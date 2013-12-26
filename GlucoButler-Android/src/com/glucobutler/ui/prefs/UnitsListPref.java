package com.glucobutler.ui.prefs;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.glucobutler.R;
import com.glucobutler.model.GlucoUnit;

public class UnitsListPref extends ListPreference
{
	public UnitsListPref(Context context)
	{
		super(context);
	}

	public UnitsListPref(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	protected View onCreateDialogView()
	{
		CharSequence[] entries = new CharSequence[2];
		String[] values = new String[2];

		entries[0] = getContext().getText(R.string.mg_dl);
		entries[1] = getContext().getText(R.string.mmo_l);

		values[0] = String.valueOf(GlucoUnit.MG_DL.toInt());
		values[1] = String.valueOf(GlucoUnit.MMOL_L.toInt());

		ListView view = new ListView(getContext());
        setEntries(entries);
        setEntryValues(values);

        // init
        String currentStr = getPersistedString(values[0]);
        if (values[0].equals(currentStr)) {
        	setValueIndex(0);
        } else {
        	setValueIndex(1);
        }

        return view;
	}
}
