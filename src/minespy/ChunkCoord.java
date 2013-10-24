package minespy;

public class ChunkCoord {

	private final int m_x, m_z;
	
	public ChunkCoord(int x_, int z_) {
		m_x = x_;
		m_z = z_;
	}
	
	public int x() {
		return m_x;
	}
	
	public int z() {
		return m_z;
	}
	
	public ChunkCoord north() {
		return new ChunkCoord(m_x, m_z - 1);
	}
	
	public ChunkCoord south() {
		return new ChunkCoord(m_x, m_z + 1);
	}
	
	public ChunkCoord west() {
		return new ChunkCoord(m_x - 1, m_z);
	}
	
	public ChunkCoord east() {
		return new ChunkCoord(m_x + 1, m_z);
	}
	
	public ChunkCoord northWest() {
		return new ChunkCoord(m_x - 1, m_z - 1);
	}
	
	public ChunkCoord northEast() {
		return new ChunkCoord(m_x + 1, m_z - 1);
	}
	
	public ChunkCoord southWest() {
		return new ChunkCoord(m_x - 1, m_z + 1);
	}
	
	public ChunkCoord southEast() {
		return new ChunkCoord(m_x + 1, m_z + 1);
	}
	
	public RegionCoord getRegionCoord() {
		return new RegionCoord(m_x >> 5, m_z >> 5);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m_x;
		result = prime * result + m_z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ChunkCoord other = (ChunkCoord) obj;
		if (m_x != other.m_x) return false;
		if (m_z != other.m_z) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "ChunkCoord[x=" + m_x + ",z=" + m_z + "]";
	}
	
}
