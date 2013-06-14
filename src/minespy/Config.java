package minespy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import minespy.colorproviders.DataBiomeBlockColorProvider;
import minespy.colorproviders.DelegatingBlockColorProvider;
import minespy.colorproviders.IBlockColorProvider;
import minespy.mapshaders.*;

public final class Config {

	private static Config config = null;

	private final DelegatingBlockColorProvider cp;
	private final String[] blocknames = new String[4096];
	private final boolean[] blocktransparent = new boolean[4096];
	private final File mcdir;

	private final List<File> saves = new ArrayList<File>();

	private final Map<String, Preset> presets = new HashMap<String, Preset>();

	private static class Enumerator<T> implements Enumeration<T> {

		private Iterator<T> m_it;

		public Enumerator(Iterator<T> it) {
			m_it = it;
		}

		@Override
		public boolean hasMoreElements() {
			return m_it.hasNext();
		}

		@Override
		public T nextElement() {
			return m_it.next();
		}

	}

	private Config() {
		try {
			// TODO parse files individually so proper errors can be given!
			// need to add whitespace between files
			byte[] crlf = new byte[] { 13, 10 };

			// concatenate all config files as one input stream
			List<InputStream> configs = new ArrayList<InputStream>();

			InputStream config_internal = Config.class.getResourceAsStream("/minespy/main.minespyconfig");
			if (config_internal == null)
				throw new IOException("Failed to open 'main.minespyconfig' as class resource.");
			configs.add(config_internal);

			// get all config files in 'working directory'
			File[] config_files = new File(".").listFiles(new ConfigFilenameFilter());

			for (File f : config_files) {
				configs.add(new ByteArrayInputStream(crlf));
				configs.add(new FileInputStream(f));
				MineSpy.log("Found config file: " + f.getName());
			}

			// concatenated stream
			InputStream is = new SequenceInputStream(new Enumerator<InputStream>(configs.iterator()));

			// now parse concatenated streams
			File home = new File(System.getProperty("user.home"));

			cp = new DelegatingBlockColorProvider();

			Scanner scan = new Scanner(new InputStreamReader(is));
			scan.useDelimiter("\\s+");

			// parser state
			int blockid = -1;
			int data = -1;
			int datamask = 0xF;
			int biome = -1;
			int alpha = 0xFF;
			Map<Integer, DataBiomeBlockColorProvider> dbcp_map = new HashMap<Integer, DataBiomeBlockColorProvider>();
			List<String> mcdirs = new ArrayList<String>();
			Preset preset = new Preset("default");
			presets.put(preset.getName(), preset);

			try {
				while (scan.hasNext()) {
					if (scan.hasNext("//.*")) {
						// comment
						scan.nextLine();
					} else if (gobble(scan, "block")) {
						blockid = scan.nextInt();
						// reset data, datamask, biome values when changing blockid
						data = -1;
						datamask = 0xF;
						biome = -1;
					} else if (gobble(scan, "transparent")) { 
						// mark selected block id as transparent (game logic version)
						blocktransparent[blockid] = true;
					} else if (gobble(scan, "name")) {
						blocknames[blockid] = nextQuotedString(scan);
					} else if (gobble(scan, "color")) {
						int rgb = (alpha << 8) | scan.nextInt();
						// reset alpha for next color
						alpha = 0xFF;
						rgb <<= 8;
						rgb |= scan.nextInt();
						rgb <<= 8;
						rgb |= scan.nextInt();
						if (blockid >= 0 && blockid < 4096) {
							// let's not try to do stuff with invalid ids now shall we?
							DataBiomeBlockColorProvider dbcp = dbcp_map.get(blockid);
							if (dbcp != null) {
								// blockid-specific color provider exists, use it
								if (data >= 0 && biome >= 0) {
									// data, biome both specified
									dbcp.setDataBiomeColor(datamask, data, biome, rgb);
								} else if (biome >= 0) {
									// only biome specified
									dbcp.setBiomeColor(biome, rgb);
								} else if (data >= 0) {
									// only data specified
									dbcp.setDataColor(datamask, data, rgb);
								} else {
									// nothing specified
									dbcp.setColor(rgb);
								}
							} else {
								// blockid-specific color provider does not exist
								if (data < 0 && biome < 0) {
									// neither data nor biome specified, don't need to create it
									cp.setDefaultColor(blockid, rgb);
								} else {
									// data or biome specified, need to create it
									dbcp_map.put(blockid, dbcp = new DataBiomeBlockColorProvider());
									dbcp.setColor(cp.getDefaultColor(blockid));
									if (data >= 0 && biome >= 0) {
										// data, biome both specified
										dbcp.setDataBiomeColor(datamask, data, biome, rgb);
									} else if (biome >= 0) {
										// only biome specified
										dbcp.setBiomeColor(biome, rgb);
									} else if (data >= 0) {
										// only data specified
										dbcp.setDataColor(datamask, data, rgb);
									}
								}
							}
						}
					} else if (gobble(scan, "alpha")) {
						alpha = scan.nextInt();
					} else if (gobble(scan, "data")) {
						if (gobble(scan, "all")) {
							data = -1;
						} else {
							data = scan.nextInt();
						}
					} else if (gobble(scan, "datamask")) {
						if (gobble(scan, "all")) {
							datamask = 0xF;
						} else {
							datamask = scan.nextInt();
						}
					} else if (gobble(scan, "biome")) {
						if (gobble(scan, "all")) {
							biome = -1;
						} else {
							biome = scan.nextInt();
						}
					} else if (gobble(scan, "minecraftdir")) {
						mcdirs.add(nextQuotedString(scan));
					} else if (gobble(scan, "savedir")) {
						String s = nextQuotedString(scan);
						if (s.startsWith("~/")) {
							s = home.getPath() + s.substring(1);
						}
						addSaveDir(new File(s.replace('/', File.separatorChar)));
					}

					// preset commands
					else if (gobble(scan, "preset")) {
						String name = nextQuotedString(scan);
						preset = presets.get(name);
						if (preset == null) {
							preset = new Preset(name);
							presets.put(name, preset);
						}
					} else if (gobble(scan, "shader")) {
						String shader = scan.next();
						if ("block".equals(shader)) {
							preset.setMapShaderFactory(new BlockShader.Factory());
						} else if ("pretty".equals(shader)) {
							preset.setMapShaderFactory(new PrettyShader.Factory());
						} else if ("hmap-grey".equals(shader)) {
							preset.setMapShaderFactory(new HeightGreyscaleShader.Factory());
						} else if ("hmap-colour".equals(shader)) {
							preset.setMapShaderFactory(new HeightColorShader.Factory());
						} else if ("biome".equals(shader)) {
							preset.setMapShaderFactory(new BiomeShader.Factory());
						} else if ("blocklight".equals(shader)) {
							preset.setMapShaderFactory(new BlocklightShader.Factory());
						} else {
							throw new IOException("Bad shader name: " + shader);
						}
					} else if (gobble(scan, "skylight")) {
						preset.setSkylight(scan.nextInt());
					} else if (gobble(scan, "forcebrightness")) {
						String mode = scan.next();
						if ("on".equals(mode)) {
							preset.setForceBrightness(true);
						} else if ("off".equals(mode)) {
							preset.setForceBrightness(false);
						} else {
							throw new IOException("Bad forcebrightness param: " + mode);
						}
					} else if (gobble(scan, "cavemode")) {
						String mode = scan.next();
						if ("on".equals(mode)) {
							preset.setCaveMode(true);
						} else if ("off".equals(mode)) {
							preset.setCaveMode(false);
						} else {
							throw new IOException("Bad cavemode param: " + mode);
						}
					} else if (gobble(scan, "showmarkers")) {
						String mode = scan.next();
						if ("on".equals(mode)) {
							preset.setShowMarkers(true);
						} else if ("off".equals(mode)) {
							preset.setShowMarkers(false);
						} else {
							throw new IOException("Bad showmarkers param: " + mode);
						}
					} else if (gobble(scan, "includeblocks")) {
						preset.setBlockFilter(MineSpy.parseIntArray(nextQuotedString(scan)));
						preset.setBlockFilterInclusive(true);
						preset.setBlockFilterEnabled(true);
					} else if (gobble(scan, "excludeblocks")) {
						preset.setBlockFilter(MineSpy.parseIntArray(nextQuotedString(scan)));
						preset.setBlockFilterInclusive(false);
						preset.setBlockFilterEnabled(true);
					} else if (gobble(scan, "unfilterblocks")) {
						preset.setBlockFilterEnabled(false);
					} else if (gobble(scan, "includelevels")) {
						preset.setLevelFilter(MineSpy.parseIntArray(nextQuotedString(scan)));
						preset.setLevelFilterInclusive(true);
						preset.setLevelFilterEnabled(true);
					} else if (gobble(scan, "excludelevels")) {
						preset.setLevelFilter(MineSpy.parseIntArray(nextQuotedString(scan)));
						preset.setLevelFilterInclusive(false);
						preset.setLevelFilterEnabled(true);
					} else if (gobble(scan, "unfilterlevels")) {
						preset.setLevelFilterEnabled(false);
					} else if (gobble(scan, "throttle")) {
						preset.setThrottle(scan.nextDouble());
					}

					else {
						throw new IOException("Unknown command: " + scan.next());
					}
				}
			} catch (Exception e) {
				// TODO proper line numbers on config error
				throw new IOException("Error reading a *.minespyconfig file.", e);
			}

			scan.close();

			// put any blockid-specific color providers into the main one
			for (Map.Entry<Integer, DataBiomeBlockColorProvider> e : dbcp_map.entrySet()) {
				cp.setBlockColorProvider(e.getKey(), e.getValue());
			}

			// find minecraft directory
			File f = null;
			if (home.exists()) {
				for (String s : mcdirs) {
					if (s.startsWith("~/")) {
						s = home.getPath() + s.substring(1);
					}
					f = new File(s.replace('/', File.separatorChar));
					if (f.exists() && f.isDirectory()) {
						File f2 = new File(f.getPath() + File.separator + "bin" + File.separator + "minecraft.jar");
						if (f2.exists()) {
							// found it!
							addSaveDir(new File(f.getPath() + File.separator + "saves"));
							break;
						}
					}
				}
			}
			// null if not found
			mcdir = f;

			MineSpy.log("Config loaded.");

		} catch (Exception e) {
			throw new ConfigLoadException("Unable to load config.", e);
		}
	}

