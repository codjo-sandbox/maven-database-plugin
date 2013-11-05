package net.codjo.maven.mojo.database;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.codjo.maven.common.ant.AntUtil;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
import net.codjo.maven.mojo.database.util.Filter;
import net.codjo.maven.mojo.database.util.IncludeUtil;
import net.codjo.maven.mojo.database.util.ScriptUtil;
import net.codjo.maven.mojo.database.util.SqlDirectory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Replace;
import org.codehaus.plexus.util.FileUtils;
/**
 *
 */
public abstract class AbstractInitMojo extends AbstractDatabaseMojo {
    /**
     * @parameter expression="${databaseType}"
     * @noinspection UnusedDeclaration
     */
    protected String databaseType;

    /**
     * @parameter expression="${procedureOrder}" default-value="${project.basedir}/src/main/sql/procedure-order.txt"
     */
    protected File procedureOrder;

    /**
     * @parameter expression="${sqlBaseDir}" default-value="${project.basedir}/src/main/sql"
     */
    protected String sqlBaseDir;

    /**
     * @parameter expression="${project.basedir}"
     * @readonly
     */
    protected File projectDirectory;
    protected String workDir;
    protected SqlDirectoryFilter sqlFilter = new SqlDirectoryFilter();
    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;
    /**
     * Liste des artifacts inclus.
     *
     * @parameter
     * @noinspection UNUSED_SYMBOL
     */
    protected ArtifactDescriptor[] includes;
    /**
     * Liste des filtres.
     *
     * @parameter
     * @noinspection UNUSED_SYMBOL
     */
    protected Filter[] filters;
    /**
     * @parameter expression="${component.org.apache.maven.artifact.manager.WagonManager}"
     * @required
     * @readonly
     * @noinspection UNUSED_SYMBOL
     */
    protected WagonManager wagonManager;
    /**
     * @parameter expression="${component.org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager}"
     * @required
     * @readonly
     * @noinspection UnusedDeclaration
     */
    protected RepositoryMetadataManager repositoryMetadataManager;
    /**
     * @parameter expression="${sqlBaseDirTest}" default-value="${project.basedir}/src/test/sql"
     */
    protected String sqlBaseDirTest;
    /**
     * @parameter expression="${sqlBaseDirFiltered}" default-value="${project.build.directory}/sql-filtered"
     */
    protected String sqlBaseDirFiltered;


    protected AbstractInitMojo() {
    }


    protected abstract boolean shouldExecuteSQL() throws MojoExecutionException;


    protected void initWorkDir() {
        workDir = ScriptUtil.computeCommonDir(new File(sqlBaseDir).getAbsolutePath(),
                                              new File(sqlBaseDirGenerated).getAbsolutePath());
    }


    protected void printStartMessage() {
        super.printStartMessage();
        getLog().info(getGoalName() + " en execution dans " + getConnectionMetadata().getCatalog());
    }


    protected void executeSqlCommands(List sqlFileSets) throws BuildException {
        ScriptUtil.executeSqlCommands(sqlFileSets, sqlFilter, this.workDir, getExecSqlScript());
    }


    protected void prepareSqlDirectory() throws MojoExecutionException, IOException {
        if (sqlBaseDirGenerated == null) {
            sqlBaseDirGenerated = project.getBuild().getDirectory();
        }
        sqlFilter.setEngine(databaseType);

        initWorkDir();
        prepareSql();
        copyDirectory(new File(sqlBaseDirFiltered), new File(sqlBaseDirGenerated));
    }


    protected List prepareSql() throws MojoExecutionException {
        return getSqlFileSets();
    }


    private List getSqlFileSets() throws MojoExecutionException {
        List fileSets = new ArrayList();

        // Ajout des fichiers inclus
        File destination = new File(sqlBaseDirGenerated);
        List procedureOrderFiles = executeUnzipSqlFiles(destination);
        SqlDirectory fileSet = new SqlDirectory(destination);
        if (procedureOrderFiles != null) {
            fileSet.getCommandFiles().addAll(procedureOrderFiles);
        }
        fileSets.add(fileSet);

        // Ajout des fichiers test
        if (sqlBaseDirTest != null && new File(sqlBaseDirTest).exists()) {
            destination = new File(sqlBaseDirTest);
            fileSet = new SqlDirectory(destination);
            File procedureOrderFile = new File(sqlBaseDirTest, PROCEDURE_ORDER_FILE);
            if (procedureOrderFile.exists()) {
                fileSet.getCommandFiles().add(procedureOrderFile);
            }
            fileSets.add(fileSet);
        }

        // Ajout des fichiers principaux filtrés
        if (sqlBaseDirFiltered == null) {
            sqlBaseDirFiltered = project.getBuild().getDirectory() + "/sql-filtered";
        }
        destination = new File(sqlBaseDirFiltered);
        destination.mkdirs();
        fileSet = new SqlDirectory(destination);

        File sourceDir = new File(sqlBaseDir);
        File targetDir = new File(sqlBaseDirFiltered);
        try {
            copyDirectory(sourceDir, targetDir);
        }
        catch (IOException e) {
            throw new MojoExecutionException(
                  "Erreur lors de la copie des fichiers de " + sourceDir + " vers " + targetDir, e);
        }
        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                Replace replace = new Replace();
                replace.setToken(filters[i].getToken());
                replace.setValue(filters[i].getValue());
                replace.setDir(targetDir);
                replace.setProject(new Project());
                replace.execute();
            }
        }
        if (procedureOrder.exists()) {
            fileSet.getCommandFiles().add(procedureOrder);
        }
        fileSets.add(fileSet);

        return fileSets;
    }


    private List executeUnzipSqlFiles(File destination) throws MojoExecutionException {
        if (includes != null) {
            try {
                Map files = IncludeUtil.getIncludeFiles(artifactFactory, localRepository,
                                                        project, wagonManager,
                                                        includes, repositoryMetadataManager);
                List procedureList = new ArrayList();
                for (Iterator i = files.entrySet().iterator(); i.hasNext(); ) {
                    Map.Entry entry = (Map.Entry)i.next();
                    File file = (File)entry.getValue();
                    String subDir = (String)entry.getKey();
                    AntUtil.unzipFiles(new File[]{file}, destination);
                    File procedureOrderFile = new File(destination, PROCEDURE_ORDER_FILE);
                    File renamedProcedureOrder = new File(destination, subDir + "-" + PROCEDURE_ORDER_FILE);
                    procedureList.add(renamedProcedureOrder);
                    if (procedureOrderFile.exists()) {
                        procedureOrderFile.renameTo(renamedProcedureOrder);
                    }
                }
                return procedureList;
            }
            catch (Exception exception) {
                getLog().error("Unzip du fichier généré par datagen en echec.");
                throw new MojoExecutionException(exception.getMessage(), exception);
            }
        }
        return null;
    }


    private void copyDirectory(File sourceDir, File targetDir) throws IOException {
        if (sourceDir.isFile()) {
            FileUtils.copyFile(sourceDir, targetDir);
        }
        else {
            targetDir.mkdirs();
            String[] files = sourceDir.list();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    String file = files[i];
                    copyDirectory(new File(sourceDir, file), new File(targetDir, file));
                }
            }
        }
    }
}
