package net.codjo.maven.mojo.database;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.test.common.LogString;
import net.codjo.test.common.fixture.DirectoryFixture;
import java.io.File;
import junit.framework.Assert;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
public class InitFromDevMojoTest extends DatabaseMojoTestCase {
    private LogString log = new LogString();
    private DirectoryFixture directoryFixture;


    protected void setUp() throws Exception {
        super.setUp();
        directoryFixture = DirectoryFixture.newTemporaryDirectoryFixture();
        directoryFixture.doSetUp();
    }


    protected void tearDown() throws Exception {
        directoryFixture.doTearDown();
        super.tearDown();
    }


    public void test_init() throws Exception {
        executeAndAssert("init", "pom-default.xml", new String[][]{{"MESSAGE"},
                                                                   {"TABLE"},
                                                                   {"VIEW"},
                                                                   {"GAP"},
                                                                   {"INDEX"},
                                                                   {"CONSTRAINT"},
                                                                   {"RULE"},
                                                                   {"PROCEDURE.PARENT"},
                                                                   {"PROCEDURE.CHILD"},
                                                                   {"TRIGGER"},
                                                                   {"REVOKE"},
                                                                   {"GRANT"}});

        log.assertContent(
              "maven.execute(net.codjo.maven.mojo:maven-database-plugin:drop, target\\test-classes\\mojos\\init, null)");
    }

//    public void test_initDontExecuteIfUseless() throws Exception {
//        InitFromDevMojo mojo = initMojo("init/pom-default.xml");
//        MojoLogString mojoLog = new MojoLogString();
//        mojo.setLog(mojoLog);
//
//        initDropDBMockCommand(mojo);
//        mojo.execute();
//
//        log.assertContent(
//              "maven.execute(net.codjo.maven.mojo:maven-database-plugin:drop, target\\test-classes\\mojos\\init)");
//        mojoLog.assertContains("Initialisation de la base en cours.");
//        mojoLog.clear();
//        log.clear();
//
//        // Try to do it again
//        mojo.execute();
//
//        log.assertContent("");
//        mojoLog.assertContains("Initialisation de la base non necessaire.");
//    }


    public void test_shouldExecuteSQL() throws Exception {
        InitFromDevMojo mojo = initMojo("init/pom-default.xml");
        MojoLogString mojoLog = new MojoLogString();
        mojo.setLog(mojoLog);

        initDropDBMockCommand(mojo);

        replaceScriptDelimiter("init", "${queryDelimiter}", getDatabaseScriptHelper().getQueryDelimiter());
        assertTrue(mojo.shouldExecuteSQL());
        try {
            mojo.execute();
            assertFalse(mojo.shouldExecuteSQL());
        }
        finally {
            replaceScriptDelimiter("init", getDatabaseScriptHelper().getQueryDelimiter(),
                                   "${queryDelimiter}");
        }
        assertTrue(mojo.shouldExecuteSQL());
    }


    public void test_init_exception() throws Exception {
        //TODO messages informatifs supprimés avec argument '-m 12' donc ce test passe plus
        InitFromDevMojo mojo = initMojo("initException/pom-with-badProcedureOrder.xml");

        initDropDBMockCommand(mojo);
        try {
            execute(mojo, "initException");
            fail();
        }
        catch (MojoExecutionException ex) {
            // Tout est ok
        }
    }


    public void test_init_withGeneratedFolder() throws Exception {
        InitFromDevMojo mojo = initMojo("init/pom-withInclude.xml");

        copyZipArtifactSql();

        TestUtil.addDependencyManagement("test", "artifact", "sql", "1.0", MockUtil.singleton.getProject());

        MockUtil.singleton.getArtifactRepository().setUrl(MockUtil.toUrl(
              "target/test-classes/mojos/init/generated"));

        initDropDBMockCommand(mojo);

        execute(mojo, "init");

        String[][] expected = {{"MESSAGE"},
                               {"GENERATED.TABLE"},
                               {"TABLE"},
                               {"VIEW"},
                               {"GAP"},
                               {"INDEX"},
                               {"CONSTRAINT"},
                               {"RULE"},
                               {"PROCEDURE.PARENT"},
                               {"PROCEDURE.CHILD"},
                               {"TRIGGER"},
                               {"REVOKE"},
                               {"GRANT"}};
        fixture.assertContent(SqlTable.table(TEST_TABLE), expected);

        log.assertContent(
              "maven.execute(net.codjo.maven.mojo:maven-database-plugin:drop, target\\test-classes\\mojos\\init, null)");
    }


