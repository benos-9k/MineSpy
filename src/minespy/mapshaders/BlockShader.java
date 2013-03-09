package minespy.mapshaders;

import minespy.*;
import minespy.colorproviders.IBlockColorProvider;
import static minespy.mapshaders.Util.*;

public class BlockShader implements IMapShader {

	public static class Factory extends AbstractMapShaderFactory {

		@Override
		public IMapShader createInstance() {
			return new BlockShader();
		}

		@Override
		public String toString() {
			return "Block Shader";
		}

		@Override
		public String getDisplayName() {
			return "Block Shader";
		}

		@Override
		public String getFileName() {
			return "block";
		}
	}

	@Override
	public int shade(IWorld w, int x, int z, IBlockColorProvider cp, int skylight) {
		double f_skylight = skylight / 15d;
		YRun yrun = w.getYRun(x, z);
		for (int y = 256; y-- > 0;) {
			int block = yrun.getBlock(y);
			if (block != 0) {
				int rgb = cp.getRGB(block, yrun.getData(y), yrun.getBiome());
				return 0xFF000000 | colorMul(rgb,
						Math.min(yrun.getBlockLight(y + 1) / 15d + yrun.getSkyLight(y + 1) / 15d * f_skylight, 1d));
			}
		}
		return 0;
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