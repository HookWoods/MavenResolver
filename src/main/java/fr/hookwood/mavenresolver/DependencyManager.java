package fr.hookwood.mavenresolver;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class DependencyManager {

    private Method method;
    private URLClassLoader classLoader;
    private List<Dependency> toLoad;

    /**
     * @param mainClass The main class of the application / plugin (Spigot, BungeeCoord, ...)
     */
    public DependencyManager(Class<?> mainClass) {
        this.toLoad = new ArrayList<>();

        if (mainClass.getClassLoader() instanceof URLClassLoader) {
            this.classLoader = (URLClassLoader) mainClass.getClassLoader();
        } else {
            throw new ClassCastException("Error while loading URLClassLoader");
        }

        try {
            this.method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            this.method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function allow to put the dependency in a list before being downloaded in parallel.
     *
     * @param dependency The object of the dependency we want to download
     */
    public void preLoad(Dependency dependency) {
        if (!this.toLoad.contains(dependency)) {
            if (dependency.getArtifactId() == null || dependency.getVersion() == null || dependency.getGroupId() == null) {
                return;
            }
            if (dependency.getArtifactId().isEmpty() || dependency.getVersion().isEmpty() || dependency.getGroupId().isEmpty()) {
                return;
            }

            this.toLoad.add(dependency);
        }
    }

    /**
     * This function start the process to download and inject the dependencies.
     *
     * @param libsFolder the folder where the dependency will be placed
     */
    public void load(File libsFolder) {
        DependencyDownloader dependencyDownloader = new DependencyDownloader();
        dependencyDownloader.download(this.toLoad, libsFolder, dependencyFile -> {
            if (dependencyFile != null) {
                injectJar(dependencyFile);
            }
        });
    }

    /**
     * @param jarFile the file of the jar to load all the class of a dependency dynamically
     */
    private void injectJar(File jarFile) {
        try {
            this.method.invoke(this.classLoader, jarFile.toURI().toURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
