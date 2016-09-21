package com.florian.regexfindandreplace;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class ClassPathProvider implements IClassPathProvider {

	@Override
	public String getClassPath() {
		Bundle bundle = Platform.getBundle("com.florian.regexfindandreplace");
		File bundleLocation = new File(bundle.getLocation().substring("reference:file:".length()));
		StringBuilder sb = new StringBuilder("\"");
		if (bundleLocation.getName().toLowerCase().endsWith(".jar")) {
			// The classpath in a jar is found by looking at the Class-Path
			// attribute in MANIFEST.mf
			// see
			// http://stackoverflow.com/questions/32226317/java-ignoring-class-path-entry-in-jar-files-manifest
			sb.append(bundleLocation.getAbsolutePath()).append(";");
		} else // The plugin isn't installed, started from the IDE
		{
			sb.append(new File(new File(bundleLocation, "lib"), "regex-0.0.1-SNAPSHOT.jar").getAbsolutePath())
					.append(";");
			sb.append(new File(bundleLocation, "bin").getAbsolutePath()).append(";");
		}
		sb.append("\"");
		return sb.toString();
	}

}
