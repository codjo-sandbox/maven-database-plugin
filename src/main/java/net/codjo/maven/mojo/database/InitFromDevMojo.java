/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import net.codjo.maven.mojo.database.util.IncludeUtil;
import net.codjo.maven.mojo.database.util.ScriptUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
/**
 * Goal pour initialiser la base de données
 *
 * @goal init
 * @phase process-resources
 * @noinspection ALL
 */
public class InitFromDevMojo extends AbstractInitMojo {
    private static final String TIMESTAMP_FILENAME = "DBInit.timestamp";

    /**
     * @parameter expression="${applicationDatabaseGroup}" default-value="Utilisateur"
     */
    private String databaseApplicationGroup;

    /**
     * @parameter expression="${databaseApplicationUser}"
     */
    private String databaseApplicationUser;

    private long lastModified;


    public String getDatabaseApplicationGroup() {
        return databaseApplicationGroup;
    }


    public void doExecute() throws
                            MojoExecutionException,
                            MojoFailureException {
        if (sqlBaseDirGenerated == null) {
            sqlBaseDirGenerated = project.getBuild().getDirectory();
        }
        if (shouldExecuteSQL()) {
            sqlFilter.setEngine(databaseType);
            executeDropDatabaseObjects(projectDirectory);

            getLog().info("Initialisation de la base en cours.");

            setApplicationUserToGroup();

            initWorkDir();

            try {
                executeSqlCommands(prepareSql());
            }
            catch (RuntimeException exception) {
                getLog().error("Initialisation de la base en echec.");
                throw new MojoExecutionException(exception.getMessage(), exception);
            }
        }
        else {
            getLog().info("---------------------------------------------");
            getLog().info("|                                           |");
            getLog().info("| Initialisation de la base non necessaire. |");
            getLog().info("|                                           |");
            getLog().info("---------------------------------------------");
        }
    }


    protected String getGoalName() {
        return "init";
    }


    protected boolean shouldExecuteSQL() throws MojoExecutionException {
        if (includes != null) {
            try {
                Map files = IncludeUtil.getIncludeFiles(artifactFactory, localRepository,
                                                        project, wagonManager,
                                                        includes, repositoryMetadataManager);
                initialiseLastModified((File[])files.values().toArray(new File[]{}));
            }
            catch (Exception exception) {
                getLog().error("Unzip des fichiers inclus.");
                throw new MojoExecutionException(exception.getMessage(), exception);
            }
        }

        initialiseLastModified(procedureOrder);

        initialiseLastModified(DIR_MESSAGE);
        initialiseLastModified(DIR_TABLE);
        initialiseLastModified(DIR_GAP);
        initialiseLastModified(DIR_INDEX);
        initialiseLastModified(DIR_CONSTRAINT);
        initialiseLastModified(DIR_RULE);
        initialiseLastModified(DIR_PROCEDURE);
        initialiseLastModified(DIR_TRIGGER);
        initialiseLastModified(DIR_VIEW);
        initialiseLastModified(DIR_PERMISSION);

        return lastModified > getLastInitDate();
    }


    protected void executeSqlCommands(List scripts) {
        super.executeSqlCommands(scripts);
        setLastInitDate(lastModified);
    }


    protected String getDatabaseApplicationUser() {
        return databaseApplicationUser;
    }


    protected void setApplicationUserToGroup() throws MojoExecutionException {
        if (databaseApplicationUser != null) {
            try {
                getDatabaseHelper().changeUserGroup(getConnection(),
                                                    databaseApplicationUser,
                                                    databaseApplicationGroup);
            }
            catch (Exception e) {
                throw new MojoExecutionException("Exception pendant l'affectation du user applicatif '"
                                                 + databaseApplicationUser + "' au groupe '"
                                                 + databaseApplicationGroup + "'", e);
            }
        }
    }


    private void initialiseLastModified(File[] files) {
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isFile()) {
                    initialiseLastModified(file);
                }
            }
        }
    }


    private void initialiseLastModified(File file) {
        lastModified = Math.max(lastModified, file.lastModified());
    }


    private void initialiseLastModified(String currentDirectory) {
        File sqlFile = new File(ScriptUtil.createRelativePath(sqlBaseDir, currentDirectory));
        initialiseLastModified(ScriptUtil.collectFiles(sqlFile, sqlFilter));
    }


    private long getLastInitDate() {
        File timestampFile = new File(project.getBuild().getDirectory(), TIMESTAMP_FILENAME);
        if (timestampFile.exists()) {
            try {
                String content = FileUtils.fileRead(timestampFile);
                return Long.valueOf(content).longValue();
            }
            catch (IOException e) {
                getLog()
                      .error("Impossible de lire le fichier de timestamp " + timestampFile.getAbsolutePath());
            }
        }
        return 0;
    }


    private void setLastInitDate(long lastModified) {
        String fileName = project.getBuild().getDirectory() + "\\" + TIMESTAMP_FILENAME;
        try {
            FileUtils.fileWrite(fileName, String.valueOf(System.currentTimeMillis()));
        }
        catch (IOException e) {
            getLog().error(
                  "Impossible d'écrire dans le fichier de timestamp " + fileName);
        }
    }
}
