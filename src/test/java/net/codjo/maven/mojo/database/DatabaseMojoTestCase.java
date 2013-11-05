package net.codjo.maven.mojo.database;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.database.common.api.DatabaseScriptHelper;
import net.codjo.database.common.api.JdbcFixture;
import net.codjo.database.common.api.structure.SqlTable;
import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Replace;
public abstract class DatabaseMojoTestCase extends AbstractMojoTestCase {
    protected JdbcFixture fixture;
    protected static final String TEST_TABLE = "PLUGIN_DATABASE_TEST_TABLE";
    private final DatabaseFactory databaseFactory = new DatabaseFactory();
    private final DatabaseScriptHelper databaseScriptHelper = databaseFactory.createDatabaseScriptHelper();


    protected void setUp() throws Exception {
        super.setUp();
        fixture = databaseFactory.createJdbcFixture();
        fixture.doSetUp();
        fixture.advanced().dropAllObjects();
        fixture.create(SqlTable.table(TEST_TABLE), "COL_TEXT varchar(255)");
    }


    protected void tearDown() throws Exception {
        fixture.doTearDown();
        super.tearDown();
    }


    protected DatabaseFactory getDatabaseFactory() {
        return databaseFactory;
    }


    protected DatabaseScriptHelper getDatabaseScriptHelper() {
        return databaseScriptHelper;
    }


    protected Mojo lookupMojo(String goal, String pomFile)
          throws Exception {
        try {
            return super.lookupMojo(goal, getPomFile(pomFile));
        }
        catch (Exception e) {
            fail("lookup en echec : " + e.getLocalizedMessage());
        }
        return null;
    }


    protected File getPomFile(String path) {
        return getTestFile("target/test-classes/mojos/" + path);
    }


    protected void execute(AbstractMojo mojo, String module)
          throws MojoExecutionException, MojoFailureException {
        replaceScriptDelimiter(module, "${queryDelimiter}", getDatabaseScriptHelper().getQueryDelimiter());
        try {
            mojo.execute();
        }
        finally {
            replaceScriptDelimiter(module, getDatabaseScriptHelper().getQueryDelimiter(),
                                   "${queryDelimiter}");
        }
    }


    protected void replaceScriptDelimiter(File file, String token, String value) {
        Replace replace = new Replace();
        replace.setToken(token);
        replace.setValue(value);
        replace.setDir(file);
        replace.setProject(new Project());
        replace.execute();
    }


    protected void replaceScriptDelimiter(String module, String token, String value) {
        replaceScriptDelimiter(new File("target/test-classes/mojos/" + module), token, value);
    }
}
