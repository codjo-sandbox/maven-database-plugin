/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import net.codjo.database.common.api.ConnectionMetadata;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.database.common.api.DatabaseHelper;
import net.codjo.database.common.api.ExecSqlScript;
import net.codjo.database.common.api.ExecSqlScript.Logger;
import net.codjo.maven.common.embedder.MavenCommand;
import net.codjo.util.file.FileUtil;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
public abstract class AbstractDatabaseMojo extends AbstractMojo implements DatabaseConstants {
    private static final String DATABASE_DROP_GOAL = "net.codjo.maven.mojo:maven-database-plugin:drop";
    private final DatabaseFactory databaseFactory = new DatabaseFactory();
    private final DatabaseHelper databaseHelper = databaseFactory.createDatabaseHelper();
    private ExecSqlScript execSqlScript = databaseFactory.createExecSqlScript();

    /**
     * @parameter expression="${databaseServer}"
     * @required
     * @noinspection UnusedDeclaration
     */
    private String databaseServer;
    /**
     * @parameter expression="${databasePort}"
     * @required
     * @noinspection UnusedDeclaration
     */
    private String databasePort;
    /**
     * @parameter expression="${databaseCatalog}"
     * @required
     * @noinspection UnusedDeclaration
     */
    private String databaseCatalog;
    /**
     * @parameter expression="${databaseBase}"
     * @required
     * @noinspection UnusedDeclaration
     */
    private String databaseBase;
    /**
     * @parameter expression="${databaseUser}"
     * @required
     * @noinspection UnusedDeclaration
     */
    private String databaseUser;
    /**
     * @parameter expression="${databasePassword}"
     * @required
     * @noinspection UnusedDeclaration
     */
    private String databasePassword;

    /**
     * @parameter expression="${releaseScriptsFile}" default-value="${project.build.directory}/sql/livraison-sql.txt"
     * @noinspection UnusedDeclaration
     */
    protected File releaseScriptsFile;

    /**
     * @parameter expression="${project}"
     * @required
     * @noinspection UNUSED_SYMBOL
     */
    protected MavenProject project;

    private ConnectionMetadata connectionMetadata;
    private Connection connection;

    /**
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @parameter expression="${sqlBaseDirGenerated}" default-value="${project.build.directory}/sql"
     */
    protected String sqlBaseDirGenerated;

    private MavenCommand dropDatabaseMavenCommand = new MavenCommand();


    public void setDropDatabaseMavenCommand(MavenCommand command) {
        dropDatabaseMavenCommand = command;
    }


    public DatabaseFactory getDatabaseFactory() {
        return databaseFactory;
    }


    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }


    public ExecSqlScript getExecSqlScript() {
        execSqlScript.setConnectionMetadata(getConnectionMetadata());
        execSqlScript.setLogger(new Logger() {
            public void log(String log) {
                getLog().debug(log);
            }
        });
        return execSqlScript;
    }


    public void execute() throws MojoExecutionException, MojoFailureException {
        printStartMessage();
        doExecute();
        printTerminationMessage();
    }


    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;


    protected abstract String getGoalName();


    protected ConnectionMetadata getConnectionMetadata() {
        if (connectionMetadata == null) {
            connectionMetadata = new ConnectionMetadata();
            connectionMetadata.setHostname(databaseServer);
            connectionMetadata.setPort(databasePort);
            connectionMetadata.setUser(databaseUser);
            connectionMetadata.setPassword(databasePassword);
            connectionMetadata.setBase(databaseBase);
            connectionMetadata.setCatalog(databaseCatalog);
        }
        return connectionMetadata;
    }


    protected Connection getConnection() {
        if (connection == null) {
            try {
                connection = databaseHelper.createConnection(getConnectionMetadata());
                getLog().info("Utilisation de la DB : " + connection.getMetaData().getURL());
            }
            catch (SQLException e) {
                throw new RuntimeException(
                      "Problème lors de la connexion à la base " + databaseServer + ":" + databasePort);
            }
        }
        return connection;
    }


    protected void executeDropDatabaseObjects(File targetDirectory) throws MojoExecutionException {
        try {
            dropDatabaseMavenCommand
                  .execute(new String[]{DATABASE_DROP_GOAL}, targetDirectory, getLog(), false);
        }
        catch (Exception exception) {
            getLog().error("drop goal en echec.");
            throw new MojoExecutionException(exception.getLocalizedMessage(), exception);
        }
    }


    protected void printStartMessage() {
        getLog().info("database:" + getGoalName() + " en execution...");
    }


    protected void printTerminationMessage() {
        getLog().info("database:" + getGoalName() + " execution terminee");
    }


    void setExecSqlScript(ExecSqlScript execSqlScript) {
        this.execSqlScript = execSqlScript;
    }


    protected void runReleaseScriptsTwice() {
        runReleaseScripts();
        runReleaseScripts();
    }


    private void runReleaseScripts() {
        getLog().info("\tExecution du livraison-sql.txt...");
        if (sqlBaseDirGenerated != null && releaseScriptsFile != null) {
            String sqlDirectoryPath = new File(sqlBaseDirGenerated).getAbsolutePath();
            getExecSqlScript().execute(sqlDirectoryPath, FileUtil.loadContentAsLines(releaseScriptsFile));
        }
    }
}
