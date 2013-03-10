package minespy;

import java.util.Arrays;
import java.util.NoSuchElementException;

import minespy.nbt.*;

public final class Chunk implements IChunk {

	public static final Chunk BLANK = new Chunk();

	private short[] m_blocks = new short[16 * 16 * 256];
	private byte[] m_data = new byte[16 * 16 * 256];
	private byte[] m_blocklight = new byte[16 * 16 * 256];
	private byte[] m_skylight = new byte[16 * 16 * 256];
	private byte[] m_biomes;
	private int[] m_heightmap;

	public Chunk() {
		m_biomes = new byte[16 * 16];
		m_heightmap = new int[16 * 16];
		Arrays.fill(m_biomes, (byte) -1);
	}

	public Chunk(Tag root_) {
		Tag level = root_.child("Level");
		try {
			m_biomes = level.get("Biomes");
		} catch (NoSuchElementException e) {
			m_biomes = new byte[16 * 16];
			Arrays.fill(m_biomes, (byte) -1);
		}

		try {
			m_heightmap = level.get("HeightMap");
		} catch (NoSuchElementException e) {
			m_heightmap = new int[16 * 16];
			// TODO precompute heightmap if not in file (is this even necessary?)
			System.out.println("*** Ben: write the on-chunk-load heightmap computation! ***");
		}

		for (Tag t : level.child("Sections")) {
			byte section_y = t.get("Y");
			byte[] section_blocks = t.get("Blocks");
			byte[] section_add = null;
			try {
				section_add = t.get("Add");
			} catch (NoSuchElementException e) {
				// nothing to do
			}
			byte[] section_data = t.get("Data");
			byte[] section_blocklight = t.get("BlockLight");
			byte[] section_skylight = t.get("SkyLight");
			// unpack / copy section to offset in chunk
			int offset = section_y * 16 * 16 * 16;
			for (int i = 0; i < section_blocks.length; i++) {
				m_blocks[i + offset] = (short) (section_blocks[i] & 0xFF);
			}
			if (section_add != null) {
				for (int i = 0; i < section_blocks.length; i++) {
					m_blocks[i + offset] += ((short) nibble4(section_add, i)) << 8;
				}
			}
			for (int i = 0; i < section_blocks.length; i++) {
				m_data[i + offset] = nibble4(section_data, i);
			}
			for (int i = 0; i < section_blocks.length; i++) {
				m_blocklight[i + offset] = nibble4(section_blocklight, i);
			}
			for (int i = 0; i < section_blocks.length; i++) {
				m_skylight[i + offset] = nibble4(section_skylight, i);
			}
		}
	}

	private byte nibble4(byte[] arr, int index) {
		return (byte) (index % 2 == 0 ? arr[index / 2] & 0x0F : (arr[index / 2] >> 4) & 0x0F);
	}

	public final int getBiome(int local_x, int local_z) {
		return m_biomes[local_z * 16 + local_x];
	}

	public final int getHeight(int local_x, int local_z) {
		return m_heightmap[local_z * 16 + local_x];
	}

	public final int getBlock(int local_x, int local_z, int local_y) {
		if (local_y < 0 || local_y > 255) return 0;
		return m_blocks[local_y * 16 * 16 + local_z * 16 + local_x];
	}

	public final int getData(int local_x, int local_z, int local_y) {
		if (local_y < 0 || local_y > 255) return 0;
		return m_data[local_y * 16 * 16 + local_z * 16 + local_x];
	}

	public final int getBlockLight(int local_x, int local_z, int local_y) {
		if (local_y < 0 || local_y > 255) return 0;
		return m_blocklight[local_y * 16 * 16 + local_z * 16 + local_x];
	}

	public final int getSkyLight(int local_x, int local_z, int local_y) {
		if (local_y < 0) return 0;
		if (local_y > 255) return 15;
		return m_skylight[local_y * 16 * 16 + local_z * 16 + local_x];
	}

}
