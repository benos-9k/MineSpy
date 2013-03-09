package minespy.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class TagByteArray extends Tag {

	public static final byte TAG_ID = 7;
	public static final String TYPENAME = "TAG_Byte_Array";

	public static class Parser extends Tag.Parser {

		public Parser() {
			super(TAG_ID, TYPENAME);
		}

		@Override
		protected Tag parsePayload(String name, DataInput in) throws IOException {
			int length = in.readInt();
			if (length < 1) throw new IOException("TAG_Byte_Array (" + name + "): negative length.");
			byte[] data = null;
			try {
				data = new byte[length];
			} catch (OutOfMemoryError e) {
				throw new IOException("TAG_Byte_Array (" + name + "): unable to allocate space for " + length);
			}
			in.readFully(data);
			return new TagByteArray(name, data);
		}

		@Override
		public Class<?> payloadClass() {
			return byte[].class;
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
			try {
				return new TagByteArray(tagname, (byte[]) t);
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		}

	}

	byte[] m_data;

	/**
	 * Construct a TagByteArray by reference to a <code>byte[]</code>.
	 * 
	 * @param name_
	 *            The name for this tag.
	 * @param data_
	 *            The <code>byte[]</code> to reference.
	 * @throws NullPointerException
	 *             If <code>data_</code> is null.
	 */
	public TagByteArray(String name_, byte[] data_) {
		super(TAG_ID, name_);
		if (data_ == null) throw new NullPointerException();
		m_data = data_;
	}

	/**
	 * @return The <code>byte[]</code> referenced by this tag.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get() {
		return (T) m_data;
	}

	/**
	 * Set the <code>byte[]</code> referenced by this tag.
	 * 
	 * @param t
	 *            The <code>byte[]</code> to set.
	 * @throws NullPointerException
	 *             If <code>t</code> is null.
	 * @throws IllegalArgumentException
	 *             If <code>t</code> is not a <code>byte[]</code>.
	 */
	@Override
	public <T> void set(T t) {
		if (t == null) throw new NullPointerException();
		try {
			m_data = (byte[]) t;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected void writePayload(DataOutput out) throws IOException {
		out.writeInt(m_data.length);
		out.write(m_data);
	}

	@Override
	public String toString() {
		return super.toString() + " length=" + m_data.length;
	}

	@Override
	public TagByteArray clone() {
		TagByteArray t = (TagByteArray) super.clone();
		t.m_data = this.m_data.clone();
		return t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(m_data);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		TagByteArray other = (TagByteArray) obj;
		if (!Arrays.equals(m_data, other.m_data)) return false;
		return true;
	}

}
