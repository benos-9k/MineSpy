package minespy;

public interface IChunk {

	public int getBiome(int local_x, int local_z);
	
	public int getHeight(int local_x, int local_z);
	
	public int getBlock(int local_x, int local_z, int local_y);
	
	public int getData(int local_x, int local_z, int local_y);
	
	public int getBlockLight(int local_x, int local_z, int local_y);
	
	public int getSkyLight(int local_x, int local_z, int local_y);
	
}
