package minespy;

public class RegionCoord {

	private final int m_x, m_z;

	public RegionCoord(int x_, int z_) {
		m_x = x_;
		m_z = z_;
	}

	public int x() {
		return m_x;
	}

	public int z() {
		return m_z;
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
		RegionCoord other = (RegionCoord) obj;
		if (m_x != other.m_x) return false;
		if (m_z != other.m_z) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "RegionCoord[x=" + m_x + ",z=" + m_z + "]";
	}

}
