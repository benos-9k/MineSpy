package minespy;

import java.io.File;
import java.io.FilenameFilter;

public class AnvilRegionFilenameFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		// r.x.z.mca
		return name.matches("^r\\.-?[0-9]+\\.-?[0-9]+\\.mca$");
	}

}
