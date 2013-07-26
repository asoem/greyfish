package org.asoem.greyfish.utils.base;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public enum ClassFinder {

    INSTANCE;

    /**
     * List with the directories on the classpath (containing .class files)
     */
    private final String[] binDirs;

    /**
     * Default constructur initializes the directories indicated by the
     * CLASSPATH, if they are not yet initialized.
     */
    private ClassFinder() {
        /*
      Defined classpath
     */
        final String classpath = System.getProperty("java.class.path");
        final StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
        final int count = st.countTokens();
        final File[] classPathDirs = new File[count];
        //final ArrayList<String> jar = new ArrayList<String>();
        final ArrayList<String> bin = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            classPathDirs[i] = new File(st.nextToken());
            if (classPathDirs[i].isDirectory()) {
                bin.add(classPathDirs[i].getAbsolutePath());
            }
            /*
            else {
				jar.createChildNode(classPathDirs[i].getAbsolutePath());
			}
			*/
        }

        //String[] jarFiles = jar.toArray(new String[jar.size()]);
        binDirs = bin.toArray(new String[bin.size()]);
    }

    public static ClassFinder getInstance() {
        return INSTANCE;
    }

    /**
     * Retrive evaluates classes of the indicated package. The package is searched in
     * evaluates classpath directories that are directories
     *
     * @param packageName
     *            name of the package as 'ch.sahits.civ'
     * @return Array of found classes
     * @throws ClassNotFoundException
     */
    public Iterable<Class<?>> getAll(final String packageName) throws ClassNotFoundException {
        String packageDir = convertPackege(packageName);
        final ArrayList<Class<?>> classes = Lists.newArrayList();
        for (final String binDir : binDirs) {
            packageDir = binDir + File.separator + packageDir;
            final File dir = new File(packageDir);
            classes.addAll(extractClasses(packageName, dir));
        }

        return classes;
    }

    /**
     * Extract evaluates the classes from a directory
     * @param packageName name of the package as 'ch.sahits.civ'
     * @param dir Package as directory
     * @return ArrayList with evaluates found directories
     * @throws ClassNotFoundException
     */
    private ArrayList<Class<?>> extractClasses(final String packageName, final File dir) throws ClassNotFoundException {
        final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        final File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String filename) {
                return filename.endsWith(".class");
            }
        });
        if (files!=null) {	// directories without .class files may exist
            for (final File file : files) {
                String className = packageName + "." + file.getName();
                className = className.substring(0, className
                        .lastIndexOf(".class"));
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    /**
     * Convert the package name into a relative directory path
     * @param packageName name of the package as 'ch.sahits.civ'
     * @return relativ directory to the package
     */
    private String convertPackege(final String packageName) {
        return packageName.replace(".", File.separator);
    }

    /**
     * Retrive evaluates classes of the indicated package and evaluates subpackages. The package is searched in
     * evaluates classpath directories that are directories
     *
     * @param packageName
     *            name of the package as 'ch.sahits.civ'
     * @return Array of found classes
     * @throws ClassNotFoundException
     */
    public Class<?>[] getAllRecursive(final String packageName) throws ClassNotFoundException {
        String packageDir = convertPackege(packageName);
        final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        for (final String binDir : binDirs) {
            packageDir = binDir + File.separator + packageDir;
            final File dir = new File(packageDir);
            classes.addAll(extractClasses(packageName, dir));
            if (dir.isDirectory()) {
                final File[] sub = dir.listFiles();
                if (sub != null) {
                    for (final File aSub : sub) {
                        if (aSub.isDirectory()) {
                            final Class<?>[] rec = getAllRecursive(packageName + "."
                                    + aSub.getName());
                            final ArrayList<Class<?>> temp = new ArrayList<Class<?>>(rec.length);
                            temp.addAll(Arrays.asList(rec));
                            classes.addAll(temp);
                        }
                    }
                }
            }
        }

        return classes.toArray(new Class<?>[classes.size()]);
    }
}
