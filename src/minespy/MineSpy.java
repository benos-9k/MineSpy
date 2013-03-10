package minespy;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import minespy.gui.MainFrame;
import minespy.nbt.Tag;

public class MineSpy {

	private static List<PrintWriter> logouts = new ArrayList<PrintWriter>();

	public static void main(String[] args) throws Exception {

		if (args.length == 0) {
			// run gui
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						MainFrame mf = new MainFrame();
						mf.setLocationRelativeTo(null);
						mf.setVisible(true);
						mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					} catch (ConfigLoadException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null,
								"Unable to load configuration file. Please run from console for details.", "MineSpy",
								JOptionPane.ERROR_MESSAGE);
						System.exit(1);
					}
				}
			});
		} else {
			// run cmdline
			Preset p = Config.getConfig().getPreset(args[0]);
			File worlddir = new File(args[1]);
			Dimension dim = Dimension.OVERWORLD;
			if (args.length > 2 && args[2].equals("nether")) {
				dim = Dimension.NETHER;
			} else if (args.length > 2 && args[2].equals("end")) {
				dim = Dimension.END;
			}
			renderPreset(p, worlddir, dim, new IRenderListener() {

				@Override
				public void notifyRenderer(Renderer r) {

				}

				@Override
				public void notifyProgress(int progress, int total) {
					System.out.printf("\r%d / %d [%d%%] chunks rendered.", progress, total, progress * 100 / total);
				}

				@Override
				public void notifyTermination(Exception te) {
					System.out.println();
				}

			});
		}
	}

	public static void renderPreset(final Preset p, final File worlddir, final Dimension dim, final IRenderListener rl) {
		new Thread() {

			@Override
			public void run() {
				// TODO this is kinda ugly
				try {
					long time_render_start = System.currentTimeMillis();

					final File imgfile = new File(worlddir.getName().replaceAll("\\s+", "_") + "_" + dim.getFileName()
							+ "_" + p.getFileName() + ".png");

					File regiondir = new File(worlddir.getPath());
					if (dim.getIndex() != 0) {
						regiondir = new File(worlddir.getPath() + File.separator + "DIM" + dim.getIndex());
					}

					regiondir = new File(regiondir.getPath() + File.separator + "region");

					if (!regiondir.isDirectory()) {
						throw new RuntimeException("World directory is not valid.");
					}

					// get the coords of all players (in this dimension)
					Map<String, double[]> players = new HashMap<String, double[]>();
					double[] pos_worldspawn = new double[3];
					if (p.getShowMarkers()) {
						Tag level = Tag.parse(new DataInputStream(new GZIPInputStream(new FileInputStream(worlddir
								.getPath() + File.separator + "level.dat"))));
						pos_worldspawn[0] = level.getDouble("Data", "SpawnX");
						pos_worldspawn[1] = level.getDouble("Data", "SpawnY");
						pos_worldspawn[2] = level.getDouble("Data", "SpawnZ");
						for (File f : new File(worlddir.getPath() + File.separator + "players").listFiles()) {
							Tag player = Tag.parse(new DataInputStream(new GZIPInputStream(new FileInputStream(f))));
							int playerdim = player.getInt("Dimension");
							if (playerdim != dim.getIndex()) continue;
							double[] pos = new double[3];
							pos[0] = player.getDouble("Pos", 0);
							pos[1] = player.getDouble("Pos", 1);
							pos[2] = player.getDouble("Pos", 2);
							String name = f.getName().replaceFirst("\\.dat$", "");
							players.put(name, pos);
						}
					}

					try {
						if (!imgfile.canWrite() && !imgfile.createNewFile()) {
							// (doesnt exist or isnt writable) and (cant create it)
							// wont be able to write image
							throw new RuntimeException("Image file is not valid.");
						}
					} catch (IOException e) {
						// trying to create failed
						throw new RuntimeException("Image file is not valid.", e);
					}

					IWorld world = new RegionFileWorld(regiondir.listFiles(new AnvilRegionFilenameFilter()));

					world.setChunkFilter(p.getChunkFilter());

					final Renderer r = new Renderer(world, Config.getConfig().getBlockColorProvider(),
							p.getMapShaderFactory(), p.getSkylight());
					if (rl != null) {
						r.addRenderListener(rl);
					}
					r.setThrottle(p.getThrottle());

					try {
						MineSpy.log("Starting render...");
						r.start();

						MineSpy.log("Waiting for renderer to finish...");
						r.waitUntilDone();
						double time_render = (System.currentTimeMillis() - time_render_start) / 1000d;
						MineSpy.logf("Rendering done in %.3f seconds at %.1f chunks per second.\n", time_render,
								world.totalChunks() / time_render);

						// draw markers
						if (p.getShowMarkers()) {
							Graphics2D g = r.getImage().createGraphics();
							g.translate(-world.minX(), -world.minZ());
							Composite c = g.getComposite();
							Composite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);

							// 0,0
							g.setComposite(ac);
							g.setColor(Color.RED);
							g.fillOval(0 - 4, 0 - 4, 9, 9);
							g.setComposite(c);
							g.setColor(Color.BLACK);
							g.drawLine(0, 0 - 19, 0, 0);
							g.setComposite(ac);
							g.fillRect(0, 0 - 19, g.getFontMetrics().stringWidth("Origin") + 5, 14);
							g.setColor(Color.WHITE);
							g.drawString("Origin", 0 + 3, 0 - 8);

							// world spawn (overworld only?)
							if (dim.equals(Dimension.OVERWORLD)) {
								int sx = (int) pos_worldspawn[0];
								int sz = (int) pos_worldspawn[2];
								g.setComposite(ac);
								g.setColor(Color.RED);
								g.fillOval(sx - 4, sz - 4, 9, 9);
								g.setComposite(c);
								g.setColor(Color.BLACK);
								g.drawLine(sx, sz - 19, sx, sz);
								g.setComposite(ac);
								g.fillRect(sx, sz - 19, g.getFontMetrics().stringWidth("Spawn") + 5, 14);
								g.setColor(Color.WHITE);
								g.drawString("Spawn", sx + 3, sz - 8);
							}

							// players
							for (Map.Entry<String, double[]> e : players.entrySet()) {

								String name = e.getKey();
								double[] pos = e.getValue();
								int px = (int) pos[0];
								int pz = (int) pos[2];

								g.setComposite(ac);
								g.setColor(Color.MAGENTA);
								g.fillOval(px - 4, pz - 4, 9, 9);
								g.setComposite(c);
								g.setColor(Color.BLACK);
								g.drawLine(px, pz - 19, px, pz);
								g.setComposite(ac);
								g.fillRect(px, pz - 19, g.getFontMetrics().stringWidth(name) + 5, 14);
								g.setColor(Color.WHITE);
								g.drawString(name, px + 3, pz - 8);

							}
						}

						try {
							MineSpy.logf("Writing image to file '%s'...\n", imgfile.getName());
							long time_imgwrite_start = System.currentTimeMillis();
							ImageIO.write(r.getImage(), "PNG", imgfile);
							MineSpy.logf("Image written to file in %.3f seconds.\n",
									(System.currentTimeMillis() - time_imgwrite_start) / 1000d);
						} catch (Exception e) {
							e.printStackTrace();
							MineSpy.log("Error writing '" + imgfile.getName() + "'.");
						}

						MineSpy.logf("Done in %.3f seconds.\n",
								(System.currentTimeMillis() - time_render_start) / 1000d);

						world.close();

					} catch (Exception e) {
						e.printStackTrace();
						MineSpy.log("Error during render. Please run from console for details.");
					}

				} catch (Exception e) {
					e.printStackTrace();
					MineSpy.log("Error preparing to start render:");
					MineSpy.log(e.getMessage());
				}
				System.gc();
			}

		}.start();
	}

	public static synchronized void logf(String fmt, Object... args) {
		String s = String.format(fmt, args);
		System.out.print(s);
		for (PrintWriter p : logouts) {
			p.print(s);
			p.flush();
		}
	}

	public static synchronized void log(String s) {
		logf("%s\n", s);
	}

	public static synchronized void addLogOutput(PrintWriter p) {
		logouts.add(p);
	}

}
