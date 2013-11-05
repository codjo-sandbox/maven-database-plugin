package net.codjo.maven.mojo.database;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
/**
 *
 */
public class TestUtil {
    private TestUtil() {
    }


    public static Dependency addDependencyToProject(String groupId,
                                                    String artifactId,
                                                    String version,
                                                    MavenProjectMock project) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        project.addPluginDependency(dependency);
        return dependency;
    }


    public static void addDependencyManagement(String groupId,
                                               String artifactId,
                                               String classifier, String version,
                                               MavenProjectMock project) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setClassifier(classifier);
        dependency.setVersion(version);
        if (project.getDependencyManagement() == null) {
            project.getModel().setDependencyManagement(new DependencyManagement());
        }
        project.getDependencyManagement().addDependency(dependency);
    }
}
