package minespy.chunkfilters;

import minespy.IChunk;

public class ForceBrightnessFilter implements IChunkFilter {

	@Override
	public IChunk filter(IChunk c) {
		return new Chunk(c);
	}
	
	private static class Chunk extends AbstractFilteredChunk {

		public Chunk(IChunk c) {
			super(c);
		}
		
		@Override
		public int getSkyLight(int local_x, int local_z, int local_y) {
			return 15;
		}

	}

	@Override
	public String getFileName() {
		return "bright";
	}
	
}