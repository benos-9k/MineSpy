package minespy.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TagByte extends Tag {

	public static final byte TAG_ID = 1;
	public static final String TYPENAME = "TAG_Byte";

	public static class Parser extends Tag.Parser {

		public Parser() {
			super(TAG_ID, TYPENAME);
		}

		@Override
		protected Tag parsePayload(String name, DataInput in) throws IOException {
			byte val = in.readByte();
			return new TagByte(name, val);
		}

		@Override
		public Class<?> payloadClass() {
			return Byte.class;
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
			return new TagByte(tagname, t);
		}

	}

	private byte m_val;

	public <T> TagByte(String name_, T val_) {
		super(TAG_ID, name_);
		set(val_);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get() {
		return (T) ((Byte) m_val);
	}

	@Override
	public boolean getBool() {
		return m_val != 0;
	}

	@Override
	public byte getByte() {
		return m_val;
	}

	@Override
	public <T> void set(T t) {
		if (t == null) throw new NullPointerException();
		try {
			// allow setting from boolean
			if (t instanceof Boolean) {
				m_val = ((Boolean) t) ? (byte) 1 : (byte) 0;
			} else if (t instanceof String) {
				try {
					m_val = Byte.parseByte((String) t);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			} else {
				m_val = ((Number) t).byteValue();
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected void writePayload(DataOutput out) throws IOException {
		out.writeByte(m_val);
	}

	@Override
	public String toString() {
		return super.toString() + " value=" + m_val;
	}

	@Override
	public TagByte clone() {
		return (TagByte) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + m_val;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		TagByte other = (TagByte) obj;
		if (m_val != other.m_val) return false;
		return true;
	}

}
