package minespy.chunkfilters;

import java.util.Arrays;

import minespy.IChunk;
import minespy.MineSpy;

public class LevelIncludeFilter implements IChunkFilter {

	private final boolean[] m_include = new boolean[256];
	private final int[] m_levels;

	public LevelIncludeFilter(int[] levels_) {
		m_levels = Arrays.copyOf(levels_, levels_.length);
		Arrays.sort(m_levels);
		for (int level : levels_) {
			if (level >= 0 && level < 256) {
				m_include[level] = true;
			}
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
			if (local_y >= 0 && local_y < 256 && !m_include[local_y]) return 0;
			return super.getBlock(local_x, local_z, local_y);
		}

		@Override
		public int getData(int local_x, int local_z, int local_y) {
			if (local_y >= 0 && local_y < 256 && !m_include[local_y]) return 0;
			return super.getData(local_x, local_z, local_y);
		}

	}

	@Override
	public String getFileName() {
		return "il(" + MineSpy.intArrayToString(m_levels) + ")";
	}

}
