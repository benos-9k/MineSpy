package minespy.chunkfilters;

import java.util.Arrays;

import minespy.IChunk;
import minespy.MineSpy;

public class BlockExcludeFilter implements IChunkFilter {

	private final boolean[] m_exclude = new boolean[4096];
	private final int[] m_blocks;
	
	public BlockExcludeFilter(int[] blocks_) {
		m_blocks = Arrays.copyOf(blocks_, blocks_.length);
		Arrays.sort(m_blocks);
		for (int block : blocks_) {
			m_exclude[block] = true;
		}
	}
	
	@Override
	public IChunk filter(IChunk c) {
		return new Chunk(c, m_exclude);
	}
	
	private static class Chunk extends AbstractFilteredChunk {

		private final boolean[] m_exclude;
		
		public Chunk(IChunk c, boolean[] exclude_) {
			super(c);
			m_exclude = exclude_;
		}
		
		@Override
		public int getBlock(int local_x, int local_z, int local_y) {
			int block = super.getBlock(local_x, local_z, local_y);
			if (m_exclude[block]) return 0;
			return block;
		}

		@Override
		public int getData(int local_x, int local_z, int local_y) {
			if (m_exclude[super.getBlock(local_x, local_z, local_y)]) return 0;
			return super.getData(local_x, local_z, local_y);
		}
		
	}

	@Override
	public String getFileName() {
		return "eb(" + MineSpy.intArrayToString(m_blocks) + ")";
	}

}
