package minespy;

public class YRun {
	
	public static final YRun BLANK = new BlankYRun();
	
	private static class BlankYRun extends YRun {
		
		public BlankYRun() {
			super(null, 0, 0);
		}
		
		@Override
		public int getBiome() {
			return -1;
		}
		
		@Override
		public int getHeight() {
			return 0;
		}
		
		@Override
		public int getBlock(int y) {
			return 0;
		}
		
		@Override
		public int getData(int y) {
			return 0;
		}
		
		@Override
		public int getBlockLight(int y) {
			return 0;
		}
		
		@Override
		public int getSkyLight(int y) {
			return 15;
		}
		
	}
	
	private final IChunk m_chunk;
	private final int m_local_x, m_local_z;
	
	public YRun(IChunk chunk_, int local_x_, int local_z_) {
		m_chunk = chunk_;
		m_local_x = local_x_;
		m_local_z = local_z_;
	}

	public int getBiome() {
		return m_chunk.getBiome(m_local_x, m_local_z);
	}
	
	public int getHeight() {
		return m_chunk.getHeight(m_local_x, m_local_z);
	}
	
	public int getBlock(int y) {
		return m_chunk.getBlock(m_local_x, m_local_z, y);
	}
	
	public int getData(int y) {
		return m_chunk.getData(m_local_x, m_local_z, y);
	}
	
	public int getBlockLight(int y) {
		return m_chunk.getBlockLight(m_local_x, m_local_z, y);
	}
	
	public int getSkyLight(int y) {
		return m_chunk.getSkyLight(m_local_x, m_local_z, y);
	}
	
}
