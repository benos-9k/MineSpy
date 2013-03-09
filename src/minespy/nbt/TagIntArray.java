package minespy.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

public class TagIntArray extends Tag {

	public static final byte TAG_ID = 11;
	public static final String TYPENAME = "TAG_Int_Array";

	public static class Parser extends Tag.Parser {

		public Parser() {
			super(TAG_ID, TYPENAME);
		}

		@Override
		protected Tag parsePayload(String name, DataInput in) throws IOException {
			int length = in.readInt();
			if (length < 1) throw new IOException("TAG_Int_Array (" + name + "): negative length.");
			int[] data = null;
			try {
				data = new int[length];
			} catch (OutOfMemoryError e) {
				throw new IOException("TAG_Int_Array (" + name + "): unable to allocate space for " + length, e);
			}
			for (int i = 0; i < length; i++) {
				try {
					data[i] = in.readInt();
				} catch (EOFException e) {
					throw new EOFException("TAG_Int_Array (" + name + "): read failed, " + i + " / " + length
							+ " ints read.");
				}
			}
			return new TagIntArray(name, data);
		}

		@Override
		public Class<?> payloadClass() {
			return int[].class;
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
				return new TagIntArray(tagname, (int[]) t);
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		}

	}

	int[] m_data;

	/**
	 * Construct a TagIntArray by reference to an <code>int[]</code>.
	 * 
	 * @param name_
	 *            The name for this tag.
	 * @param data_
	 *            The <code>int[]</code> to reference.
	 * @throws NullPointerException
	 *             If <code>data_</code> is null.
	 */
	public TagIntArray(String name_, int[] data_) {
		super(TAG_ID, name_);
		if (data_ == null) throw new NullPointerException();
		m_data = data_;
	}

	/**
	 * @return The <code>int[]</code> referenced by this tag.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get() {
		return (T) m_data;
	}

	/**
	 * Set the <code>int[]</code> referenced by this tag.
	 * 
	 * @param t
	 *            The <code>int[]</code> to set.
	 * @throws NullPointerException
	 *             If <code>t</code> is null.
	 * @throws IllegalArgumentException
	 *             If <code>t</code> is not a <code>int[]</code>.
	 */
	@Override
	public <T> void set(T t) {
		if (t == null) throw new NullPointerException();
		try {
			m_data = (int[]) t;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected void writePayload(DataOutput out) throws IOException {
		out.writeInt(m_data.length);
		for (int i = 0; i < m_data.length; i++) {
			out.writeInt(m_data[i]);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " length=" + m_data.length;
	}

	@Override
	public TagIntArray clone() {
		TagIntArray t = (TagIntArray) super.clone();
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
		TagIntArray other = (TagIntArray) obj;
		if (!Arrays.equals(m_data, other.m_data)) return false;
		return true;
	}

}
