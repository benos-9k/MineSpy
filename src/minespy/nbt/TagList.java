package minespy.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class TagList extends Tag {

	public static final byte TAG_ID = 9;
	public static final String TYPENAME = "TAG_List";

	public static class Parser extends Tag.Parser {

		public Parser() {
			super(TAG_ID, TYPENAME);
		}

		@Override
		protected Tag parsePayload(String name, DataInput in) throws IOException {
			byte tagid = in.readByte();
			int size = in.readInt();
			TagList ret = new TagList(name, tagid);
			Tag.Parser parser = Tag.getTagParser(tagid);
			for (int i = 0; i < size; i++) {
				ret.add(parser.parsePayload("", in));
			}
			return ret;
		}

		@Override
		public Class<?> payloadClass() {
			return null;
		}

	}

	private static class ListImpl extends ArrayList<Tag> {

		private static final long serialVersionUID = 1L;

		private final byte m_tagid;

		public ListImpl(byte tagid_) {
			m_tagid = tagid_;
		}

		@Override
		public boolean add(Tag t) {
			if (t.tagID() != m_tagid)
				throw new IllegalArgumentException("Cannot add TAG[id=" + t.tagID() + "] to TAG_List[id=" + m_tagid
						+ "]");
			return super.add(t);
		}

	}

	private final byte m_data_tagid;
	private ListImpl m_data;

	public TagList(String name_, byte data_tagid_) {
		super(TAG_ID, name_);
		m_data_tagid = data_tagid_;
		m_data = new ListImpl(m_data_tagid);
	}

	@Override
	public int size() {
		return m_data.size();
	}

	@Override
	public Tag child(int i) {
		return m_data.get(i);
	}

	@Override
	public <T> T get() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void set(T t) {
		if (t == null) throw new NullPointerException();
		try {
			Iterable<?> l = (Iterable<?>) t;
			// check tag types
			for (Object o : l) {
				Tag tag = (Tag) o;
				if (tag.tagID() != m_data_tagid) {
					throw new IllegalArgumentException("Cannot add tag[id=" + tag.tagID() + "] to list[id="
							+ m_data_tagid + "]");
				}
			}
			// copy tags
			m_data.clear();
			for (Object o : l) {
				m_data.add((Tag) o);
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public <T> void set(int i, T t) {
		if (i > size()) {
			throw new IndexOutOfBoundsException();
		} else if (i == size()) {
			Tag.Wrapper wrapper = Tag.getTagWrapper(m_data_tagid);
			if (wrapper == null) throw new IllegalStateException("No wrapper for tag id " + m_data_tagid);
			add(wrapper.wrap("", t));
		} else if (i > 0) {
			child(i).set(t);
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void add(int i, Tag tag) {
		if (tag == null) throw new NullPointerException();
		m_data.add(i, tag);
	}

	@Override
	public void add(Tag tag) {
		if (tag == null) throw new NullPointerException();
		m_data.add(tag);
	}

	@Override
	public Tag remove(int i) {
		try {
			return m_data.remove(i);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public Tag replace(int i, Tag tag) {
		if (tag == null) throw new NullPointerException();
		if (i == size()) {
			m_data.add(tag);
			return null;
		} else {
			return m_data.set(i, tag);
		}
	}

	@Override
	public void clear() {
		m_data.clear();
	}

	@Override
	public Iterator<Tag> iterator() {
		return m_data.iterator();
	}

	@Override
	protected void writePayload(DataOutput out) throws IOException {
		out.writeByte(m_data_tagid);
		out.writeInt(m_data.size());
		for (Tag t : m_data) {
			t.writePayload(out);
		}
	}

	@Override
	public String toString() {
		Tag.Parser p = Tag.getTagParser(m_data_tagid);
		return super.toString() + " datatype=" + p.typeName() + "(" + p.tagID() + ")" + " size=" + m_data.size();
	}

	@Override
	public TagList clone() {
		TagList t = (TagList) super.clone();
		t.m_data = new ListImpl(t.m_data_tagid);
		for (Tag tag : this.m_data) {
			t.m_data.add(tag.clone());
		}
		return t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((m_data == null) ? 0 : m_data.hashCode());
		result = prime * result + m_data_tagid;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		TagList other = (TagList) obj;
		if (m_data == null) {
			if (other.m_data != null) return false;
		} else if (!m_data.equals(other.m_data)) return false;
		if (m_data_tagid != other.m_data_tagid) return false;
		return true;
	}

}
