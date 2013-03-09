package minespy.mapshaders;

class Util {

	private Util() {
		throw new AssertionError("You're doing it wrong.");
	}

	static double cross_x(double a_x, double a_y, double a_z, double b_x, double b_y, double b_z) {
		return a_y * b_z - a_z * b_y;
	}

	static double cross_y(double a_x, double a_y, double a_z, double b_x, double b_y, double b_z) {
		return a_z * b_x - a_x * b_z;
	}

	static double cross_z(double a_x, double a_y, double a_z, double b_x, double b_y, double b_z) {
		return a_x * b_y - a_y * b_x;
	}

	static int colorMul(int rgb, double k) {
		k = k < 0d ? 0d : k;
		int a = (int) ((rgb >>> 24) * k);
		a = a < 255 ? a : 255;
		int r = (int) (((rgb >>> 16) & 0xFF) * k);
		r = r < 255 ? r : 255;
		int g = (int) (((rgb >>> 8) & 0xFF) * k);
		g = g < 255 ? g : 255;
		int b = (int) ((rgb & 0xFF) * k);
		b = b < 255 ? b : 255;
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	static int colorAdd(int rgb0, int rgb1) {
		int a = (rgb0 >>> 24) + (rgb1 >>> 24);
		a = a < 255 ? a : 255;
		int r = ((rgb0 >>> 16) & 0xFF) + ((rgb1 >>> 16) & 0xFF);
		r = r < 255 ? r : 255;
		int g = ((rgb0 >>> 8) & 0xFF) + ((rgb1 >>> 8) & 0xFF);
		g = g < 255 ? g : 255;
		int b = (rgb0 & 0xFF) + (rgb1 & 0xFF);
		b = b < 255 ? b : 255;
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	static int alphaBlend(int base, int top) {
		double alpha_base = ((base & 0xFF000000) >>> 24) * (1d / 255d);
		double alpha_top = ((top & 0xFF000000) >>> 24) * (1d / 255d);
		double alpha = 1d - (1d - alpha_top) * (1d - alpha_base);
		// color = alpha_top * color_top + (1 - alpha_top) * color_base
		return (((int) (alpha * 255d)) << 24)
				| colorAdd(colorMul(0x00FFFFFF & top, alpha_top), colorMul(0x00FFFFFF & base, 1d - alpha_top));
	}

	static float fastInverseSqrt(float number) {
		float x2 = number * 0.5f;
		// evil floating point bit level hacking
		number = Float.intBitsToFloat(0x5f3759df - (Float.floatToRawIntBits(number) >>> 1));
		// 1st iteration of newton's method
		number = number * (1.5f - (x2 * number * number));
		return number;
	}

}
