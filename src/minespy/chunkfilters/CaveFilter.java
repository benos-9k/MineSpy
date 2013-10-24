package minespy.chunkfilters;

import minespy.Config;
import minespy.IChunk;

public class CaveFilter implements IChunkFilter {

	public static class Chunk extends AbstractFilteredChunk {

		// order z, x
		private final int[] cave_hmap = new int[256];

		public Chunk(IChunk c_) {
			super(c_);
			for (int z = 0; z < 16; z++) {
				for (int x = 0; x < 16; x++) {
					int y = 255;
					while (Config.getConfig().isBlockTransparent(c_.getBlock(x, z, y))) {
						// first opaque
						y--;
						if (y < 0) continue;
					}
					while (!Config.getConfig().isBlockTransparent(c_.getBlock(x, z, y - 1))) {
						// next transparent
						y--;
						if (y < 1) continue;
					}
					cave_hmap[z * 16 + x] = y;
				}
			}
		}

		@Override
		public int getBlock(int local_x, int local_z, int local_y) {
			if (local_y >= cave_hmap[local_z * 16 + local_x]) {
				return 0;
			} else {
				return super.getBlock(local_x, local_z, local_y);
			}
		}

		@Override
		public int getData(int local_x, int local_z, int local_y) {
			if (local_y >= cave_hmap[local_z * 16 + local_x]) {
				return 0;
			} else {
				return super.getData(local_x, local_z, local_y);
			}
		}

	}

	@Override
	public IChunk filter(IChunk c) {
		return new Chunk(c);
	}

	@Override
	public String getFileName() {
		return "cave";
	}

}
