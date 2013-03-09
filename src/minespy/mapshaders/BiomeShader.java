package minespy.mapshaders;

import java.awt.Color;

import minespy.*;
import minespy.colorproviders.IBlockColorProvider;

public class BiomeShader implements IMapShader {

	public static class Factory extends AbstractMapShaderFactory {

		@Override
		public IMapShader createInstance() {
			return new BiomeShader();
		}

		@Override
		public String toString() {
			return "Biome Shader";
		}

		@Override
		public String getDisplayName() {
			return "Biome Shader";
		}

		@Override
		public String getFileName() {
			return "biome";
		}
	}

	@Override
	public int shade(IWorld w, int x, int z, IBlockColorProvider block_color_provider, int skylight) {
		YRun yrun = w.getYRun(x, z);
		return 0xFF000000 | Color.getHSBColor(yrun.getBiome() / 22f, 1f, 1f).getRGB();
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