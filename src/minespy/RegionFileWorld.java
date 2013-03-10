package minespy;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import minespy.chunkfilters.IChunkFilter;
import minespy.nbt.Tag;

public class RegionFileWorld implements IWorld {

	private final List<RegionFile> m_rflist = new ArrayList<RegionFile>();
	private int m_min_x = Integer.MAX_VALUE;
	private int m_min_z = Integer.MAX_VALUE;
	private int m_max_x = Integer.MIN_VALUE;
	private int m_max_z = Integer.MIN_VALUE;
	private int m_size_x, m_size_z;
	private int m_min_cx, m_min_cz, m_size_cx, m_size_cz;

	private final Map<ChunkCoord, ChunkReference> m_crmap = new HashMap<ChunkCoord, ChunkReference>();
	private final IChunk[] m_chunks;
	private final AtomicInteger[] m_chunk_atomic_refcount;

	private IChunkFilter m_cf = null;

	public RegionFileWorld(File[] regionfiles_) throws IOException {
		MineSpy.log("Opening region files...");

		for (File f : regionfiles_) {
			m_rflist.add(new RegionFile(f));
		}

		MineSpy.log("Calculating map size...");

		for (RegionFile rf : m_rflist) {
			for (int r_cx = 0; r_cx < 32; r_cx++) {
				for (int r_cz = 0; r_cz < 32; r_cz++) {
					ChunkCoord cc = new ChunkCoord(rf.getRegionX() * 32 + r_cx, rf.getRegionZ() * 32 + r_cz);
					if (rf.hasChunk(r_cx, r_cz)) {
						m_crmap.put(cc, new ChunkReference(rf, cc));
						int thischunk_min_x = cc.x() * 16;
						int thischunk_min_z = cc.z() * 16;
						if (thischunk_min_x < m_min_x) m_min_x = thischunk_min_x;
						if (thischunk_min_z < m_min_z) m_min_z = thischunk_min_z;
						if (thischunk_min_x + 16 > m_max_x) m_max_x = thischunk_min_x + 16;
						if (thischunk_min_z + 16 > m_max_z) m_max_z = thischunk_min_z + 16;
					}
				}
			}
		}

		m_size_x = m_max_x - m_min_x;
		m_size_z = m_max_z - m_min_z;

		MineSpy.logf("Map Size: %d x %d (%d chunks) [minx=%d,maxx=%d,minz=%d,maxz=%d]\n", m_size_x, m_size_z,
				totalChunks(), m_min_x, m_max_x, m_min_z, m_max_z);

		m_min_cx = m_min_x >> 4;
		m_min_cz = m_min_z >> 4;
		m_size_cx = m_size_x >> 4;
		m_size_cz = m_size_z >> 4;

		m_chunks = new IChunk[m_size_cz * m_size_cx];
		m_chunk_atomic_refcount = new AtomicInteger[m_size_cz * m_size_cx];

		// fill the atomic refcount array with objects...
		for (int i = 0; i < m_chunk_atomic_refcount.length; i++) {
			m_chunk_atomic_refcount[i] = new AtomicInteger(0);
		}
	}

	@Override
	public int minX() {
		return m_min_x;
	}

	@Override
	public int maxX() {
		return m_max_x;
	}

	@Override
	public int minZ() {
		return m_min_z;
	}

	@Override
	public int maxZ() {
		return m_max_z;
	}

	@Override
	public int sizeX() {
		return m_size_x;
	}

	@Override
	public int sizeZ() {
		return m_size_z;
	}

	@Override
	public int totalChunks() {
		return m_crmap.keySet().size();
	}

	@Override
	public void acquireChunks(ChunkCoord... ccs) {
		boolean chunks_to_acquire;
		// protect against incrementing the refcount more than once
		boolean[] acquired = new boolean[ccs.length];
		do {
			chunks_to_acquire = false;
			for (int i = 0; i < ccs.length; i++) {
				ChunkCoord cc = ccs[i];
				ChunkReference cr = m_crmap.get(cc);
				if (cr == null) continue;
				int index = (cc.z() - m_min_cz) * m_size_cx + (cc.x() - m_min_cx);
				IChunk c = cr.tryGet();
				if (c != null && acquired[i] == false) {
					acquired[i] = true;
					if (m_chunk_atomic_refcount[index].getAndIncrement() == 0) {
						// were no references. need to put chunk in table
						while (m_chunks[index] != null) {
							// wait for previous unload to finish
							// busy waiting because this should almost never need to wait
							// and lock overhead is too high
						}
						m_chunks[index] = m_cf == null ? c : m_cf.filter(c);
					}
				} else if (c == null) {
					chunks_to_acquire = true;
				}
				// the 'else' case is where we were able to get it but already have
			}
			// this will (and is intended to) burn cpu until all chunks are loaded
		} while (chunks_to_acquire);
	}

