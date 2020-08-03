package fr.hookwood.mavenresolver;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DependencyDownloader {

    /**
     * This function check if the dependency is already downloaded or not and download it if needed
     *
     * @param dependency    the object Depency that contains group, artifact name and version of the jar
     * @param outputDir     the directory where the dependency will be downloaded
     * @param afterDownload it's used to load the jar only if the file has been downloaded and exist
     * @return the dependency object
     */
    public Dependency downloadDependency(Dependency dependency, File outputDir, Consumer<File> afterDownload) {
        String name = dependency.getArtifactId() + "-" + dependency.getVersion() + ".jar";
        File dependencyFile = new File(outputDir + File.separator + name);
        boolean fileExists = dependencyFile.exists() && dependencyFile.isFile();

        if (fileExists) {
            if (checksumDependency(dependency, dependencyFile)) {
                afterDownload.accept(dependencyFile);
            }
            return dependency;
        }

        download(dependency.getURLName(), dependencyFile);

        boolean fileExist2 = dependencyFile.exists() && dependencyFile.isFile();
        if (fileExist2) {
            if (checksumDependency(dependency, dependencyFile)) {
                afterDownload.accept(dependencyFile);
            }
            return dependency;
        }

        afterDownload.accept(null);
        return null;
    }

    /**
     * This function allow us to download a file (here the jar)
     *
     * @param urlLink        the url of the file we wanna download
     * @param dependencyFile the directory where the dependency will be downloaded
     */
    public void download(String urlLink, File dependencyFile) {
        try {
            URL url = new URL(urlLink);
            if (!dependencyFile.exists()) {
                dependencyFile.mkdirs();
            }
            Path targetPath = dependencyFile.toPath();
            Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This function allow to download multiple file at one with the java Future API
     *
     * @param dependencies  the list of dependencies to download at same time
     * @param outputDir     he directory where the dependency will be downloaded
     * @param afterDownload it's used to load the jar only if the file has been downloaded and exist
     */
    public void download(List<Dependency> dependencies, File outputDir, Consumer<File> afterDownload) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<Dependency>> futures = new ArrayList<>();
        for (final Dependency dependency : dependencies) {
            futures.add(executorService.submit(() -> downloadDependency(dependency, outputDir, afterDownload)));
        }
        for (Future<Dependency> future : futures) {
            try {
                future.get();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * This function will check if the sha1 of the remote depency and the sha1 of our previous downloaded dependency are the same
     * if not, the file is corrupted and we need to delete it
     *
     * @param dependency    the object to check
     * @param dependecyFile the file where is located the dependency
     * @return true if the two sha1 are the same
     */
    public boolean checksumDependency(Dependency dependency, File dependecyFile) {
        try (InputStream stream = new URL(dependency.getURLName() + ".sha1").openStream()) {
            final String urlSHa1 = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            final String dependencySha1 = calcSHA1(dependecyFile).toLowerCase();

            if (!urlSHa1.equals(dependencySha1)) {
                dependecyFile.delete();
                return false;
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            dependecyFile.delete();
            return false;
        }
        return true;
    }


    /**
     * Own sha1 calculator
     *
     * @param file we need to calculate
     * @return the Sha1 of the files
     * @throws IOException              if the file does not exist
     * @throws NoSuchAlgorithmException if the diggest does not contain this one
     */
    private static String calcSHA1(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        try (InputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }
            return new HexBinaryAdapter().marshal(sha1.digest());
        }
    }
}
