package minespy.mapshaders;

import minespy.*;
import minespy.colorproviders.IBlockColorProvider;
import static minespy.mapshaders.Util.*;

public class PrettyShader implements IMapShader {

	public static class Factory extends AbstractMapShaderFactory {

		public Factory() {
			createProperty("SurfaceNormalBaseLightingModifier", Double.class, new Double(0.5));
			createProperty("SurfaceNormalTransparentLightingModifier", Double.class, new Double(0.5));
		}

		@Override
		public IMapShader createInstance() {
			return new PrettyShader(this.<Double> getProperty("SurfaceNormalBaseLightingModifier"),
					this.<Double> getProperty("SurfaceNormalTransparentLightingModifier"));
		}

		@Override
		public String toString() {
			return "Pretty Shader";
		}

		@Override
		public String getDisplayName() {
			return "Pretty Shader";
		}

		@Override
		public String getFileName() {
			return "pretty";
		}
	}

	private final double m_a, m_b;

	public PrettyShader(double a_, double b_) {
		m_a = a_;
		m_b = b_;
	}

	protected static int height(YRun yrun) {
		for (int y = 256; y-- > 0;) {
			if (yrun.getBlock(y) != 0) return y + 1;
		}
		return 0;
	}

	protected static int heightOpaque(YRun yrun, IBlockColorProvider cp) {
		for (int y = 256; y-- > 0;) {
			if ((cp.getRGB(yrun.getBlock(y), yrun.getData(y), yrun.getBiome()) & 0xFF000000) == 0xFF000000)
				return y + 1;
		}
		return 0;
	}

	protected static int heightOpaque(YRun yrun, IBlockColorProvider cp, int alpha) {
		// the height of the highest block of at least that alpha value
		// FIXME this is a bottleneck!
		// but it didn't use to be quite so bad...
		int biome = yrun.getBiome();
		for (int y = 256; y-- > 0;) {
			if ((cp.getRGB(yrun.getBlock(y), yrun.getData(y), biome) >>> 24) >= alpha) return y + 1;
		}
		return 0;
		// return yrun.getHeight();
	}

