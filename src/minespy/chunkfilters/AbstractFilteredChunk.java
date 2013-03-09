package minespy.chunkfilters;

import minespy.IChunk;

public abstract class AbstractFilteredChunk implements IChunk {

	private final IChunk m_c;
	
	public AbstractFilteredChunk(IChunk c_) {
		m_c = c_;
	}
	
	@Override
	public int getBiome(int local_x, int local_z) {
		return m_c.getBiome(local_x, local_z);
	}

	@Override
	public int getHeight(int local_x, int local_z) {
		return m_c.getHeight(local_x, local_z);
	}

	@Override
	public int getBlock(int local_x, int local_z, int local_y) {
		return m_c.getBlock(local_x, local_z, local_y);
	}

	@Override
	public int getData(int local_x, int local_z, int local_y) {
		return m_c.getData(local_x, local_z, local_y);
	}

	@Override
	public int getBlockLight(int local_x, int local_z, int local_y) {
		return m_c.getBlockLight(local_x, local_z, local_y);
	}

	@Override
	public int getSkyLight(int local_x, int local_z, int local_y) {
		return m_c.getSkyLight(local_x, local_z, local_y);
	}

}
