package minespy.mapshaders;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMapShaderFactory implements IMapShaderFactory {

	private final Map<String, Object> m_props = new HashMap<String, Object>();
	private final Map<String, Class<?>> m_types = new HashMap<String, Class<?>>();

	public AbstractMapShaderFactory() {

	}

	protected <T> void createProperty(String name, Class<T> type, T defaultval) {
		if (name == null || type == null || defaultval == null) throw new NullPointerException();
		m_props.put(name, defaultval);
		m_types.put(name, type);
	}

	@Override
	public <T> void setProperty(String name, T val) {
		if (name == null || val == null) throw new NullPointerException();
		if (!m_props.containsKey(name)) throw new IllegalArgumentException("No such property.");
		m_props.put(name, m_types.get(name).cast(val));
	}

	@Override
	public void setProperty(String name, String val) {
		if (name == null || val == null) throw new NullPointerException();
		if (!m_props.containsKey(name)) throw new IllegalArgumentException("No such property.");
		try {
			Constructor<?> ctor = m_types.get(name).getConstructor(String.class);
			m_props.put(name, ctor.newInstance(val));
		} catch (Exception e) {
			throw new IllegalStateException("Unable to set property <" + m_types.get(name).getName() + "> '" + name
					+ "'" + " to value '" + val + "'", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String name) {
		if (name == null) throw new NullPointerException();
		if (!m_props.containsKey(name)) throw new IllegalArgumentException("No such property.");
		return (T) m_props.get(name);
	}

	@Override
	public String[] listProperties() {
		return (String[]) m_props.keySet().toArray();
	}

}
