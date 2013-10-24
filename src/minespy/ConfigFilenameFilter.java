package minespy;

import java.io.File;
import java.io.FilenameFilter;

public class ConfigFilenameFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		return name.matches("^.*\\.minespyconfig$");
	}

}
