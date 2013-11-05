package net.codjo.maven.mojo.database;
import java.io.File;
import net.codjo.database.common.api.ConnectionMetadata;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
/**
 * @goal assert-dataserver
 * @aggregator
 */
public class AssertDataserverMojo extends AbstractDatabaseMojo {

    /**
     * @parameter expression="${expectedDatabaseServer}"
     * @required
     */
    private String expectedDatabaseServer;
    /**
     * @parameter expression="${expectedDatabasePort}"
     * @required
     */
    private String expectedDatabasePort;
    /**
     * @parameter expression="${expectedDatabaseCatalog}"
     * @required
     */
    private String expectedDatabaseCatalog;
    /**
     * @parameter expression="${expectedDatabaseBase}"
     * @required
     */
    private String expectedDatabaseBase;
    /**
     * @parameter expression="${projectFile}" default-value="${project.file}"
     * @noinspection UnusedDeclaration
     */
    private File projectFile;


    public void setExpectedDatabaseServer(String expectedDatabaseServer) {
        this.expectedDatabaseServer = expectedDatabaseServer;
    }


    public void setExpectedDatabasePort(String expectedDatabasePort) {
        this.expectedDatabasePort = expectedDatabasePort;
    }


    public void setExpectedDatabaseCatalog(String expectedDatabaseCatalog) {
        this.expectedDatabaseCatalog = expectedDatabaseCatalog;
    }


    public void setExpectedDatabaseBase(String expectedDatabaseBase) {
        this.expectedDatabaseBase = expectedDatabaseBase;
    }


    protected String getGoalName() {
        return "assert-dataserver";
    }


    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        ConnectionMetadata actualDatabase = getConnectionMetadata();
        StringBuilder actual = new StringBuilder();
        actual.append(actualDatabase.getHostname());
        actual.append(actualDatabase.getBase());
        actual.append(actualDatabase.getCatalog());
        actual.append(actualDatabase.getPort());

        StringBuilder expected = new StringBuilder();
        expected.append(expectedDatabaseServer);
        expected.append(expectedDatabaseBase);
        expected.append(expectedDatabaseCatalog);
        expected.append(expectedDatabasePort);

        if (!expected.toString().equals(actual.toString())) {
            String errorMessage = new StringBuilder()
                  .append("Le paramétrage de la base n'est pas correct !!!\n")
                  .append("\n")
                  .append("EXPECTED :\n")
                  .append("databaseServer = ").append(expectedDatabaseServer).append("\n")
                  .append("databasePort = ").append(expectedDatabasePort).append("\n")
                  .append("databaseBase = ").append(expectedDatabaseBase).append("\n")
                  .append("databaseCatalog = ").append(expectedDatabaseCatalog).append("\n")
                  .append("ACTUAL (").append(projectFile.getPath()).append(") :\n")
                  .append("databaseServer = ").append(actualDatabase.getHostname()).append("\n")
                  .append("databasePort = ").append(actualDatabase.getPort()).append("\n")
                  .append("databaseBase = ").append(actualDatabase.getBase()).append("\n")
                  .append("databaseCatalog = ").append(actualDatabase.getCatalog()).append("\n").toString();
            throw new MojoExecutionException(errorMessage);
        }
    }
}
