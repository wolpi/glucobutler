package com.glucobutler.model;

import java.util.HashMap;
import java.util.Map;

import android.util.SparseArray;

public enum TimeOfDay
{
	MORNING(1),
	FORENOON(2),
	NOON(3),
	EARLY_AFTERNOON(4),
	LATE_AFTERNOON(5),
	EVENING(6),
	LATE_EVENING(7),
	NIGHT(8);

	private final int id;
	private final String idStr;

	private TimeOfDay(int id)
	{
		this.id = id;
		this.idStr = String.valueOf(id);
	}

	public int toInt()
	{
		return id;
	}

	private static final SparseArray<TimeOfDay> ID_TO_ENUM;
	private static final Map<String, TimeOfDay> IDSTR_TO_ENUM;

	static {
		ID_TO_ENUM = new SparseArray<TimeOfDay>();
		IDSTR_TO_ENUM = new HashMap<String, TimeOfDay>();
		for (TimeOfDay timeOfDay : TimeOfDay.values()) {
			ID_TO_ENUM.put(Integer.valueOf(timeOfDay.id), timeOfDay);
			IDSTR_TO_ENUM.put(timeOfDay.idStr, timeOfDay);
		}
	}

	public static TimeOfDay fromInt(int id)
	{
		return fromInt(Integer.valueOf(id));
	}

	public static TimeOfDay fromInt(Integer id)
	{
		return ID_TO_ENUM.get(id);
	}

	public static TimeOfDay fromIdStr(String id)
	{
		return IDSTR_TO_ENUM.get(id);
	}

	public int getId()
	{
		return id;
	}

	public String getIdStr()
	{
		return idStr;
	}
}
