package com.glucobutler.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.glucobutler.R;
import com.glucobutler.model.TimeOfDay;

public enum TimeOfDayTrans
{
	MORNING(TimeOfDay.MORNING, R.string.morning),
	FORENOON(TimeOfDay.FORENOON, R.string.forenoon),
	NOON(TimeOfDay.NOON, R.string.noon),
	EARLY_AFTERNOON(TimeOfDay.EARLY_AFTERNOON, R.string.early_afternoon),
	LATE_AFTERNOON(TimeOfDay.LATE_AFTERNOON, R.string.late_afternoon),
	EVENING(TimeOfDay.EVENING, R.string.evening),
	LATE_EVENING(TimeOfDay.LATE_EVENING, R.string.late_evening),
	NIGHT(TimeOfDay.NIGHT, R.string.night);

	private final int textId;
	private final TimeOfDay timeOfDay;

	private TimeOfDayTrans(TimeOfDay timeOfDay, int textId)
	{
		this.timeOfDay = timeOfDay;
		this.textId = textId;
	}

	public TimeOfDay getTimeOfDay()
	{
		return timeOfDay;
	}

	public int getTextId()
	{
		return textId;
	}

	private static final Map<TimeOfDay, TimeOfDayTrans> TOD_TO_TODT;
	static {
		Map<TimeOfDay, TimeOfDayTrans> tmp = new HashMap<TimeOfDay, TimeOfDayTrans>();
		for (TimeOfDayTrans todt : values()) {
			tmp.put(todt.getTimeOfDay(), todt);
		}
		TOD_TO_TODT = Collections.unmodifiableMap(tmp);
	}

	public static TimeOfDayTrans fromTod(TimeOfDay tod) {
		return TOD_TO_TODT.get(tod);
	}
}