    public void test_init_withGeneratedFolderWrongDependency()
          throws Exception {
        InitFromDevMojo mojo = initMojo("init/pom-withWrongInclude.xml");

        copyZipArtifactSql();

        TestUtil.addDependencyManagement("test", "artifact", "sql", "1.0", MockUtil.singleton.getProject());

        MockUtil.singleton.getArtifactRepository().setUrl(MockUtil.toUrl(
              "target/test-classes/mojos/init/generated"));

        initDropDBMockCommand(mojo);

        try {
            execute(mojo, "init");
            fail("La dépendance n'existe pas, ça devrait partir en erreur");
        }
        catch (Exception e) {
            fixture.assertIsEmpty(SqlTable.table(TEST_TABLE));
        }
    }


    public void test_init_noProcedureOrderFile() throws Exception {
        executeAndAssert("initNoProcedure", "pom-default.xml", new String[][]{{"TABLE"}});
    }


    public void test_init_noProcedureWithEmptyOrder() throws Exception {
        executeAndAssert("initNoProcedure", "pom-withEmptyOrder.xml", new String[][]{{"TABLE"}});
    }


    public void test_init_cleanBefore() throws Exception {
        // UGLY : ce test peut planter quand on fait des modifs du plugin database (évolutions du pom).
        // Dans ce cas, le plugin doit être installer en local pour que le test passe.
        // Faire un mvn install sans les tests pour le faire passer.

        fixture.create(SqlTable.table("REF_PERSON"), "NAME varchar(20)");
        fixture.create(SqlTable.table("REF_DEPARTMENT"), "NAME varchar(20)");

        InitFromDevMojo mojo = initMojo("initCleanBefore/pom.xml");

        execute(mojo, "initCleanBefore");

        fixture.advanced().assertDoesntExist("REF_PERSON");
        fixture.advanced().assertDoesntExist("REF_DEPARTMENT");

        fixture.advanced().assertDoesntExist(TEST_TABLE);
    }


    public void test_init_recursive() throws Exception {
        // UGLY : cf test_init_cleanBefore
        File fileDest = new File("target\\test-classes\\mojos\\initRecursive\\table\\.svn");
        if (!fileDest.exists()) {
            File fileSrc = new File("target\\test-classes\\mojos\\initRecursive\\table\\.toBeSvn");
            fileSrc.renameTo(fileDest);
        }

        executeAndAssert("initRecursive", "pom.xml", new String[][]{{"TABLE.SUBFUNCTION"},
                                                                    {"PROCEDURE.SUBFUNCTION"}});
    }


    public void test_init_withTestSql() throws Exception {
        executeAndAssert("initWithTestSql", "pom.xml", new String[][]{{"TABLE_TEST"},
                                                                      {"TABLE"}});
    }


    public void test_init_filteredSql() throws Exception {
        executeAndAssert("initFilteredSql", "pom.xml", new String[][]{{"AP_TABLE_FILTERED NEW_COL"},
                                                                      {"VU_TABLE_FILTERED"}});
    }


    public void test_init_initWithTestProcedureOrder() throws Exception {
        executeAndAssert("initWithTestProcedureOrder", "pom.xml",
                         new String[][]{{"AP_TABLE_TEST"}, {"AP_TABLE"},
                                        {"sp_proc_test"}, {"sp_proc_2"}}
        );
    }


    public void test_setApplicationUserToGroup() throws Exception {
        InitFromDevMojo mojo = initMojo("setApplicationUserToGroup/pom.xml");

        initDropDBMockCommand(mojo);
        mojo.setApplicationUserToGroup();

        fixture.advanced()
              .assertUserInGroup(mojo.getDatabaseApplicationUser(), mojo.getDatabaseApplicationGroup());
    }


