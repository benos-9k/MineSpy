package minespy.mapshaders;

import java.awt.Color;

import minespy.*;
import minespy.colorproviders.IBlockColorProvider;

public class HeightColorShader implements IMapShader {

	public static class Factory extends AbstractMapShaderFactory {

		@Override
		public IMapShader createInstance() {
			return new HeightColorShader();
		}

		@Override
		public String toString() {
			return "Heightmap (Colour) Shader";
		}

		@Override
		public String getDisplayName() {
			return "Heightmap (Colour) Shader";
		}

		@Override
		public String getFileName() {
			return "hmap-colour";
		}
	}

	@Override
	public int shade(IWorld w, int x, int z, IBlockColorProvider block_color_provider, int skylight) {
		YRun yrun = w.getYRun(x, z);
		return 0xFF000000 | Color.getHSBColor((1f - yrun.getHeight() / 255f) * 0.7f, 1f, 1f).getRGB();
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