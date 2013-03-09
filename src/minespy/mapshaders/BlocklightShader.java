package minespy.mapshaders;

import java.awt.Color;

import minespy.*;
import minespy.colorproviders.IBlockColorProvider;

public class BlocklightShader implements IMapShader {

	public static class Factory extends AbstractMapShaderFactory {

		@Override
		public IMapShader createInstance() {
			return new BlocklightShader();
		}

		@Override
		public String toString() {
			return "Blocklight Shader";
		}

		@Override
		public String getDisplayName() {
			return "Blocklight Shader";
		}

		@Override
		public String getFileName() {
			return "blocklight";
		}

	}

	@Override
	public int shade(IWorld w, int x, int z, IBlockColorProvider block_color_provider, int skylight) {
		YRun yrun = w.getYRun(x, z);
		int y = 255;
		while (Config.getConfig().isBlockTransparent(yrun.getBlock(y - 1))) {
			// first opaque
			y--;
			if (y < 1) return 0;
		}
		return 0xFF000000 | Color.getHSBColor((1f - yrun.getBlockLight(y) / 15f) * 0.7f, 1f, 1f).getRGB();
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