	private void addSaveDir(File dir) {
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				if (f.isDirectory()) {
					File f2 = new File(f.getPath() + File.separator + "level.dat");
					if (f2.exists()) {
						saves.add(f);
					}
				}
			}
		}
	}

	private static String nextQuotedString(Scanner scan) {
		String s;
		scan.useDelimiter("\"");
		gobble(scan, "\\s+");
		s = scan.next();
		scan.useDelimiter("\\s+");
		gobble(scan, "\"");
		return s;
	}

	private static boolean gobble(Scanner scan, String pattern) {
		if (scan.hasNext(pattern)) {
			scan.next(pattern);
			return true;
		}
		return false;
	}

	public static Config getConfig() {
		if (config == null) config = new Config();
		return config;
	}

	public IBlockColorProvider getBlockColorProvider() {
		return cp;
	}

	public String getBlockName(int blockid) {
		if (blocknames[blockid] != null) return blocknames[blockid];
		return "";
	}
	
	public boolean isBlockTransparent(int blockid) {
		return blocktransparent[blockid];
	}

	public File getMinecraftDir() {
		return mcdir;
	}

	public File[] getMinecraftSaveDirs() {
		File[] files = new File[saves.size()];
		return saves.toArray(files);
	}

	public Preset getPreset(String name) {
		return presets.get(name);
	}

	public Preset[] getPresets() {
		Preset[] p = new Preset[presets.values().size()];
		return presets.values().toArray(p);
	}

	public void savePreset(Preset p) {
		// TODO
	}

}
