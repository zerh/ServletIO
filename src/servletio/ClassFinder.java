package servletio;

import java.io.File;
import java.net.URL;
import java.util.*;

public class ClassFinder {

  private static final char   DOT               = '.';
  private static final char   SLASH             = '/';
  private static final String CLASS_SUFFIX      = ".class";
  private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

  public static List<Class<?>> find(final String scannedPackage) {
    final String scannedPath = scannedPackage.replace(DOT, SLASH);
    final URL scannedUrl     = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
    
    if (scannedUrl == null) {
      throw new IllegalArgumentException(String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage));
    }
    
    final File scannedDir = new File(scannedUrl.getFile());
    final List<Class<?>> classes = new ArrayList<>();
    for (final File file : scannedDir.listFiles()) {
      classes.addAll(find(file, scannedPackage));
    }
    return classes;
  }

  private static List<Class<?>> find(final File file, final String scannedPackage) {
    List<Class<?>> classes = new ArrayList<>();
    String resource = scannedPackage + DOT + file.getName();
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        classes.addAll(find(child, resource));
      }
    } else if (resource.endsWith(CLASS_SUFFIX)) {
      int endIndex = resource.length() - CLASS_SUFFIX.length();
      String className = resource.substring(0, endIndex);
      try {
        classes.add(Class.forName(className));
      } catch (ClassNotFoundException ignore) {
      }
    }
    return classes;
  }

}