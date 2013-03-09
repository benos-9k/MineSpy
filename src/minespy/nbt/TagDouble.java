package minespy.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TagDouble extends Tag {

	public static final byte TAG_ID = 6;
	public static final String TYPENAME = "TAG_Double";

	public static class Parser extends Tag.Parser {

		public Parser() {
			super(TAG_ID, TYPENAME);
		}

		@Override
		protected Tag parsePayload(String name, DataInput in) throws IOException {
			double val = in.readDouble();
			return new TagDouble(name, val);
		}

		@Override
		public Class<?> payloadClass() {
			return Double.class;
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
			return new TagDouble(tagname, t);
		}

	}

	private double m_val;

	public <T> TagDouble(String name_, T val_) {
		super(TAG_ID, name_);
		set(val_);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get() {
		return (T) ((Double) m_val);
	}

	@Override
	public double getDouble() {
		return m_val;
	}

	@Override
	public <T> void set(T t) {
		if (t == null) throw new NullPointerException();
		try {
			if (t instanceof String) {
				try {
					m_val = Double.parseDouble((String) t);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			} else {
				m_val = ((Number) t).doubleValue();
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected void writePayload(DataOutput out) throws IOException {
		out.writeDouble(m_val);
	}

	@Override
	public String toString() {
		return super.toString() + " value=" + m_val;
	}

	@Override
	public TagDouble clone() {
		return (TagDouble) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(m_val);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		TagDouble other = (TagDouble) obj;
		if (Double.doubleToLongBits(m_val) != Double.doubleToLongBits(other.m_val)) return false;
		return true;
	}

}