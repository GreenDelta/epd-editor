package app.rcp;

import app.App;
import org.openlca.ilcd.commons.Copyable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads and writes values from and to the *.ini file which is located in the
 * installation directory of the application. Writing is not possible when it is
 * installed in a read-only folder. This class is independent of the user
 * interface and could be re-used in other packages if needed.
 */
public class IniFile implements Copyable<IniFile> {

	public String lang = "en";
	public int maxMemory = 1024;

	public static IniFile read() {
		try {
			File iniFile = getIniFile();
			if (!iniFile.exists())
				return new IniFile();
			return parseFile(iniFile);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(IniFile.class);
			log.error("failed to read *.ini file", e);
			return new IniFile();
		}
	}

	public void write() {
		Logger log = LoggerFactory.getLogger(IniFile.class);
		try {
			File iniFile = getIniFile();
			if (!iniFile.exists())
				return;
			List<String> oldLines = Files.readAllLines(iniFile.toPath());
			List<String> newLines = new ArrayList<>();
			boolean nextIsLanguage = false;
			for (String line : oldLines) {
				if (line.trim().equals("-nl")) {
					nextIsLanguage = true;
					newLines.add(line);
				} else if (nextIsLanguage) {
					nextIsLanguage = false;
					newLines.add(lang);
				} else if (line.trim().startsWith("-Xmx")) {
					newLines.add("-Xmx" + maxMemory + "M");
				} else {
					newLines.add(line);
				}
			}
			Files.write(iniFile.toPath(), newLines);
			log.info("wrote ini file {}", iniFile);
		} catch (Exception e) {
			log.error("failed to write *.ini file", e);
		}
	}

	private static File getIniFile() {
		var dir = App.getInstallLocation();
		return new File(dir, "EPDEditor.ini");
	}

	private static IniFile parseFile(File iniFile) throws Exception {
		List<String> lines = Files.readAllLines(iniFile.toPath());
		IniFile ini = new IniFile();
		boolean nextIsLanguage = false;
		for (String line : lines) {
			if (line.trim().equals("-nl")) {
				nextIsLanguage = true;
				continue;
			}
			if (nextIsLanguage) {
				ini.lang = line.trim();
				nextIsLanguage = false;
			} else if (line.trim().startsWith("-Xmx")) {
				readMemory(line, ini);
			}
		}
		return ini;
	}

	private static void readMemory(String line, IniFile ini) {
		if (line == null || ini == null)
			return;
		String memStr = line.trim().toLowerCase();
		Pattern pattern = Pattern.compile("-xmx([0-9]+)m");
		Matcher matcher = pattern.matcher(memStr);
		if (!matcher.find()) {
			Logger log = LoggerFactory.getLogger(IniFile.class);
			log.warn("could not extract memory value from "
				+ "{} with -xmx([0-9]+)m", memStr);
			return;
		}
		try {
			ini.maxMemory = Integer.parseInt(matcher.group(1));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(IniFile.class);
			log.error("failed to parse memory value from ini: " + memStr, e);
		}
	}

	@Override
	public IniFile copy() {
		IniFile clone = new IniFile();
		clone.lang = lang;
		clone.maxMemory = maxMemory;
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (!(obj instanceof IniFile other))
			return false;
		return Objects.equals(this.lang, other.lang)
			&& this.maxMemory == other.maxMemory;
	}
}
