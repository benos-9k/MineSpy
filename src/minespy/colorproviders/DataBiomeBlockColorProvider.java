package minespy.colorproviders;

public class DataBiomeBlockColorProvider implements IBlockColorProvider {

	private int[] m_blockcolors = new int[128 * 16];

	public DataBiomeBlockColorProvider() {

	}

	/**
	 * Set the color for all data values and biomes.
	 * 
	 * @param rgb
	 *            The color to set.
	 */
	public void setColor(int rgb) {
		for (int i = 0; i < m_blockcolors.length; i++) {
			m_blockcolors[i] = rgb;
		}
	}

	/**
	 * Set the color for all data values within a specific biome.
	 * 
	 * @param biome
	 *            The biome to set the color for, range 0 - 127 inclusive.
	 * @param rgb
	 *            The color to set.
	 */
	public void setBiomeColor(int biome, int rgb) {
		for (int i = biome * 16; i < biome * 16 + 16; i++) {
			m_blockcolors[i] = rgb;
		}
	}

	/**
	 * Set the color for a specific data value in all biomes.
	 * 
	 * @param data
	 *            The data value to set the color for, range 0 - 15 inclusive.
	 * @param rgb
	 *            The color to set.
	 */
	public void setDataColor(int data, int rgb) {
		for (int i = data; i < m_blockcolors.length; i += 16) {
			m_blockcolors[i] = rgb;
		}
	}

	/**
	 * Set the color for all data values <code>i</code> such that <code>i & datamask == data</code> in all biomes.
	 * 
	 * @param datamask
	 *            Mask to determine which data values to set the color for.
	 * @param data
	 *            The data value to set the color for, range 0 - 15 inclusive.
	 * @param rgb
	 *            The color to set.
	 */
	public void setDataColor(int datamask, int data, int rgb) {
		for (int i = 0; i < m_blockcolors.length; i++) {
			if ((i & datamask) == data) {
				m_blockcolors[i] = rgb;
			}
		}
	}

	/**
	 * Set the color for a specific data value and specific biome.
	 * 
	 * @param data
	 *            The data value to set the color for, range 0 - 15 inclusive.
	 * @param biome
	 *            The biome to set the color for, range 0 - 127 inclusive.
	 * @param rgb
	 *            The color to set.
	 */
	public void setDataBiomeColor(int data, int biome, int rgb) {
		m_blockcolors[biome * 16 + data] = rgb;
	}

	/**
	 * Set the color for all data values <code>i</code> such that <code>i & datamask == data</code> in the specified
	 * biome.
	 * 
	 * @param datamask
	 *            Mask to determine which data values to set the color for.
	 * @param data
	 *            The data value to set the color for, range 0 - 15 inclusive.
	 * @param biome
	 *            The biome to set the color for, range 0 - 127 inclusive.
	 * @param rgb
	 *            The color to set.
	 */
	public void setDataBiomeColor(int datamask, int data, int biome, int rgb) {
		for (int i = biome * 16; i < biome * 16 + 16; i++) {
			if ((i & datamask) == data) {
				m_blockcolors[i] = rgb;
			}
		}
	}

	@Override
	public int getRGB(int blockid, int data, int biome) {
		// make biome and data values wrap (so biome -1 doesn't error)
		return m_blockcolors[(biome & 127) * 16 + (data & 15)];
	}

}