	@Override
	public void releaseChunks(ChunkCoord... ccs) {
		for (ChunkCoord cc : ccs) {
			ChunkReference cr = m_crmap.get(cc);
			if (cr == null) return;
			int index = (cc.z() - m_min_cz) * m_size_cx + (cc.x() - m_min_cx);
			if (m_chunk_atomic_refcount[index].decrementAndGet() == 0) {
				m_chunks[index] = null;
			}
		}
	}

	@Override
	public IChunkCoordIterator iterator() {
		return new ChunkCoordIterator();
	}

	@Override
	public YRun getYRun(int x, int z) {
		// this method _shouldn't_ need to be synchro
		IChunk c = null;
		if (x >= m_min_x && z >= m_min_z && x < m_max_x && z < m_max_z) {
			c = m_chunks[((z >> 4) - m_min_cz) * m_size_cx + ((x >> 4) - m_min_cx)];
		}
		if (c == null) return YRun.BLANK;
		return new YRun(c, x & 15, z & 15);
	}

	@Override
	public void setChunkFilter(IChunkFilter cf) {
		m_cf = cf;
	}
	
	private static class ChunkReference {

		private final RegionFile m_rf;
		private final ChunkCoord m_cc;
		private volatile SoftReference<Chunk> m_ref = new SoftReference<Chunk>(null);

		private static AtomicLong m_loadtime = new AtomicLong(0);

		private final Lock m_loadlock = new ReentrantLock();

		public ChunkReference(RegionFile rf_, ChunkCoord cc_) {
			m_rf = rf_;
			m_cc = cc_;
		}

		public static long getLoadTime() {
			return m_loadtime.get();
		}

		public Chunk get() {
			// looks like i dont really need this method anymore
			// avoid synchro overhead for retrieval and make loading lazy
			Chunk c = m_ref.get();
			if (c == null) {
				// chunk not loaded or chunk cleared by gc
				m_loadlock.lock();
				try {
					c = loadChunk();
				} finally {
					m_loadlock.unlock();
				}
			}
			return c;
		}

		public Chunk tryGet() {
			// avoid synchro overhead for retrieval and make loading lazy
			Chunk c = m_ref.get();
			if (c == null) {
				// chunk not loaded or chunk cleared by gc
				if (m_loadlock.tryLock()) {
					try {
						c = loadChunk();
					} finally {
						m_loadlock.unlock();
					}
				}
			}
			return c;
		}

		private Chunk loadChunk() {
			Chunk c = m_ref.get();
			if (c != null) return c;
			long tstart = System.nanoTime();
			try {
				c = new Chunk(Tag.parse(m_rf.getChunkDataInputStream(m_cc.x() & 31, m_cc.z() & 31)));
			} catch (IOException e) {
				// well shit.
				c = Chunk.BLANK;
			}
			m_ref = new SoftReference<Chunk>(c);
			m_loadtime.addAndGet(System.nanoTime() - tstart);
			return c;
		}

	}

	private class ChunkCoordIterator implements IChunkCoordIterator {

		private volatile int m_rf_index = 0;
		private volatile int m_r_cx = 0;
		private volatile int m_r_cz = 0;

		public ChunkCoordIterator() {

		}

		@Override
		public synchronized ChunkCoord next() {
			while (m_rf_index < m_rflist.size()) {
				RegionFile rf = m_rflist.get(m_rf_index);
				try {
					if (rf.hasChunk(m_r_cx, m_r_cz)) {
						return new ChunkCoord(rf.getRegionX() * 32 + m_r_cx, rf.getRegionZ() * 32 + m_r_cz);
					}
				} finally {
					// move to the next (possible) chunk even if we found one to return
					m_r_cz++;
					if (m_r_cz > 31) {
						m_r_cz = 0;
						m_r_cx++;
						if (m_r_cx > 31) {
							m_r_cx = 0;
							m_rf_index++;
						}
					}
				}
			}
			throw new NoMoreChunksException();
		}

	}

	@Override
	public void close() {
		for (RegionFile rf : m_rflist) {
			try {
				rf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
