package net.codjo.maven.mojo.database;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.codjo.database.common.api.DatabaseComparator;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.maven.common.embedder.MavenCommand;
import net.codjo.maven.mojo.database.util.ScriptUtil;
import net.codjo.maven.mojo.database.util.SqlDirectory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Settings;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
/**
 * Récupère la version de production spécifiée.
 *
 * @goal compare-from-prod
 * @phase pre-integration-test
 * @noinspection UNUSED_SYMBOL
 */
public class CompareFromProdMojo extends AbstractDatabaseMojo {
    private static final String INIT_FROM_PROD_GOAL
          = "net.codjo.maven.mojo:maven-database-plugin:init-from-prod";

    /**
     * @parameter expression="${sqlBaseDir}" default-value="${project.build.directory}/sql-filtered"
     * @noinspection UnusedDeclaration
     */
    private String sqlBaseDir;

    /**
     * @parameter expression="${sqlBaseDirTest}" default-value="${project.basedir}/src/test/sql"
     * @noinspection UnusedDeclaration
     */
    private String sqlBaseDirTest;

    /**
     * @parameter expression="${project.basedir}"
     * @readonly
     */
    protected File projectDirectory;

    /**
     * The production tag to be compared with the development version
     *
     * @parameter expression="${maven.database.versionInProduction}" default-value="${versionInProduction}"
     * @noinspection UnusedDeclaration
     */
    protected String versionInProduction;

    /**
     * @parameter expression="${excludeFromComparison}"
     */
    protected ExcludeFromComparison excludeFromComparison = new ExcludeFromComparison();

    /**
     * @parameter expression="${settings}"
     * @noinspection UnusedDeclaration
     * @required
     * @readonly
     */
    private Settings settings;

    private MavenCommand initFromProdCommand = new MavenCommand();


    public void setInitFromProdCommand(MavenCommand initFromProdCommand) {
        this.initFromProdCommand = initFromProdCommand;
    }


    public void doExecute() throws MojoExecutionException, MojoFailureException {
        try {
            executeImpl();
        }
        catch (MojoExecutionException exception) {
            throw exception;
        }
        catch (MojoFailureException exception) {
            throw exception;
        }
        catch (Exception exception) {
            throw new MojoExecutionException(exception.getLocalizedMessage(), exception);
        }
    }


    protected String getGoalName() {
        return "compare-from-prod";
    }


    private void executeImpl() throws Exception {
        if (versionInProduction == null || "".equals(versionInProduction.trim())) {
            printWarningMessage();
            return;
        }

        DatabaseComparator comparator;
        try {
            comparator = new DatabaseFactory().createDatabaseComparator();
            comparator.setIgnoredTableNames(excludeFromComparison.getTables());
            comparator.setIgnoredProcedureNames(excludeFromComparison.getProcedures());
        }
        catch (RuntimeException ex) {
            if ("Method not yet implemented".equals(ex.getLocalizedMessage())) {
                throw new MojoFailureException(
                      "La comparaison de base n'est pas encore implementee pour ce type de base");
            }
            else {
                throw ex;
            }
        }

        comparator.loadActualStructure(getConnectionMetadata());

        getLog().info("");
        getLog().info("\tInstallation de la base de donnees de la version : " + versionInProduction);
        getLog().info("");
        initDatabaseInProduction();

        if (sqlBaseDirTest != null && new File(sqlBaseDirTest).exists()) {
            getLog().info("");
            getLog().info("\tInstallation des scripts de mock...");
            initSqlBaseDirTest();
        }

        copyDirectory(sqlBaseDir, sqlBaseDirGenerated);

        runReleaseScriptsTwice();

        comparator.loadExpectedStructure(getConnectionMetadata());
        if (comparator.areDatabasesDifferent()) {
            throw new MojoExecutionException(
                  "La base de donnees de production est differente de celle de developpement.");
        }

        getLog().info("");
        getLog().info("Comparaison de base de donnees OK");
        getLog().info("");
    }


    private void initDatabaseInProduction() throws MojoExecutionException {
        try {
            File file = new File(projectDirectory, "target\\DBInit.timestamp");
            boolean done = file.delete();
            getLog().info("fichier " + projectDirectory + " target\\DBInit.timestamp - EFFACE : " + done);

            configureInitFromProdCommand();
            initFromProdCommand.execute(new String[]{INIT_FROM_PROD_GOAL}, projectDirectory, getLog(), false);
        }
        catch (Exception exception) {
            getLog().error("goal '" + INIT_FROM_PROD_GOAL + "' en echec.");
            throw new MojoExecutionException(exception.getLocalizedMessage(), exception);
        }
    }


    private void configureInitFromProdCommand() {
        Properties properties = initFromProdCommand.getProperties();
        if (properties == null) {
            properties = new Properties();
            initFromProdCommand.setProperties(properties);
        }
        properties.setProperty("versionInProduction", versionInProduction);
        properties.setProperty("assertDataserver", "true");
    }


    private void initSqlBaseDirTest() {
        List fileSets = new ArrayList();
        File destination = new File(sqlBaseDirTest);
        SqlDirectory fileSet = new SqlDirectory(destination);
        File procedureOrderFile = new File(sqlBaseDirTest, PROCEDURE_ORDER_FILE);
        if (procedureOrderFile.exists()) {
            fileSet.getCommandFiles().add(procedureOrderFile);
        }
        fileSets.add(fileSet);

        ScriptUtil.executeSqlCommands(fileSets,
                                      new SqlDirectoryFilter(),
                                      new File(sqlBaseDirTest).getParentFile().getAbsolutePath(),
                                      getExecSqlScript());
    }


    private void copyDirectory(String from, String to) {
        Project copyProject = new Project();
        Copy copyTask = new Copy();
        FileSet fileSet = new FileSet();
        fileSet.setDir(new File(from));
        copyTask.addFileset(fileSet);
        copyTask.setTodir(new File(to));
        copyTask.setOverwrite(true);
        copyTask.setProject(copyProject);
        copyProject.init();
        copyTask.execute();
    }


    public String getVersionInProduction() {
        return versionInProduction;
    }


    private void printWarningMessage() {
        getLog().warn("");
        getLog()
              .warn("|---------------------------------------------------------------------------------------------------|");
        getLog()
              .warn("| ATTENTION : La comparaison de base de donnees avec la version en production ne sera pas effectuee |");
        getLog()
              .warn("|             car la propriete 'versionInProduction' n'est pas definie dans le pom.xml.             |");
        getLog()
              .warn("|---------------------------------------------------------------------------------------------------|");
        getLog().warn("");
    }
}
