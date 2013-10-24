package minespy.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TagLong extends Tag {

	public static final byte TAG_ID = 4;
	public static final String TYPENAME = "TAG_Long";

	public static class Parser extends Tag.Parser {

		public Parser() {
			super(TAG_ID, TYPENAME);
		}

		@Override
		protected Tag parsePayload(String name, DataInput in) throws IOException {
			long val = in.readLong();
			return new TagLong(name, val);
		}

		@Override
		public Class<?> payloadClass() {
			return Long.class;
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
			return new TagLong(tagname, t);
		}

	}

	private long m_val;

	public <T> TagLong(String name_, T val_) {
		super(TAG_ID, name_);
		set(val_);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get() {
		return (T) ((Long) m_val);
	}

	@Override
	public long getLong() {
		return m_val;
	}

	@Override
	public <T> void set(T t) {
		if (t == null) throw new NullPointerException();
		try {
			if (t instanceof String) {
				try {
					m_val = Long.parseLong((String) t);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			} else {
				m_val = ((Number) t).longValue();
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected void writePayload(DataOutput out) throws IOException {
		out.writeLong(m_val);
	}

	@Override
	public String toString() {
		return super.toString() + " value=" + m_val;
	}

	@Override
	public TagLong clone() {
		return (TagLong) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (m_val ^ (m_val >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		TagLong other = (TagLong) obj;
		if (m_val != other.m_val) return false;
		return true;
	}

}