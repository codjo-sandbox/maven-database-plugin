package net.codjo.maven.mojo.database;
import org.apache.maven.plugin.MojoExecutionException;
/**
 *
 */
public class AssertDataserverMojoTest extends DatabaseMojoTestCase {

    public void test_assertOk() throws Exception {
        execute(initMojo("assertDataserver/pom-default.xml"), "assertDataserver");
    }


    public void test_assertKo() throws Exception {
        try {
            execute(initMojo("assertDataserver/pom-assertKO.xml"), "assertDataserver");
            fail();
        }
        catch (MojoExecutionException e) {
            assertEquals(new StringBuffer()
                  .append("Le paramétrage de la base n'est pas correct !!!\n")
                  .append("\n")
                  .append("EXPECTED :\n")
                  .append("databaseServer = anotherDatabaseServer\n")
                  .append("databasePort = databasePort\n")
                  .append("databaseBase = databaseBase\n")
                  .append("databaseCatalog = databaseCatalog\n")
                  .append("ACTUAL (pom.xml) :\n")
                  .append("databaseServer = databaseServer\n")
                  .append("databasePort = databasePort\n")
                  .append("databaseBase = databaseBase\n")
                  .append("databaseCatalog = databaseCatalog\n").toString()
                  , e.getMessage());
        }
    }


    private AssertDataserverMojo initMojo(String pomFilePath) throws Exception {
        MockUtil.setupEnvironment(pomFilePath);
        return (AssertDataserverMojo)lookupMojo("assert-dataserver", pomFilePath);
    }
}
