package minespy;

public interface IChunkCoordIterator {

	/**
	 * @return The next ChunkCoord.
	 * @throws NoSuchElementException
	 *             If there are no more ChunkCoords.
	 */
	public ChunkCoord next();

}