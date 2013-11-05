package net.codjo.maven.mojo.database;
import net.codjo.test.common.fixture.DirectoryFixture;
import junit.framework.TestCase;
import org.codehaus.plexus.util.FileUtils;
/**
 *
 */
public abstract class DefaultProductVersionTestCase extends TestCase {
    private DirectoryFixture fixture = DirectoryFixture.newTemporaryDirectoryFixture();


    public void test_getProductVersion() throws Exception {
        DefaultProductVersion productVersion = new DefaultProductVersionMock("gabi");

        assertEquals("gabi-1-08-00-00-c", productVersion.getProductVersion());
    }


    protected void setUp() throws Exception {
        fixture.doSetUp();
        FileUtils.fileWrite((fixture.getAbsolutePath() + "\\version.properties"), "tag = gabi-1-08-00-00-c");
    }


    private class DefaultProductVersionMock extends DefaultProductVersion {
        DefaultProductVersionMock(String applicationName) {
            super(applicationName);
        }


        public String getApplicationProductionDirectory() {
            return fixture.getAbsolutePath();
        }
    }
}
