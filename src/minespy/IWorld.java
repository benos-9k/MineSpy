package minespy;

import minespy.chunkfilters.IChunkFilter;

public interface IWorld {

	public int minX();
	
	public int maxX();
	
	public int minZ();
	
	public int maxZ();
	
	public int sizeX();
	
	public int sizeZ();
	
	public int totalChunks();
	
	public YRun getYRun(int x, int z);
	
	public void setChunkFilter(IChunkFilter cf);
	
	public void acquireChunks(ChunkCoord... ccs);
	
	public void releaseChunks(ChunkCoord... ccs);
	
	public IChunkCoordIterator iterator();
	
	public void close();
	
}
