package minespy.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TagShort extends Tag {

	public static final byte TAG_ID = 2;
	public static final String TYPENAME = "TAG_Short";

	public static class Parser extends Tag.Parser {

		public Parser() {
			super(TAG_ID, TYPENAME);
		}

		@Override
		protected Tag parsePayload(String name, DataInput in) throws IOException {
			short val = in.readShort();
			return new TagShort(name, val);
		}

		@Override
		public Class<?> payloadClass() {
			return Short.class;
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
			return new TagShort(tagname, t);
		}

	}

	private short m_val;

	public <T> TagShort(String name_, T val_) {
		super(TAG_ID, name_);
		set(val_);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get() {
		return (T) ((Short) m_val);
	}

	@Override
	public short getShort() {
		return m_val;
	}

	@Override
	public <T> void set(T t) {
		if (t == null) throw new NullPointerException();
		try {
			if (t instanceof String) {
				try {
					m_val = Short.parseShort((String) t);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			} else {
				m_val = ((Number) t).shortValue();
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected void writePayload(DataOutput out) throws IOException {
		out.writeShort(m_val);
	}

	@Override
	public String toString() {
		return super.toString() + " value=" + m_val;
	}

	@Override
	public TagShort clone() {
		return (TagShort) super.clone();
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
		TagShort other = (TagShort) obj;
		if (m_val != other.m_val) return false;
		return true;
	}

}