package minespy.chunkfilters;

import java.util.Arrays;

import minespy.IChunk;

public class BlockIncludeFilter implements IChunkFilter {

	private final boolean[] m_include = new boolean[4096];
	private final int[] m_blocks;

	public BlockIncludeFilter(int[] blocks_) {
		m_blocks = Arrays.copyOf(blocks_, blocks_.length);
		for (int block : blocks_) {
			m_include[block] = true;
		}
	}

	@Override
	public IChunk filter(IChunk c) {
		return new Chunk(c, m_include);
	}

	private static class Chunk extends AbstractFilteredChunk {

		private final boolean[] m_include;

		public Chunk(IChunk c, boolean[] include_) {
			super(c);
			m_include = include_;
		}

		@Override
		public int getBlock(int local_x, int local_z, int local_y) {
			int block = super.getBlock(local_x, local_z, local_y);
			if (!m_include[block]) return 0;
			return block;
		}

		@Override
		public int getData(int local_x, int local_z, int local_y) {
			if (!m_include[super.getBlock(local_x, local_z, local_y)]) return 0;
			return super.getData(local_x, local_z, local_y);
		}

	}

	@Override
	public String getFileName() {
		return "includeblocks" + Arrays.toString(m_blocks).replace(" ", "");
	}

}
