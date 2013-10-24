package minespy.mapshaders;

public interface IMapShaderFactory {
	
	public IMapShader createInstance();
	
	public <T> void setProperty(String name, T val);
	
	public void setProperty(String name, String val);
	
	public <T> T getProperty(String name);
	
	public String[] listProperties();
	
	public String getDisplayName();
	
	public String getFileName();
	
}