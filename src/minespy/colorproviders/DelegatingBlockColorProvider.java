package minespy.colorproviders;


public class DelegatingBlockColorProvider implements IBlockColorProvider {

	private final int[] m_default_colors = new int[4096];
	private final IBlockColorProvider[] m_block_color_providers = new IBlockColorProvider[4096];

	public DelegatingBlockColorProvider() {

	}
	
	public DelegatingBlockColorProvider(int[] default_colors_) {
		System.arraycopy(default_colors_, 0, m_default_colors, 0, m_default_colors.length);
	}
	
	public void setDefaultColor(int blockid, int rgb) {
		m_default_colors[blockid] = rgb;
	}
	
	public int getDefaultColor(int blockid) {
		return m_default_colors[blockid];
	}
	
	public void setBlockColorProvider(int blockid, IBlockColorProvider cp) {
		m_block_color_providers[blockid] = cp;
	}
	
	public IBlockColorProvider getBlockColorProvider(int blockid) {
		return m_block_color_providers[blockid];
	}

	@Override
	public int getRGB(int blockid, int data, int biome) {
		if (m_block_color_providers[blockid] != null) {
			return m_block_color_providers[blockid].getRGB(blockid, data, biome);
		} else {
			return m_default_colors[blockid];
		}
	}

}