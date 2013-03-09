package minespy.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Parser and Document Object Model for Minecraft's NBT (Named Binary Tag) data structure (version 19133). Allows
 * editing tags and writing to a data output. Implements Iterable < Tag > and Cloneable.<br>
 * <br>
 * Documentation on the format itself can be found <a href=http://www.minecraftwiki.net/wiki/NBT_Format>here</a>.<br>
 * <br>
 * There is currently no protection against constructing a cyclical or otherwise invalid tag graph. Attempting to
 * serialise a tag graph with a cycle (eg. a TagCompound that contains itself) will result in infinite recursion and
 * probably a stack overflow (and also a very large memory usage if writing to an in-memory location).
 * 
 * @author Ben Allen
 * 
 */
public abstract class Tag implements Cloneable, Iterable<Tag> {

	private static final Tag.Parser[] tag_parsers = new Tag.Parser[128];
	private static final Map<Class<?>, Tag.Wrapper> tag_class_wrappers = new HashMap<Class<?>, Tag.Wrapper>();
	private static final Tag.Wrapper[] tag_id_wrappers = new Tag.Wrapper[128];

	static {
		/*
		 * This code has to be here (not in the individual tag classes) because otherwise those tag classes may never
		 * get loaded.
		 */

		// setup default parsers
		addTagParser(new TagByte.Parser());
		addTagParser(new TagShort.Parser());
		addTagParser(new TagInt.Parser());
		addTagParser(new TagLong.Parser());
		addTagParser(new TagFloat.Parser());
		addTagParser(new TagDouble.Parser());
		addTagParser(new TagByteArray.Parser());
		addTagParser(new TagString.Parser());
		addTagParser(new TagList.Parser());
		addTagParser(new TagCompound.Parser());
		addTagParser(new TagIntArray.Parser());

		// and default wrappers
		addTagWrapper(Byte.class, new TagByte.Wrapper());
		addTagWrapper(Boolean.class, new TagByte.Wrapper());
		addTagWrapper(Short.class, new TagShort.Wrapper());
		addTagWrapper(Integer.class, new TagInt.Wrapper());
		addTagWrapper(Long.class, new TagLong.Wrapper());
		addTagWrapper(Float.class, new TagFloat.Wrapper());
		addTagWrapper(Double.class, new TagDouble.Wrapper());
		addTagWrapper(byte[].class, new TagByteArray.Wrapper());
		addTagWrapper(String.class, new TagString.Wrapper());
		addTagWrapper(int[].class, new TagIntArray.Wrapper());
	}

	private static void printIndent(int indent) {
		for (int i = 0; i < indent; i++) {
			System.out.print(" ");
		}
	}

	public static void print(Tag root, int indent, int truncate) {
		printIndent(indent);
		System.out.println(root);
		if (root.size() > 0) {
			printIndent(indent);
			System.out.println("{");
		}
		int i = 0;
		for (Tag t : root) {
			if (i < truncate) {
				print(t, indent + 4, truncate);
				i++;
			} else {
				break;
			}
		}
		if (root.size() > truncate) {
			printIndent(indent);
			System.out.println("[" + root.name() + "] + " + (root.size() - truncate) + " more.");
		}
		if (root.size() > 0) {
			printIndent(indent);
			System.out.println("}");
		}
	}

	public static void print(Tag root, int truncate) {
		print(root, 0, truncate);
	}

	public static void print(Tag root) {
		print(root, 0, 10);
	}

	/**
	 * Add a tag parser. Does not allow an existing parser for a tag id to be overwritten.
	 * 
	 * @param parser
	 *            The parser to add.
	 * @return True if the parser was able to be added, false otherwise.
	 * @throws NullPointerException
	 *             If <code>parser</code> is null.
	 * @throws IllegalArgumentException
	 *             If the tag id of the specified parser is not valid (ie. negative).
	 */
	public static boolean addTagParser(Tag.Parser parser) {
		if (parser == null) throw new NullPointerException();
		if (parser.tagID() < 1) throw new IllegalArgumentException("Invalid tagid.");
		if (tag_parsers[parser.tagID()] == null) {
			tag_parsers[parser.tagID()] = parser;
			return true;
		}
		return false;
	}

