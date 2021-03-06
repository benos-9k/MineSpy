package minespy.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TagString extends Tag {

	public static final byte TAG_ID = 8;
	public static final String TYPENAME = "TAG_String";

	public static class Parser extends Tag.Parser {

		public Parser() {
			super(TAG_ID, TYPENAME);
		}

		@Override
		protected Tag parsePayload(String name, DataInput in) throws IOException {
			String val = in.readUTF();
			return new TagString(name, val);
		}

		@Override
		public Class<?> payloadClass() {
			return String.class;
		}

	}

	public static class Wrapper implements Tag.Wrapper {

		@Override
		public byte tagID() {
			return TAG_ID;
		}

		@Override
		public <T> Tag wrap(String tagname, T t) {
			if (t == null) throw new NullPointerException();
			return new TagString(tagname, t.toString());
		}

	}

	private String m_val;

	public TagString(String name_, String val) {
		super(TAG_ID, name_);
		m_val = val;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get() {
		return (T) m_val;
	}

	@Override
	public String getString() {
		return m_val;
	}

	@Override
	public <T> void set(T t) {
		if (t == null) throw new NullPointerException();
		try {
			m_val = t.toString();
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected void writePayload(DataOutput out) throws IOException {
		out.writeUTF(m_val);
	}

	@Override
	public String toString() {
		return super.toString() + " value=" + m_val;
	}

	@Override
	public TagString clone() {
		return (TagString) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((m_val == null) ? 0 : m_val.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		TagString other = (TagString) obj;
		if (m_val == null) {
			if (other.m_val != null) return false;
		} else if (!m_val.equals(other.m_val)) return false;
		return true;
	}

}
