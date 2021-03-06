package peakaboo.common;

import java.io.File;

import commonenvironment.Env;

public class Configuration {

	public static File appDir() {
		return Env.appDataDirectory(Version.program_name + Version.versionNoMajor);
	}
	public static File appDir(String subdir) {
		return Env.appDataDirectory(Version.program_name + Version.versionNoMajor, subdir);
	}
}
