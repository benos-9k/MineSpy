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

	@Override
	public int shade(IWorld w, int x, int z, IBlockColorProvider block_color_provider, int skylight) {
		YRun yrun = w.getYRun(x, z);
		int h = yrun.getHeight();
		return 0xFF000000 | (h << 16) | (h << 8) | h;
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