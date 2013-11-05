package net.codjo.maven.mojo.database.util;
import java.io.File;
import java.util.List;
import junit.framework.TestCase;
import net.codjo.test.common.fixture.DirectoryFixture;
import net.codjo.util.file.FileUtil;
/**
 *
 */
public class ScriptUtilTest extends TestCase {
    private DirectoryFixture directoryFixture;


    protected void setUp() throws Exception {
        directoryFixture = DirectoryFixture.newTemporaryDirectoryFixture();
        directoryFixture.doSetUp();
    }


    protected void tearDown() throws Exception {
        directoryFixture.doTearDown();
    }


    public void test_computeCommonDir() throws Exception {
        File firstDir = new File(directoryFixture, "target");
        firstDir.mkdir();
        File secondDir = new File(firstDir, "test");
        assertEquals(directoryFixture.getPath(),
                     ScriptUtil.computeCommonDir(directoryFixture.getPath(), secondDir.getPath()));
    }


/*    public void test_getScripts_withMacOsEndOfLine() throws Exception {
        File firstDir = new File(directoryFixture, "target");
        firstDir.mkdir();
        File scriptsFile = new File(firstDir, "test");

        FileUtil.saveContent(scriptsFile, "script1.sql\rscript2.sql");
        List actual = ScriptUtil.getScripts(scriptsFile.getCanonicalPath());
        assertEquals(2, actual.size());
    }

    public void test_getScripts_withUnixSeparator() throws Exception {
        File firstDir = new File(directoryFixture, "target");
        firstDir.mkdir();
        File scriptsFile = new File(firstDir, "test");

        FileUtil.saveContent(scriptsFile, "script1.sql\nscript2.sql\nscript.sql");

        List actual = ScriptUtil.getScripts(scriptsFile.getCanonicalPath());
        assertEquals(3, actual.size());
    }

    public void test_getScripts_withWindowsSeparator() throws Exception {
        File firstDir = new File(directoryFixture, "target");
        firstDir.mkdir();
        File scriptsFile = new File(firstDir, "test");

        FileUtil.saveContent(scriptsFile, "script1.sql\r\nscript2.sql\r\nscript.sql");

        List actual = ScriptUtil.getScripts(scriptsFile.getCanonicalPath());
        assertEquals(3, actual.size());
    }*/


}
