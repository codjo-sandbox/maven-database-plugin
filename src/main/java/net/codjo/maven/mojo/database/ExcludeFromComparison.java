package net.codjo.maven.mojo.database;
import java.util.ArrayList;
import java.util.List;
/**
 *
 */
public class ExcludeFromComparison {
    private final List tables = new ArrayList();
    private final List procedures = new ArrayList();


    public List getTables() {
        return tables;
    }


    public List getProcedures() {
        return procedures;
    }
}