    public void test_setApplicationUserToGroup_noDatabaseAppUser() throws Exception {
        InitFromDevMojo mojo = initMojo("setApplicationUserToGroup/pom-noDatabaseUser.xml");

        initDropDBMockCommand(mojo);
        mojo.setApplicationUserToGroup();
    }


    private void executeAndAssert(String module, String pomFileName, String[][] expected) throws Exception {
        InitFromDevMojo mojo = initMojo(module + "/" + pomFileName);

        initDropDBMockCommand(mojo);

        execute(mojo, module);

        fixture.assertContent(SqlTable.table(TEST_TABLE), expected);
    }


    private void initDropDBMockCommand(InitFromDevMojo mojo) {
        mojo.setDropDatabaseMavenCommand(new MavenCommandMock(log));
    }


    private InitFromDevMojo initMojo(String pomFilePath) throws Exception {
        MockUtil.setupEnvironment(pomFilePath);
        return (InitFromDevMojo)lookupMojo("init", pomFilePath);
    }


    private void copyZipArtifactSql() {
        File srcFile = new File(
              MockUtil.getInputFile("init/generated/test/artifact/1.0/artifact-1.0-sql.zip"));

//        MockUtil.getInputFile("init/generated/test/artifact/1.0/artifact-1.0-sql.zip");

        Expand expand = new Expand();
        expand.setSrc(srcFile);
        expand.setDest(directoryFixture);
        expand.setProject(new Project());
        expand.execute();

        replaceScriptDelimiter(directoryFixture, "${queryDelimiter}",
                               getDatabaseScriptHelper().getQueryDelimiter());

        Zip zip = new Zip();
        FileSet fileSet = new FileSet();
        fileSet.setDir(directoryFixture);
        zip.addFileset(fileSet);
        zip.setDestFile(
              new File("target/test-classes/mojos/init/generated/test/artifact/1.0/artifact-1.0-sql.zip"));
        zip.setProject(new Project());
        zip.execute();

//        File destDir = new File("target/test-classes/mojos/init/generated/test/artifact/1.0");
//        AntUtil.copyFile(srcFile, destDir);
    }


    private class MojoLogString implements Log {
        private StringBuffer log = new StringBuffer();


        public String getContent() {
            return log.toString();
        }


        public void assertContent(String expectedContent) {
            Assert.assertEquals(expectedContent, getContent());
        }


        public void assertContains(String expected) {
            Assert.assertTrue(log.indexOf(expected) != -1);
        }


        public void clear() {
            log.setLength(0);
        }


        public boolean isDebugEnabled() {
            return true;
        }


        public void debug(CharSequence content) {
            debug(content, null);
        }


        public void debug(Throwable error) {
            debug("", error);
        }


        public void debug(CharSequence content, Throwable error) {
            log("debug", content, error);
        }


        public boolean isInfoEnabled() {
            return true;
        }


        public void info(CharSequence content) {
            info(content, null);
        }


        public void info(Throwable error) {
            info("", error);
        }


        public void info(CharSequence content, Throwable error) {
            log("info", content, error);
        }


        public boolean isWarnEnabled() {
            return true;
        }


        public void warn(CharSequence content) {
            warn(content, null);
        }


        public void warn(Throwable error) {
            warn("", error);
        }


        public void warn(CharSequence content, Throwable error) {
            log("warn", content, error);
        }


        public boolean isErrorEnabled() {
            return true;
        }


        public void error(CharSequence content) {
            error(content, null);
        }


        public void error(Throwable error) {
            error("", error);
        }


        public void error(CharSequence content, Throwable error) {
            log("error", content, error);
        }


        private void log(String logLevel, CharSequence content, Throwable error) {
            log.append("[").append(logLevel).append("]").append(content);
            if (error != null) {
                log.append(" - ").append(error.getLocalizedMessage());
            }
        }


        public String toString() {
            return log.toString();
        }
    }
}
