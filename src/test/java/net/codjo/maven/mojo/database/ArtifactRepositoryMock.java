package net.codjo.maven.mojo.database;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
/**
 *
 */
public class ArtifactRepositoryMock extends DefaultArtifactRepository {
    public ArtifactRepositoryMock() {
        super("mock", MockUtil.toUrl("./target/test-classes/mojos"),
              new DefaultRepositoryLayout());
        MockUtil.singleton.setArtifactRepository(this);
    }
}
