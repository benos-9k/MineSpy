package minespy.chunkfilters;

import minespy.IChunk;

public interface IChunkFilter {

	public IChunk filter(IChunk c);
	
	public String getFileName();
	
}