	/**
	 * Get tag parser for a tag id.
	 * 
	 * @param tagid
	 *            Tag id to get the parser for.
	 * @return The parser, or null if there is none for the specified tag id.
	 * @throws IllegalArgumentException
	 *             If the tag id is not valid.
	 */
	public static Tag.Parser getTagParser(byte tagid) {
		if (tagid < 1) throw new IllegalArgumentException("Invalid tagid.");
		return tag_parsers[tagid];
	}

	/**
	 * Add a tag wrapper. Does not allow an existing wrapper for a class or tag id to be overwritten, although the same
	 * wrapper can be specified multiple times for different classes and necessarily the same tag id without problem. A
	 * wrapper will only be added for its tag id if it is added for the specified class.
	 * 
	 * @param c
	 *            Class to add a wrapper for.
	 * @param wrapper
	 *            A wrapper object.
	 * @return True if the wrapper was able to be added, false otherwise.
	 * @throws NullPointerException
	 *             If <code>c</code> or <code>wrapper</code> are null.
	 * @throws IllegalArgumentException
	 *             If the tag id of the specified wrapper is not valid (ie. negative).
	 */
	public static boolean addTagWrapper(Class<?> c, Tag.Wrapper wrapper) {
		if (c == null || wrapper == null) throw new NullPointerException();
		if (wrapper.tagID() < 1) throw new IllegalArgumentException("Invalid tagid.");
		if (tag_id_wrappers[wrapper.tagID()] != null && tag_id_wrappers[wrapper.tagID()] != wrapper) {
			// there is a wrapper for this tagid, and it's not this one
			return false;
		}
		if (!tag_class_wrappers.containsKey(c)) {
			tag_id_wrappers[wrapper.tagID()] = wrapper;
			tag_class_wrappers.put(c, wrapper);
			return true;
		}
		return false;
	}

	/**
	 * Get a tag wrapper for a tag id.
	 * 
	 * @param tagid
	 *            Tag id to get the wrapper for.
	 * @return The wrapper, or null if there is none for the specified tag id.
	 * @throws IllegalArgumentException
	 *             If the tag id is not valid.
	 */
	public static Tag.Wrapper getTagWrapper(byte tagid) {
		if (tagid < 1) throw new IllegalArgumentException("Invalid tagid.");
		return tag_id_wrappers[tagid];
	}

	/**
	 * Wrap an object in a tag. Only wrappers for the exact class, not any superclasses or superinterfaces, will be
	 * considered.
	 * 
	 * @param t
	 *            The object to wrap.
	 * @return A tag whose value represents <code>t</code>.
	 * @throws NullPointerException
	 *             If <code>tagname</code> or <code>t</code> are null.
	 * @throws IllegalStateException
	 *             If there is no wrapper for <code>t.getClass()</code>.
	 * @throws IllegalArgumentException
	 *             If there is a wrapper for <code>t.getClass()</code>, but it fails.
	 */
	public static <T> Tag wrap(String tagname, T t) {
		if (tagname == null || t == null) throw new NullPointerException();
		if (!tag_class_wrappers.containsKey(t.getClass()))
			throw new IllegalStateException("No wrapper for class " + t.getClass().getCanonicalName());
		return tag_class_wrappers.get(t.getClass()).wrap(tagname, t);
	}

