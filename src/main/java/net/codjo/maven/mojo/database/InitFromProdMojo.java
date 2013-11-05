package net.codjo.maven.mojo.database;
import java.io.File;
import java.util.Properties;
import net.codjo.maven.common.embedder.MavenCommand;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
/**
 * Goal pour initialiser la base de données
 *
 * @goal init-from-prod
 * @phase process-resources
 * @noinspection ALL
 */
public class InitFromProdMojo extends AbstractInitMojo {
    public static final String CHECKOUT_PROD_GOAL
          = "net.codjo.maven.mojo:maven-database-plugin:checkout-from-prod";
    private static final String ASSERT_DATASERVER_GOAL
          = "net.codjo.maven.mojo:maven-database-plugin:assert-dataserver";
    private static final String INSTALL_GOAL = "install";

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
     * @parameter expression="${maven.database.assertDataserver}" default-value="${assertDataserver}"
     * @noinspection UnusedDeclaration
     */
    protected boolean assertDataserver = false;

    /**
     * @parameter expression="${maven.database.fullInitFromProd}" default-value="${fullInitFromProd}"
     * @noinspection UnusedDeclaration
     */
    private boolean fullInitFromProd = false;

    /**
     * The directory to checkout the sources to for the bootstrap and checkout goals
     *
     * @parameter expression="${checkoutDirectory}" default-value="${project.build.directory}/checkout"
     */
    private File checkoutDirectory;

    /**
     * Excecutes the release scripts or not (if true then the scripts are executed twice)
     *
     * @parameter expression="${runReleaseScripts}" default-value="false"
     */
    public boolean runReleaseScripts;

    /**
     * @parameter expression="${databaseType}"
     * @noinspection UnusedDeclaration
     */
    private String databaseType;

    private MavenCommand checkoutProjectMavenCommand = new MavenCommand();
    private MavenCommand assertDataserverCommand = new MavenCommand();
    private MavenCommand installCommand = new MavenCommand();
    private String process;


    public void setInstallCommand(MavenCommand mavenCommand) {
        this.installCommand = mavenCommand;
    }


    public void setAssertDataserverCommand(MavenCommand assertDataserverCommand) {
        this.assertDataserverCommand = assertDataserverCommand;
    }


    public void setCheckoutProjectMavenCommand(MavenCommand checkoutProjectMavenCommand) {
        this.checkoutProjectMavenCommand = checkoutProjectMavenCommand;
    }


    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }


    public void doExecute() throws MojoExecutionException, MojoFailureException {
        try {
            executeImpl();
        }
        catch (MojoExecutionException exception) {
            getLog().info(exception);
            throw exception;
        }
        catch (MojoFailureException exception) {
            getLog().info(exception);
            throw exception;
        }
        catch (Exception exception) {
            getLog().info(exception);
            throw new MojoExecutionException(exception.getLocalizedMessage());
        }
    }


    protected String getGoalName() {
        return "init-from-prod";
    }


    protected boolean shouldExecuteSQL() throws MojoExecutionException {
        return true;
    }


    private void executeImpl() throws Exception {
        if (versionInProduction == null || "".equals(versionInProduction.trim())) {
            printWarningMessage();
            return;
        }

        configureCheckoutCommand();
        checkoutProjectMavenCommand
              .execute(new String[]{CHECKOUT_PROD_GOAL}, projectDirectory, getLog(), false);

        if (assertDataserver) {
            configureAssertDataserverCommand();
            assertDataserverCommand
                  .execute(new String[]{ASSERT_DATASERVER_GOAL}, checkoutDirectory, getLog(), false);
        }

        backupProcessType();
        try {
            configureInstallCommand();
            installCommand.execute(new String[]{INSTALL_GOAL}, checkoutDirectory, getLog(), true);
        }
        finally {
            restoreProcessType();
        }
        if (runReleaseScripts) {
            prepareSqlDirectory();
            runReleaseScriptsTwice();
        }
    }


    private static Properties getProperties(MavenCommand mavenCommand) {
        Properties properties = mavenCommand.getProperties();
        if (properties == null) {
            properties = new Properties();
            mavenCommand.setProperties(properties);
        }
        return properties;
    }


    private void configureCheckoutCommand() {
        Properties properties = getProperties(checkoutProjectMavenCommand);
        properties.setProperty("versionInProduction", versionInProduction);
    }


    private void configureAssertDataserverCommand() {
        Properties properties = getProperties(assertDataserverCommand);
        properties.setProperty("expectedDatabaseServer", getConnectionMetadata().getHostname());
        properties.setProperty("expectedDatabasePort", getConnectionMetadata().getPort());
        properties.setProperty("expectedDatabaseBase", getConnectionMetadata().getBase());
        properties.setProperty("expectedDatabaseCatalog", getConnectionMetadata().getCatalog());
        if (databaseType != null) {
            properties.setProperty("databaseType", databaseType);
        }
    }


    private void configureInstallCommand() {
        Properties properties = getProperties(installCommand);
        properties.setProperty("maven.test.skip.exec", "true");
        if (!fullInitFromProd) {
            properties.setProperty("maven.datagen.skip.compile", "true");
            properties.setProperty("maven.reactor.excludes",
                                   "*-batch/pom.xml,"
                                   + "*-gui/pom.xml,"
                                   + "*-client/pom.xml,"
                                   + "*-server/pom.xml,"
                                   + "*-web/pom.xml,"
                                   + "*-release-test/pom.xml");
        }
        properties.setProperty("maven.reactor.includes", "*/pom.xml");
    }


    private void backupProcessType() {
        process = (String)System.getProperties().remove("process");
    }


    private void restoreProcessType() {
        if (process != null) {
            System.getProperties().setProperty("process", process);
        }
    }


    private void printWarningMessage() {
        getLog().warn("");
        getLog().warn(
              "|---------------------------------------------------------------------------------------------------|");
        getLog().warn(
              "| ATTENTION : L'initialisation de la base de donnees de production ne sera pas effectuee            |");
        getLog().warn(
              "|             car la propriete 'versionInProduction' n'est pas definie dans le pom.xml.             |");
        getLog().warn(
              "|---------------------------------------------------------------------------------------------------|");
        getLog().warn("");
    }
}
