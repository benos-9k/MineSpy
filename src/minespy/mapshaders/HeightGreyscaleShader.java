package minespy.mapshaders;

import minespy.*;
import minespy.colorproviders.IBlockColorProvider;

public class HeightGreyscaleShader implements IMapShader {

	public static class Factory extends AbstractMapShaderFactory {

		@Override
		public IMapShader createInstance() {
			return new HeightGreyscaleShader();
		}

		@Override
		public String toString() {
			return "Heightmap (Greyscale) Shader";
		}

		@Override
		public String getDisplayName() {
			return "Heightmap (Greyscale) Shader";
		}

		@Override
		public String getFileName() {
			return "hmap-grey";
		}
	}
	
	protected static int height(YRun yrun) {
		for (int y = 256; y-- > 0;) {
			if (yrun.getBlock(y) != 0) return y + 1;
		}
		return 0;
	}

	@Override
	public int shade(IWorld w, int x, int z, IBlockColorProvider block_color_provider, int skylight) {
		YRun yrun = w.getYRun(x, z);
		int h = height(yrun);
		return (h > 0 ? 0xFF000000 : 0) | (h << 16) | (h << 8) | h;
	}

	@Override
	public void acquireChunks(IWorld w, ChunkCoord cc) {
		w.acquireChunks(cc);
	}

	@Override
	public void releaseChunks(IWorld w, ChunkCoord cc) {
		w.releaseChunks(cc);
	}

}