	protected static float normalYOpaque(IWorld w, int x, int z, IBlockColorProvider cp, int alpha) {
		YRun yrun = w.getYRun(x, z);
		int h = heightOpaque(yrun, cp, alpha);
		// get unit vectors to surrounding blocks
		float dh_nw = heightOpaque(w.getYRun(x - 1, z - 1), cp, alpha) - h; // -1, dh_nw, -1
		float il_nw = fastInverseSqrt(1 + dh_nw * dh_nw + 1);
		dh_nw *= il_nw;
		float dh_n = heightOpaque(w.getYRun(x, z - 1), cp, alpha) - h; // 0, dh_n, -1
		float il_n = fastInverseSqrt(0 + dh_n * dh_n + 1);
		dh_n *= il_n;
		float dh_ne = heightOpaque(w.getYRun(x + 1, z - 1), cp, alpha) - h; // 1, dh_ne, -1
		float il_ne = fastInverseSqrt(1 + dh_ne * dh_ne + 1);
		dh_ne *= il_ne;
		float dh_e = heightOpaque(w.getYRun(x + 1, z), cp, alpha) - h; // 1, dh_e, 0
		float il_e = fastInverseSqrt(1 + dh_e * dh_e + 0);
		dh_e *= il_e;
		float dh_se = heightOpaque(w.getYRun(x + 1, z + 1), cp, alpha) - h; // 1, dh_se, 1
		float il_se = fastInverseSqrt(1 + dh_se * dh_se + 1);
		dh_se *= il_se;
		float dh_s = heightOpaque(w.getYRun(x, z + 1), cp, alpha) - h; // 0, dh_s, 1
		float il_s = fastInverseSqrt(0 + dh_s * dh_s + 1);
		dh_s *= il_s;
		float dh_sw = heightOpaque(w.getYRun(x - 1, z + 1), cp, alpha) - h; // -1, dh_sw, 1
		float il_sw = fastInverseSqrt(1 + dh_sw * dh_sw + 1);
		dh_sw *= il_sw;
		float dh_w = heightOpaque(w.getYRun(x - 1, z), cp, alpha) - h; // -1, dh_w, 0
		float il_w = fastInverseSqrt(1 + dh_w * dh_w + 0);
		dh_w *= il_w;

		// then add succesive cross products and re-normalise to get
		// the surface normal vector
		float n_x = 0, n_y = 0, n_z = 0;
		// x component of sum of cross products
		n_x += cross_x(-il_w, dh_w, 0, -il_sw, dh_sw, il_sw);
		n_x += cross_x(-il_sw, dh_sw, il_sw, 0, dh_s, il_s);
		n_x += cross_x(0, dh_s, il_s, il_se, dh_se, il_se);
		n_x += cross_x(il_se, dh_se, il_se, il_e, dh_e, 0);
		n_x += cross_x(il_e, dh_e, 0, il_ne, dh_ne, -il_ne);
		n_x += cross_x(il_ne, dh_ne, -il_ne, 0, dh_n, -il_n);
		n_x += cross_x(0, dh_n, -il_n, -il_nw, dh_nw, -il_nw);
		n_x += cross_x(-il_nw, dh_nw, -il_nw, -il_w, dh_w, 0);
		// y component of sum of cross products
		n_y += cross_y(-il_w, dh_w, 0, -il_sw, dh_sw, il_sw);
		n_y += cross_y(-il_sw, dh_sw, il_sw, 0, dh_s, il_s);
		n_y += cross_y(0, dh_s, il_s, il_se, dh_se, il_se);
		n_y += cross_y(il_se, dh_se, il_se, il_e, dh_e, 0);
		n_y += cross_y(il_e, dh_e, 0, il_ne, dh_ne, -il_ne);
		n_y += cross_y(il_ne, dh_ne, -il_ne, 0, dh_n, -il_n);
		n_y += cross_y(0, dh_n, -il_n, -il_nw, dh_nw, -il_nw);
		n_y += cross_y(-il_nw, dh_nw, -il_nw, -il_w, dh_w, 0);
		// z component of sum of cross products
		n_z += cross_z(-il_w, dh_w, 0, -il_sw, dh_sw, il_sw);
		n_z += cross_z(-il_sw, dh_sw, il_sw, 0, dh_s, il_s);
		n_z += cross_z(0, dh_s, il_s, il_se, dh_se, il_se);
		n_z += cross_z(il_se, dh_se, il_se, il_e, dh_e, 0);
		n_z += cross_z(il_e, dh_e, 0, il_ne, dh_ne, -il_ne);
		n_z += cross_z(il_ne, dh_ne, -il_ne, 0, dh_n, -il_n);
		n_z += cross_z(0, dh_n, -il_n, -il_nw, dh_nw, -il_nw);
		n_z += cross_z(-il_nw, dh_nw, -il_nw, -il_w, dh_w, 0);
		// normalise result
		float i_length = fastInverseSqrt(n_x * n_x + n_y * n_y + n_z * n_z);
		n_x *= i_length;
		n_y *= i_length;
		n_z *= i_length;
		// note: now that its normalised, we only care about the y component really
		return n_y;
	}

	@Override
	public int shade(IWorld w, int x, int z, IBlockColorProvider cp, int skylight) {
		double f_skylight = skylight / 15d;
		YRun yrun = w.getYRun(x, z);
		int ymax = height(yrun);
		int y = heightOpaque(yrun, cp);
		int rgb = cp.getRGB(yrun.getBlock(y - 1), yrun.getData(y - 1), yrun.getBiome());
		rgb = (0xFF000000 & rgb)
				| colorMul(
						0x00FFFFFF & rgb,
						Math.min(yrun.getBlockLight(y) / 15d + yrun.getSkyLight(y) / 15d * f_skylight
								* (1d - ((1d - normalYOpaque(w, x, z, cp, 255)) * m_a)), 1d));
		for (; y < ymax; y++) {
			int rgb1 = cp.getRGB(yrun.getBlock(y), yrun.getData(y), yrun.getBiome());
			rgb1 = (0xFF000000 & rgb1)
					| colorMul(
							0x00FFFFFF & rgb1,
							Math.min(yrun.getBlockLight(y + 1) / 15d + yrun.getSkyLight(y + 1) / 15d * f_skylight
									* (1d - ((1d - normalYOpaque(w, x, z, cp, (rgb1 >>> 24))) * m_b)), 1d));
			rgb = alphaBlend(rgb, rgb1);
		}
		return rgb;
	}

	@Override
	public void acquireChunks(IWorld w, ChunkCoord cc) {
		w.acquireChunks(cc, cc.northWest(), cc.north(), cc.northEast(), cc.east(), cc.southEast(), cc.south(),
				cc.southWest(), cc.west());
	}

	@Override
	public void releaseChunks(IWorld w, ChunkCoord cc) {
		w.releaseChunks(cc, cc.northWest(), cc.north(), cc.northEast(), cc.east(), cc.southEast(), cc.south(),
				cc.southWest(), cc.west());
	}

}
