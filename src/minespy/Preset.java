package minespy;

import minespy.chunkfilters.BlockExcludeFilter;
import minespy.chunkfilters.BlockIncludeFilter;
import minespy.chunkfilters.CaveFilter;
import minespy.chunkfilters.ComposedChunkFilter;
import minespy.chunkfilters.ForceBrightnessFilter;
import minespy.chunkfilters.IChunkFilter;
import minespy.chunkfilters.LevelExcludeFilter;
import minespy.chunkfilters.LevelIncludeFilter;
import minespy.mapshaders.BlockShader;
import minespy.mapshaders.IMapShaderFactory;

public class Preset {

	private String name;
	private IMapShaderFactory msf = new BlockShader.Factory();
	private int skylight = 15;

	private boolean do_forcebrightness = false;
	private boolean do_cavemode = false;
	private boolean do_showmarkers = false;

	private boolean do_blockfilter = false, blockfilter_include = false;
	private int[] blockfilter = null;

	private boolean do_levelfilter = false, levelfilter_include = false;
	private int[] levelfilter = null;

	private String fname = null;
	private double throttle = 1.0;

	public Preset() {
		this(null);
	}

	public Preset(String name_) {
		name = name_;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IMapShaderFactory getMapShaderFactory() {
		return msf;
	}

	public void setMapShaderFactory(IMapShaderFactory msf) {
		if (msf == null) throw new NullPointerException();
		this.msf = msf;
	}

	public int getSkylight() {
		return skylight;
	}

	public void setSkylight(int skylight) {
		this.skylight = skylight;
	}

	public void setForceBrightness(boolean b) {
		do_forcebrightness = b;
	}
	
	public void setCaveMode(boolean b) {
		do_cavemode = b;
	}
	
	public void setShowMarkers(boolean b) {
		do_showmarkers = b;
	}
	
	public boolean getShowMarkers() {
		return do_showmarkers;
	}

	public void setBlockFilterEnabled(boolean b) {
		do_blockfilter = b;
	}

	public void setLevelFilterEnabled(boolean b) {
		do_levelfilter = b;
	}

	public void setBlockFilterInclusive(boolean b) {
		blockfilter_include = b;
	}

	public void setLevelFilterInclusive(boolean b) {
		levelfilter_include = b;
	}

	public void setBlockFilter(int[] f) {
		if (f == null) throw new NullPointerException();
		blockfilter = f;
	}

	public void setLevelFilter(int[] f) {
		if (f == null) throw new NullPointerException();
		levelfilter = f;
	}

	public IChunkFilter getChunkFilter() {
		ComposedChunkFilter ccf = new ComposedChunkFilter();
		if (do_forcebrightness) {
			ccf.addOuter(new ForceBrightnessFilter());
		}
		if (do_blockfilter && blockfilter.length > 0) {
			if (blockfilter_include) {
				ccf.addOuter(new BlockIncludeFilter(blockfilter));
			} else {
				ccf.addOuter(new BlockExcludeFilter(blockfilter));
			}
		}
		if (do_levelfilter && levelfilter.length > 0) {
			if (levelfilter_include) {
				ccf.addOuter(new LevelIncludeFilter(levelfilter));
			} else {
				ccf.addOuter(new LevelExcludeFilter(levelfilter));
			}
		}
		if (do_cavemode) {
			ccf.addOuter(new CaveFilter());
		}
		return ccf;
	}

	public String getFileName() {
		// this gets appended to the name of the world
		if (fname != null) return fname;
		if (name != null) return name.replace(' ', '_');
		return String.format("%s_light%d_%s", msf.getFileName(), skylight, getChunkFilter()
				.getFileName());
	}

	public void setFileName(String fname) {
		this.fname = fname;
	}

	public double getThrottle() {
		return throttle;
	}

	public void setThrottle(double f) {
		throttle = f;
	}

}
