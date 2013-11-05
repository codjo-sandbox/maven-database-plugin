/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database;
import org.apache.maven.plugin.MojoExecutionException;
/**
 * Supprime tous les objets de la Base de Données.
 *
 * @goal drop
 */
public class DropMojo extends AbstractDatabaseMojo {

    protected void doExecute() throws MojoExecutionException {
        try {
            getDatabaseHelper().dropAllObjects(getConnection());
        }
        catch (Exception exception) {
            throw new MojoExecutionException("Exception during drop execution", exception);
        }
    }


    protected String getGoalName() {
        return "drop";
    }


    protected void printStartMessage() {
        super.printStartMessage();
        getLog().info("drop en execution dans " + getConnectionMetadata().getCatalog());
    }
}
