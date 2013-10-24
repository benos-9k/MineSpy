package minespy;

import java.util.HashMap;
import java.util.Map;

public class Dimension {

	// THIS HAS TO BE BEFORE THE 'ENUM CONSTANTS' BELOW
	// otherwise initialiser errors
	private static final Map<Integer, Dimension> dims = new HashMap<Integer, Dimension>();

	public static final Dimension OVERWORLD = new Dimension("The Overworld", "overworld", 0);
	public static final Dimension NETHER = new Dimension("The Nether", "nether", -1);
	public static final Dimension END = new Dimension("The End", "end", 1);

	public static Dimension fromIndex(int i) {
		Dimension d = dims.get(i);
		if (d == null) return OVERWORLD;
		return d;
	}

	private final String m_displayname;
	private final String m_filename;
	private final int m_index;

	private Dimension(String displayname_, String filename_, int index_) {
		m_displayname = displayname_;
		m_filename = filename_;
		m_index = index_;
		dims.put(m_index, this);
	}

	@Override
	public String toString() {
		return m_displayname;
	}

	public int getIndex() {
		return m_index;
	}

	public String getDisplayName() {
		return m_displayname;
	}

	public String getFileName() {
		return m_filename;
	}
}
