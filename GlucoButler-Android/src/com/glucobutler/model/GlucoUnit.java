package com.glucobutler.model;

import java.util.HashMap;
import java.util.Map;

import android.util.SparseArray;

public enum GlucoUnit
{
	MG_DL(1),
	MMOL_L(2);

	private final int id;
	private final String idStr;

	private GlucoUnit(int id)
	{
		this.id = id;
		this.idStr = String.valueOf(id);
	}

	public int toInt()
	{
		return id;
	}

	private static final SparseArray<GlucoUnit> ID_TO_ENUM;
	private static final Map<String, GlucoUnit> IDSTR_TO_ENUM;

	static {
		ID_TO_ENUM = new SparseArray<GlucoUnit>();
		IDSTR_TO_ENUM = new HashMap<String, GlucoUnit>();
		for (GlucoUnit glucoUnit : GlucoUnit.values()) {
			ID_TO_ENUM.put(Integer.valueOf(glucoUnit.id), glucoUnit);
			IDSTR_TO_ENUM.put(glucoUnit.idStr, glucoUnit);
		}
	}

	public static GlucoUnit fromInt(int id)
	{
		return fromInt(Integer.valueOf(id));
	}

	public static GlucoUnit fromInt(Integer id)
	{
		return ID_TO_ENUM.get(id);
	}

	public static GlucoUnit fromIdStr(String id)
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
