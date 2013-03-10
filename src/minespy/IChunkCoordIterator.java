package minespy;

public interface IChunkCoordIterator {

	/**
	 * @return The next ChunkCoord.
	 * @throws NoMoreChunksException
	 *             If there are no more ChunkCoords.
	 */
	public ChunkCoord next();

}