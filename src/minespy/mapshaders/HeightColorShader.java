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
		return (h > 0 ? 0xFF000000 : 0) | (0x00FFFFFF & Color.getHSBColor((1f - h / 255f) * 0.7f, 1f, 1f).getRGB());
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