	/**
	 * Parse and return a tag in its entirety, using the first byte (tag id) to determine which parser to call.
	 * 
	 * @param in
	 *            Stream to read from.
	 * @return The tag, or null if an end tag (tag id 0).
	 * @throws IOException
	 *             If the stream cannot be read or invalid data is encountered.
	 * @throws IllegalStateException
	 *             If a valid tag id is encountered for which there is no parser.
	 */
	public static Tag parse(DataInput in) throws IOException {
		byte tagid = in.readByte();
		if (tagid == 0) return null;
		try {
			Parser p = getTagParser(tagid);
			if (p == null) throw new IllegalStateException("No parser for tag id " + tagid);
			return p.parse(in);
		} catch (IllegalArgumentException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Base class for tag id specific tag parsers.
	 */
	public static abstract class Parser {

		private final byte m_tagid;
		private final String m_typename;

		protected Parser(byte tagid_, String typename_) {
			if (tagid_ < 1) throw new IllegalArgumentException();
			m_tagid = tagid_;
			m_typename = typename_;
		}

		public final byte tagID() {
			return m_tagid;
		}

		public final String typeName() {
			return m_typename;
		}

		protected abstract Tag parsePayload(String name, DataInput in) throws IOException;

		public Tag parse(DataInput in) throws IOException {
			// tagid byte already parsed
			String name = in.readUTF();
			return parsePayload(name, in);
		}

		/**
		 * @return A Class object representing the payload type of tags generated by this parser, or null if such tags
		 *         do not have a directly accessible payload.
		 */
		public abstract Class<?> payloadClass();

	}

	/**
	 * Interface to allow arbitrary objects to be wrapped in a specific tag type.
	 */
	public static interface Wrapper {

		/**
		 * @return The tag id of tags produced by this wrapper.
		 */
		public byte tagID();

		/**
		 * Wrap an object in a tag.
		 * 
		 * @param tagname
		 *            Name to give newly created tag.
		 * @param t
		 *            Object to wrap.
		 * @return A tag whose value represents that object.
		 * @throws NullPointerException
		 *             If <code>tagname</code> or <code>t</code> are null.
		 * @throws IllegalArgumentException
		 *             If <code>t</code> cannot be wrapped by this wrapper.
		 */
		public <T> Tag wrap(String tagname, T t);

	}

	private static class EmptyIterator implements Iterator<Tag> {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Tag next() {
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private final byte m_tagid;
	private final String m_name;

	protected Tag(byte tagid_, String name_) {
		m_tagid = tagid_;
		m_name = name_;
	}

	/**
	 * @return The type of this tag.
	 */
	public final byte tagID() {
		return m_tagid;
	}

	/**
	 * @return The name of this tag.
	 */
	public final String name() {
		return m_name;
	}

	/**
	 * @return A Class object representing the type of the payload of this tag, or null if this tag does not have a
	 *         directly accessible payload.
	 */
	public final Class<?> payloadClass() {
		return getTagParser(m_tagid).payloadClass();
	}

	/**
	 * @return An iterator over all child tags of this tag.
	 */
	@Override
	public Iterator<Tag> iterator() {
		return new EmptyIterator();
	}

	/**
	 * @return The number of children of this tag.
	 */
	public int size() {
		return 0;
	}

	/**
	 * Get a child of this tag by index.
	 * 
	 * @param i
	 *            Index of the child to get.
	 * @return The tag, or null if there is none.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index.
	 */
	public Tag child(int i) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Short form of <code>child(index0).child(index1)...</code> without the possibility of NullPointerExceptions.
	 * 
	 * @param index0
	 * @param index1
	 * @param indices
	 * @return The tag if found, or null otherwise.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index.
	 */
	public final Tag child(int index0, int index1, int... indices) {
		Tag t = child(index0);
		if (t == null) return null;
		t = t.child(index1);
		for (int i : indices) {
			if (t == null) return null;
			t = t.child(i);
		}
		return t;
	}

	/**
	 * Find the first child of this tag with the specified name, where 'first' is implementation-defined.
	 * 
	 * @param tagname
	 *            Name of the tag to get.
	 * @return The tag if found, or null otherwise.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by name.
	 */
	public Tag child(String tagname) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Short form of <code>child(name0).child(name1)...</code> without the possibility of NullPointerExceptions.
	 * 
	 * @param name0
	 * @param name1
	 * @param names
	 * @return The tag if found, or null otherwise.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by name.
	 */
	public final Tag child(String name0, String name1, String... names) {
		Tag t = child(name0);
		if (t == null) return null;
		t = t.child(name1);
		for (String n : names) {
			if (t == null) return null;
			t = t.child(n);
		}
		return t;
	}

	/**
	 * Get the payload of this tag, attempting to cast it to type T. The <code>Object</code> returned by this method
	 * must be an instance of the <code>Class</code> returned by the method <code>getPayloadClass()</code> on this tag,
	 * which is in all cases the same as that returned by the method <code>getPayloadClass()</code> on the
	 * <code>Tag.Parser</code> that constructed this <code>Tag</code>. Must not return null.
	 * 
	 * @return The payload.
	 * @throws ClassCastException
	 *             If the payload cannot be cast to type T.
	 * @throws UnsupportedOperationException
	 *             If there is no directly accessible payload.
	 */
	public abstract <T> T get();

	/**
	 * Short form of <code>child(i).get()</code>.
	 * 
	 * @param i
	 *            Index of the child tag to get from.
	 * @return The value of the specified child tag.
	 * @throws IndexOutOfBoundsException
	 *             If there is no such child.
	 * @throws ClassCastException
	 *             If the payload cannot be cast to type T.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload.
	 */
	public <T> T get(int i) {
		Tag tag = child(i);
		if (tag == null) throw new IndexOutOfBoundsException("Bad index: " + i);
		return tag.get();
	}

	/**
	 * Short form of <code>child(tagname).get()</code>.
	 * 
	 * @param tagname
	 *            Name of the child tag to get from.
	 * @return The value of the specified child tag.
	 * @throws NoSuchElementException
	 *             If there is no such child.
	 * @throws ClassCastException
	 *             If the payload cannot be cast to type T.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by name or there is no directly accessible payload.
	 */
	public <T> T get(String tagname) {
		Tag tag = child(tagname);
		if (tag == null) throw new NoSuchElementException("Bad tagname: " + tagname);
		return tag.get();
	}

	/**
	 * @return A boolean representation of the payload of this tag.
	 * @throws UnsupportedOperationException
	 *             If there is no directly accessible payload or it cannot be interpreted as a boolean.
	 */
	public boolean getBool() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Short form of <code>child(i).getBool()</code>.
	 * 
	 * @param i
	 *            Index of the child tag to get from.
	 * @return A boolean representation of the payload of the specified child tag.
	 * @throws IndexOutOfBoundsException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a boolean.
	 */
	public boolean getBool(int i) {
		Tag tag = child(i);
		if (tag == null) throw new IndexOutOfBoundsException("Bad index: " + i);
		return tag.getBool();
	}

	/**
	 * Short form of <code>child(tagname).getBool()</code>.
	 * 
	 * @param tagname
	 *            Name of the child tag to get from.
	 * @return A boolean representation of the payload of the specified child tag.
	 * @throws NoSuchElementException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a boolean.
	 */
	public boolean getBool(String tagname) {
		Tag tag = child(tagname);
		if (tag == null) throw new NoSuchElementException("Bad tagname: " + tagname);
		return tag.getBool();
	}

	/**
	 * @return A byte representation of the payload of this tag.
	 * @throws UnsupportedOperationException
	 *             If there is no directly accessible payload or it cannot be interpreted as a byte.
	 */
	public byte getByte() {
		try {
			return this.<Number> get().byteValue();
		} catch (ClassCastException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * Short form of <code>child(i).getByte()</code>.
	 * 
	 * @param i
	 *            Index of the child tag to get from.
	 * @return A byte representation of the payload of the specified child tag.
	 * @throws IndexOutOfBoundsException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a byte.
	 */
	public byte getByte(int i) {
		Tag tag = child(i);
		if (tag == null) throw new IndexOutOfBoundsException("Bad index: " + i);
		return tag.getByte();
	}

	/**
	 * Short form of <code>child(tagname).getByte()</code>.
	 * 
	 * @param tagname
	 *            Name of the child tag to get from.
	 * @return A byte representation of the payload of the specified child tag.
	 * @throws NoSuchElementException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a byte.
	 */
	public byte getByte(String tagname) {
		Tag tag = child(tagname);
		if (tag == null) throw new NoSuchElementException("Bad tagname: " + tagname);
		return tag.getByte();
	}

	/**
	 * @return A short representation of the payload of this tag.
	 * @throws UnsupportedOperationException
	 *             If there is no directly accessible payload or it cannot be interpreted as a short.
	 */
	public short getShort() {
		try {
			return this.<Number> get().shortValue();
		} catch (ClassCastException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * Short form of <code>child(i).getShort()</code>.
	 * 
	 * @param i
	 *            Index of the child tag to get from.
	 * @return A short representation of the payload of the specified child tag.
	 * @throws IndexOutOfBoundsException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a short.
	 */
	public short getShort(int i) {
		Tag tag = child(i);
		if (tag == null) throw new IndexOutOfBoundsException("Bad index: " + i);
		return tag.getShort();
	}

	/**
	 * Short form of <code>child(tagname).getShort()</code>.
	 * 
	 * @param tagname
	 *            Name of the child tag to get from.
	 * @return A short representation of the payload of the specified child tag.
	 * @throws NoSuchElementException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a short.
	 */
	public short getShort(String tagname) {
		Tag tag = child(tagname);
		if (tag == null) throw new NoSuchElementException("Bad tagname: " + tagname);
		return tag.getShort();
	}

	/**
	 * @return An int representation of the payload of this tag.
	 * @throws UnsupportedOperationException
	 *             If there is no directly accessible payload or it cannot be interpreted as an int.
	 */
	public int getInt() {
		try {
			return this.<Number> get().intValue();
		} catch (ClassCastException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * Short form of <code>child(i).getInt()</code>.
	 * 
	 * @param i
	 *            Index of the child tag to get from.
	 * @return An int representation of the payload of the specified child tag.
	 * @throws IndexOutOfBoundsException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as an int.
	 */
	public int getInt(int i) {
		Tag tag = child(i);
		if (tag == null) throw new IndexOutOfBoundsException("Bad index: " + i);
		return tag.getInt();
	}

	/**
	 * Short form of <code>child(tagname).getInt()</code>.
	 * 
	 * @param tagname
	 *            Name of the child tag to get from.
	 * @return An int representation of the payload of the specified child tag.
	 * @throws NoSuchElementException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as an int.
	 */
	public int getInt(String tagname) {
		Tag tag = child(tagname);
		if (tag == null) throw new NoSuchElementException("Bad tagname: " + tagname);
		return tag.getInt();
	}

	/**
	 * @return A long representation of the payload of this tag.
	 * @throws UnsupportedOperationException
	 *             If there is no directly accessible payload or it cannot be interpreted as a long.
	 */
	public long getLong() {
		try {
			return this.<Number> get().longValue();
		} catch (ClassCastException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * Short form of <code>child(i).getLong()</code>.
	 * 
	 * @param i
	 *            Index of the child tag to get from.
	 * @return A long representation of the payload of the specified child tag.
	 * @throws IndexOutOfBoundsException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a long.
	 */
	public long getLong(int i) {
		Tag tag = child(i);
		if (tag == null) throw new IndexOutOfBoundsException("Bad index: " + i);
		return tag.getLong();
	}

	/**
	 * Short form of <code>child(tagname).getLong()</code>.
	 * 
	 * @param tagname
	 *            Name of the child tag to get from.
	 * @return A long representation of the payload of the specified child tag.
	 * @throws NoSuchElementException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a long.
	 */
	public long getLong(String tagname) {
		Tag tag = child(tagname);
		if (tag == null) throw new NoSuchElementException("Bad tagname: " + tagname);
		return tag.getLong();
	}

	/**
	 * @return A float representation of the payload of this tag.
	 * @throws UnsupportedOperationException
	 *             If there is no directly accessible payload or it cannot be interpreted as a float.
	 */
	public float getFloat() {
		try {
			return this.<Number> get().floatValue();
		} catch (ClassCastException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * Short form of <code>child(i).getFloat()</code>.
	 * 
	 * @param i
	 *            Index of the child tag to get from.
	 * @return A float representation of the payload of the specified child tag.
	 * @throws IndexOutOfBoundsException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a float.
	 */
	public float getFloat(int i) {
		Tag tag = child(i);
		if (tag == null) throw new IndexOutOfBoundsException("Bad index: " + i);
		return tag.getFloat();
	}

	/**
	 * Short form of <code>child(tagname).getFloat()</code>.
	 * 
	 * @param tagname
	 *            Name of the child tag to get from.
	 * @return A float representation of the payload of the specified child tag.
	 * @throws NoSuchElementException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a float.
	 */
	public float getFloat(String tagname) {
		Tag tag = child(tagname);
		if (tag == null) throw new NoSuchElementException("Bad tagname: " + tagname);
		return tag.getFloat();
	}

	/**
	 * @return A double representation of the payload of this tag.
	 * @throws UnsupportedOperationException
	 *             If there is no directly accessible payload or it cannot be interpreted as a double.
	 */
	public double getDouble() {
		try {
			return this.<Number> get().doubleValue();
		} catch (ClassCastException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * Short form of <code>child(i).getDouble()</code>.
	 * 
	 * @param i
	 *            Index of the child tag to get from.
	 * @return A double representation of the payload of the specified child tag.
	 * @throws IndexOutOfBoundsException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a double.
	 */
	public double getDouble(int i) {
		Tag tag = child(i);
		if (tag == null) throw new IndexOutOfBoundsException("Bad index: " + i);
		return tag.getDouble();
	}

	/**
	 * Short form of <code>child(tagname).getDouble()</code>.
	 * 
	 * @param tagname
	 *            Name of the child tag to get from.
	 * @return A double representation of the payload of the specified child tag.
	 * @throws NoSuchElementException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a double.
	 */
	public double getDouble(String tagname) {
		Tag tag = child(tagname);
		if (tag == null) throw new NoSuchElementException("Bad tagname: " + tagname);
		return tag.getDouble();
	}

	/**
	 * @return A String representation of the payload of this tag.
	 * @throws UnsupportedOperationException
	 *             If there is no directly accessible payload or it cannot be interpreted as a String.
	 */
	public String getString() {
		return this.<Object> get().toString();
	}

	/**
	 * Short form of <code>child(i).getString()</code>.
	 * 
	 * @param i
	 *            Index of the child tag to get from.
	 * @return A String representation of the payload of the specified child tag.
	 * @throws IndexOutOfBoundsException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a String.
	 */
	public String getString(int i) {
		Tag tag = child(i);
		if (tag == null) throw new IndexOutOfBoundsException("Bad index: " + i);
		return tag.getString();
	}

	/**
	 * Short form of <code>child(tagname).getString()</code>.
	 * 
	 * @param tagname
	 *            Name of the child tag to get from.
	 * @return A String representation of the payload of the specified child tag.
	 * @throws NoSuchElementException
	 *             If there is no such child.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or there is no directly accessible payload or the
	 *             payload cannot be interpreted as a String.
	 */
	public String getString(String tagname) {
		Tag tag = child(tagname);
		if (tag == null) throw new NoSuchElementException("Bad tagname: " + tagname);
		return tag.getString();
	}

	/**
	 * Set the payload of this tag.
	 * 
	 * @param t
	 *            Object to set the payload with.
	 * @throws NullPointerException
	 *             If <code>t</code> is null.
	 * @throws IllegalArgumentException
	 *             If <code>t</code> cannot be used to set the payload.
	 * @throws UnsupportedOperationException
	 *             If setting the payload is not allowed.
	 */
	public abstract <T> void set(T t);

	/**
	 * Short form of <code>child(i).set(t)</code>, with the addition that the child should be created if it does not
	 * exist, if possible.
	 * 
	 * @param i
	 *            Index of the child tag to set.
	 * @param t
	 *            The value to set.
	 * @throws IndexOutOfBoundsException
	 *             If there is no such child, and it cannot be created.
	 * @throws NullPointerException
	 *             If <code>t</code> is null.
	 * @throws IllegalStateException
	 *             If an attempt to create a new tag failed because there was no suitable wrapper for <code>t</code>.
	 * @throws IllegalArgumentException
	 *             If <code>t</code> cannot be used to set the payload.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by index or setting the payload is not allowed.
	 */
	public <T> void set(int i, T t) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Short form of <code>child(tagname).set(t)</code>, with the addition that the child should be created if it does
	 * not exist, if possible.
	 * 
	 * @param tagname
	 *            Name of the child tag to set.
	 * @param t
	 *            The value to set.
	 * @throws NullPointerException
	 *             If <code>tagname</code> or <code>t</code> are null.
	 * @throws IllegalStateException
	 *             If an attempt to create a new tag failed because there was no wrapper for <code>t</code>.
	 * @throws IllegalArgumentException
	 *             If <code>t</code> cannot be used to set the payload.
	 * @throws UnsupportedOperationException
	 *             If there are no children accessible by name, setting the payload is not allowed, or a new child
	 *             cannot be created.
	 */
	public <T> void set(String tagname, T t) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Remove all children of this tag.
	 */
	public void clear() {

	}

	/**
	 * Add a child to this tag by index.
	 * 
	 * @param i
	 *            Index to add child at.
	 * @param tag
	 *            Tag to add.
	 * @return True iff adding the tag succeeds.
	 * @throws NullPointerException
	 *             If <code>tag</code> is null.
	 * @throws IllegalArgumentException
	 *             If the tag cannot be added.
	 * @throws UnsupportedOperationException
	 *             If adding children by index is not allowed.
	 */
	public void add(int i, Tag tag) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Add a child tag to this tag.
	 * 
	 * @param tag
	 *            Child tag to add.
	 * @return True iff adding the tag succeeds.
	 * @throws NullPointerException
	 *             If <code>tag</code> is null.
	 * @throws IllegalArgumentException
	 *             If the tag cannot be added.
	 * @throws UnsupportedOperationException
	 *             If adding children is not allowed.
	 */
	public void add(Tag tag) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Remove the ith child of this tag.
	 * 
	 * @param i
	 *            Child tag to remove.
	 * @return The tag that was removed, or null if nothing happened.
	 * @throws UnsupportedOperationException
	 *             If removing children is not allowed.
	 */
	public Tag remove(int i) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Remove a child of this tag. Specifically, the first one where <code>.name().equals(tagname)</code> returns true,
	 * where 'first' is implementation-defined.
	 * 
	 * @param tagname
	 *            Name of tag to remove.
	 * @return The tag that was removed, or null if nothing happened.
	 * @throws NullPointerException
	 *             If <code>tagname</code> is null.
	 * @throws UnsupportedOperationException
	 *             If removing children is not allowed.
	 */
	public Tag remove(String tagname) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Replace a child of this tag, as if by removing a child at the specified index and then adding a new one at the
	 * same index. A child should be removed only if a new one is added.
	 * 
	 * @param i
	 *            Index of the child to replace.
	 * @param tag
	 *            Tag to replace with or add.
	 * @return The replaced tag, or null if there was none.
	 * @throws NullPointerException
	 *             If <code>tag</code> is null.
	 * @throws IndexOutOfBoundsException
	 *             If there is no child at the specified index, and one cannot be added there.
	 * @throws IllegalArgumentException
	 *             If the tag cannot be added.
	 * @throws UnsupportedOperationException
	 *             If replacing or adding children is not allowed.
	 * 
	 */
	public Tag replace(int i, Tag tag) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Replace a child of this tag, as if by removing a child by the specified name and then adding a new one (with its
	 * own name!). A child should be removed only if a new one is added.
	 * 
	 * @param tagname
	 *            Name of the child to replace.
	 * @param tag
	 *            Tag to add.
	 * @return The replaced tag, or null if there was none.
	 * @throws NullPointerException
	 *             If <code>tagname</code> or <code>tag</code> are null.
	 * @throws IllegalArgumentException
	 *             If the tag cannot be added.
	 * @throws UnsupportedOperationException
	 *             If replacing or adding children is not allowed.
	 */
	public Tag replace(String tagname, Tag tag) {
		throw new UnsupportedOperationException();
	}

	protected abstract void writePayload(DataOutput out) throws IOException;

	/**
	 * Write this tag in its entirety to a stream, including the initial tagid byte.
	 * 
	 * @param out
	 *            DataOutput to write to.
	 * @throws IOException
	 */
	public void write(DataOutput out) throws IOException {
		out.writeByte(m_tagid);
		out.writeUTF(m_name);
		writePayload(out);
	}

	@Override
	public Tag clone() {
		try {
			return (Tag) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		Parser p = getTagParser(m_tagid);
		return p.typeName() + "(" + p.tagID() + ") [" + m_name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
		result = prime * result + m_tagid;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Tag other = (Tag) obj;
		if (m_name == null) {
			if (other.m_name != null) return false;
		} else if (!m_name.equals(other.m_name)) return false;
		if (m_tagid != other.m_tagid) return false;
		return true;
	}

}
