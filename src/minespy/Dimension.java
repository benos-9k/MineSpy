package minespy;

public enum Dimension {
	OVERWORLD("The Overworld", "overworld"), NETHER("The Nether", "nether"), END("The End", "end");
	
	private final String m_displayname;
	private final String m_filename;
	
	private Dimension(String displayname_, String filename_) {
		m_displayname = displayname_;
		m_filename = filename_;
	}
	
	@Override
	public String toString() {
		return m_displayname;
	}
	
	public String getDisplayName() {
		return m_displayname;
	}
	
	public String getFileName() {
		return m_filename;
	}
}
