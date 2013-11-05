package net.codjo.maven.mojo.database;
import junit.framework.TestCase;
import java.io.IOException;
import java.io.FileNotFoundException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
/**
 *
 */
public class SqlInProductionTest extends TestCase {
    public void test_getVersionFromSources() throws Exception {
        assertGetVersionFromSources("changes.xml", "2.04.00.00-a");
        assertGetVersionFromSources("unordered_changes.xml", "3.00.00.00-a");
    }


    public void test_getVersionFromSources_FileNotFoundException()
            throws IOException, ParserConfigurationException, SAXException {
        SqlInProduction sqlInProduction = new SqlInProduction();
        try {
            sqlInProduction.getVersionFromSources("fileNotFound.jak");
            fail("FileNotFoundException attempted");
        }
        catch (FileNotFoundException e) {
            ;
        }
    }


    private void assertGetVersionFromSources(String testFileName, String expectedVersion)
            throws IOException, ParserConfigurationException, SAXException {
        SqlInProduction sqlInProduction = new SqlInProduction();
        assertEquals(expectedVersion,
            sqlInProduction.getVersionFromSources(getFilePath(testFileName)));
    }


    public void test_getVersionFromDeployedApplication()
          throws IOException, ParserConfigurationException, SAXException, SqlInProduction.JnlpBadFormatException {
        SqlInProduction sqlInProduction = new SqlInProduction();
        assertEquals("3.02.00.00-a",
                     sqlInProduction.getVersionFromDeployedApplication(getFilePath("PIMS.jnlp")));
        assertEquals("1.00.00.00-c",
                     sqlInProduction.getVersionFromDeployedApplication(getFilePath(
                             "GABI_notJ2EE.jnlp")));
    }


    private String getFilePath(String jnlpFileName) {
        return getClass().getResource(jnlpFileName).getPath();
    }


    public void test_getVersionFromDeployedApplication_failed() {
        assertExceptionThrown("fileNotFound.jnlp", FileNotFoundException.class);
        assertExceptionThrown(getFilePath("PIMS_withoutMain.jnlp"),
            SqlInProduction.JnlpBadFormatException.class);
        assertExceptionThrown(getFilePath("PIMS_withManyMain.jnlp"),
            SqlInProduction.JnlpBadFormatException.class);
    }


    private void assertExceptionThrown(String jnlpFilePath, Class attemptedExceptionClass) {
        SqlInProduction sqlInProduction = new SqlInProduction();
        try {
            sqlInProduction.getVersionFromDeployedApplication(jnlpFilePath);
        }
        catch (Throwable exception) {
            assertEquals(attemptedExceptionClass, exception.getClass());
            return;
        }
        fail(attemptedExceptionClass.getName() + " attempted");
    }
}
