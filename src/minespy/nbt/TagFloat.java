package minespy.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TagFloat extends Tag {

	public static final byte TAG_ID = 5;
	public static final String TYPENAME = "TAG_Float";

	public static class Parser extends Tag.Parser {

		public Parser() {
			super(TAG_ID, TYPENAME);
		}

		@Override
		protected Tag parsePayload(String name, DataInput in) throws IOException {
			float val = in.readFloat();
			return new TagFloat(name, val);
		}

		@Override
		public Class<?> payloadClass() {
			return Float.class;
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
			return new TagFloat(tagname, t);
		}

	}

	private float m_val;

	public <T> TagFloat(String name_, T val_) {
		super(TAG_ID, name_);
		set(val_);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get() {
		return (T) ((Float) m_val);
	}

	@Override
	public float getFloat() {
		return m_val;
	}

	@Override
	public <T> void set(T t) {
		if (t == null) throw new NullPointerException();
		try {
			if (t instanceof String) {
				try {
					m_val = Float.parseFloat((String) t);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			} else {
				m_val = ((Number) t).floatValue();
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected void writePayload(DataOutput out) throws IOException {
		out.writeFloat(m_val);
	}

	@Override
	public String toString() {
		return super.toString() + " value=" + m_val;
	}

	@Override
	public TagFloat clone() {
		return (TagFloat) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Float.floatToIntBits(m_val);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		TagFloat other = (TagFloat) obj;
		if (Float.floatToIntBits(m_val) != Float.floatToIntBits(other.m_val)) return false;
		return true;
	}

}