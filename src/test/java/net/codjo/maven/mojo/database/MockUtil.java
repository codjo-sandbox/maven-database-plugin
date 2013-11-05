package net.codjo.maven.mojo.database;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
/**
 * @noinspection Singleton
 */
public class MockUtil {
    /**
     * @noinspection StaticNonFinalField,StaticVariableMayNotBeInitialized
     */
    static MockUtil singleton;
    private File pomFile;
    private File targetDir;
    private MavenProjectMock project;
    private ArtifactRepositoryMock artifactRepository;


    private MockUtil(String pomFilePath) throws IOException {
        pomFile = new File(pomFilePath);

        targetDir = new File(PlexusTestCase.getBasedir(), "target/test-harness/MockUtil");
        if (targetDir.exists()) {
            FileUtils.deleteDirectory(targetDir);
        }
        targetDir.mkdirs();
        singleton = this;
    }

    public static MockUtil setupEnvironment(String pomFilePath)
            throws IOException {
        return new MockUtil(getInputFile(pomFilePath));
    }


    public static String getInputFile(String pomFilePath) {
        return "src/test/resources-filtered/mojos/" + pomFilePath;
    }


    public File getPomFile() {
        return pomFile;
    }


    public File getTargetDir() {
        return targetDir;
    }


    public File getTargetFile(String fileName) {
        return new File(getTargetDir(), fileName);
    }


    public MavenProjectMock getProject() {
        return project;
    }


    public void setProject(MavenProjectMock project) {
        this.project = project;
    }


    public ArtifactRepositoryMock getArtifactRepository() {
        return artifactRepository;
    }


    public void setArtifactRepository(ArtifactRepositoryMock repository) {
        this.artifactRepository = repository;
    }


    public static String toUrl(String pathname) {
        try {
            File remoteDir = new File(pathname);
            return remoteDir.toURL().toExternalForm();
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException("BadUrl");
        }
    }
}
