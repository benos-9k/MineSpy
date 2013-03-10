package minespy;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import minespy.colorproviders.IBlockColorProvider;
import minespy.mapshaders.IMapShader;
import minespy.mapshaders.IMapShaderFactory;

public class Renderer {

	private final IWorld m_world;
	private final IBlockColorProvider m_cp;
	private final IMapShaderFactory m_shaderfac;
	private final int m_skylight;
	private final BufferedImage m_img;
	private final RenderThread[] m_threads;
	private final IChunkCoordIterator m_itr;
	private final AtomicInteger m_progcount = new AtomicInteger(0);

	private final Set<IRenderListener> m_listeners = Collections.synchronizedSet(new HashSet<IRenderListener>());

	private volatile boolean m_terminate = false;
	private volatile Exception m_terminate_exception = null;
	private final AtomicInteger m_terminate_count = new AtomicInteger(0);

	private volatile double throttle = 1.0;

	public Renderer(IWorld world_, IBlockColorProvider cp_, IMapShaderFactory shaderfac_, int skylight_) {
		m_world = world_;
		m_itr = m_world.iterator();
		m_cp = cp_;
		m_shaderfac = shaderfac_;
		m_skylight = skylight_;
		m_img = new BufferedImage(m_world.sizeX(), m_world.sizeZ(), BufferedImage.TYPE_INT_ARGB);
		m_threads = new RenderThread[Runtime.getRuntime().availableProcessors()];
		MineSpy.logf("Renderer using %d thread(s).\n", m_threads.length);
		for (int i = 0; i < m_threads.length; i++) {
			m_threads[i] = new RenderThread();
		}
	}

	public void setThrottle(double f) {
		throttle = f;
	}

	public boolean addRenderListener(IRenderListener rl) {
		if (rl == null) throw new NullPointerException();
		if (m_listeners.add(rl)) {
			rl.notifyRenderer(this);
			rl.notifyProgress(m_progcount.get(), m_world.totalChunks());
			return true;
		}
		return false;
	}

	public boolean removeRenderListener(IRenderListener rl) {
		return m_listeners.remove(rl);
	}

	public BufferedImage getImage() {
		return m_img;
	}

	public void start() {
		for (RenderThread t : m_threads) {
			t.start();
		}
	}

	public int chunkProgress() {
		return m_progcount.get();
	}

	public int totalChunks() {
		return m_world.totalChunks();
	}

	public void waitUntilDone() throws AbnormalTerminationException {
		for (RenderThread t : m_threads) {
			while (t.isAlive()) {
				try {
					t.join();
				} catch (InterruptedException e) {

				}
			}
		}
		if (m_terminate_exception != null) {
			throw new AbnormalTerminationException(m_terminate_exception);
		}
	}

	public void kill() {
		m_terminate = true;
		for (RenderThread t : m_threads) {
			while (t.isAlive()) {
				try {
					t.join();
				} catch (InterruptedException e) {

				}
			}
		}
	}

	private class RenderThread extends Thread {

		public RenderThread() {

		}

		@Override
		public void run() {
			IMapShader shader = m_shaderfac.createInstance();
			try {
				while (!m_terminate) {
					long tstart = System.currentTimeMillis();

					// get a chunk-coordinate to render
					ChunkCoord cc = m_itr.next();

					// work out where it goes in the image
					int img_x = cc.x() * 16 - m_world.minX();
					int img_z = cc.z() * 16 - m_world.minZ();

					// render!
					shader.acquireChunks(m_world, cc);
					for (int cx = 0; cx < 16; cx++) {
						for (int cz = 0; cz < 16; cz++) {
							m_img.setRGB(img_x + cx, img_z + cz,
									shader.shade(m_world, cc.x() * 16 + cx, cc.z() * 16 + cz, m_cp, m_skylight));
						}
					}
					shader.releaseChunks(m_world, cc);

					int prog = m_progcount.incrementAndGet();
					if ((prog & 0x1FF) == 0) {
						// every 512 chunks send progress notification
						for (IRenderListener rpl : m_listeners) {
							rpl.notifyProgress(prog, m_world.totalChunks());
						}
					}

					long time = System.currentTimeMillis() - tstart;
					long tsleep = (long) ((1d - throttle) * (time / throttle));

					// this should _not_ sleep at all if throttle set to 100%
					if (tsleep > 0) {
						try {
							Thread.sleep(tsleep);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}
			} catch (NoMoreChunksException e) {
				// done
			} catch (Exception e) {
				// abnormal termination
				m_terminate_exception = e;
				m_terminate = true;
			}
			if (m_terminate_count.incrementAndGet() == m_threads.length) {
				// last thread to finish
				for (IRenderListener rl : m_listeners) {
					rl.notifyProgress(m_progcount.get(), m_world.totalChunks());
					rl.notifyTermination(m_terminate_exception);
				}
			}
		}

	}

}
