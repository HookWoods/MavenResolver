package fr.hookwood.mavenresolver;

public class Dependency {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String repository;

    /**
     * Used if you want to download a dependency from a private repository
     *
     * @param groupId    the group id of the dependency
     * @param artifactId the artifact id of the dependency
     * @param version    the version of the dependency
     * @param repository the custom repository of the dependency
     */
    public Dependency(String groupId, String artifactId, String version, String repository) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        String fullString = repository.endsWith("/") ? repository : repository + "/";
        this.repository = repository.isEmpty() ? "https://repo1.maven.org/maven2/" : fullString;
    }

    /***
     * Used if we only use public maven dependencies
     *
     * @param groupId the group id of the dependency
     * @param artifactId the artifact id of the dependency
     * @param version the version of the dependency
     */
    public Dependency(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, "https://repo1.maven.org/maven2/");
    }

    /**
     * @return the full URL of the dependency
     */
    public String getURLName() {
        return getRepository() + getGroupId().replace(".", "/") + "/" + getArtifactId() + "/" + getVersion() + "/" +
                getArtifactId() + "-" + getVersion() + ".jar";
    }

    /**
     * @return the group of the dependency
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * @return the name of the dependency
     */
    public String getArtifactId() {
        return this.artifactId;
    }

    /**
     * @return the version of the dependency
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * @return the repository of the dependency
     */
    public String getRepository() {
        return this.repository;
    }
}