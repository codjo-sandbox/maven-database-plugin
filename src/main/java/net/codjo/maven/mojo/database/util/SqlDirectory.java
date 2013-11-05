package net.codjo.maven.mojo.database.util;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 *
 */
public class SqlDirectory {
    private final File directory;
    private final List commandFiles = new ArrayList();


    public SqlDirectory(File directory) {
        this.directory = directory;
    }


    public File getDirectory() {
        return directory;
    }


    public List getCommandFiles() {
        return commandFiles;
    }
}
