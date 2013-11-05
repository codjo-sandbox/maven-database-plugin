package net.codjo.maven.mojo.database.util;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
import net.codjo.maven.common.artifact.ArtifactGetter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.tools.ant.BuildException;
/**
 *
 */
public class IncludeUtil {
    private IncludeUtil() {
    }


    /**
     * Renvoie une Map<String artifactId, File artifactFile> des artifacts inclus
     */
    public static Map getIncludeFiles(ArtifactFactory artifactFactory,
                                      ArtifactRepository localRepository,
                                      MavenProject project,
                                      WagonManager wagonManager,
                                      ArtifactDescriptor[] artifactDescriptors,
                                      RepositoryMetadataManager repositoryMetadataManager)
          throws BuildException,
                 ResourceDoesNotExistException,
                 TransferFailedException, ArtifactNotFoundException, ArtifactResolutionException {

        if (artifactDescriptors == null || artifactDescriptors.length == 0) {
            return null;
        }

        Map files = new LinkedHashMap(artifactDescriptors.length);

        ArtifactGetter artifactGetter = new ArtifactGetter(artifactFactory,
                                                           localRepository,
                                                           project.getRemoteArtifactRepositories(),
                                                           wagonManager, repositoryMetadataManager);

        for (int i = 0; i < artifactDescriptors.length; i++) {
            ArtifactDescriptor artifactDescriptor = artifactDescriptors[i];
            artifactDescriptor.resolveType("zip");
            artifactDescriptor.resolveIncludeVersion(project.getDependencyManagement());
            Artifact artifact = artifactGetter.getArtifact(artifactDescriptor);
            files.put(artifact.getArtifactId(), artifact.getFile());
        }
        return files;
    }
}
