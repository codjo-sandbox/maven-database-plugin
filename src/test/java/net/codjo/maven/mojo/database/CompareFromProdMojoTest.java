package net.codjo.maven.mojo.database;
import net.codjo.database.common.api.ObjectType;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.test.common.LogString;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
public class CompareFromProdMojoTest extends DatabaseMojoTestCase {
    private LogString log = new LogString();


    protected void setUp() throws Exception {
        super.setUp();
        doSetupDevDB();
    }


    protected void tearDown() throws Exception {
        fixture.advanced().dropAllObjects();
        super.tearDown();
    }


    public void test_versionInProductionMissing() throws Exception {
        CompareFromProdMojo mojo = getCompareFromProdMojo("pom-versionInProductionMissing.xml");
        mojo.setInitFromProdCommand(mockInitFromProd());

        mojo.execute();

        assertNull(mojo.getVersionInProduction());
        log.assertContent("");
    }


    public void test_versionInProductionMissing_givenAsArgument() throws Exception {
        CompareFromProdMojo mojo = getCompareFromProdMojo("pom-versionInProductionMissing.xml");
        mojo.setInitFromProdCommand(mockInitFromProd());
        mojo.versionInProduction = "1.1";

        mojo.execute();

        log.assertContent("maven.execute(net.codjo.maven.mojo:maven-database-plugin:init-from-prod, "
                          + "target\\test-classes\\mojos\\compareFromProd\\app-dev\\app-sql, "
                          + "{versionInProduction=1.1, assertDataserver=true})");
    }


    public void test_databasesAreTheSame() throws Exception {
        CompareFromProdMojo mojo = getCompareFromProdMojo("pom-database-equal.xml");
        mojo.setInitFromProdCommand(mockInitFromProd());

        try {
            mojo.execute();

            fixture.advanced().assertObjectExists("VU_REF_PERSON", ObjectType.VIEW);
            fixture.advanced().assertObjectExists("VU_REF_GROUP", ObjectType.VIEW);
            fixture.executeQuery("select ENABLED from REF_GROUP");

            assertEquals("roses-2.00.00.00-h", mojo.getVersionInProduction());
            log.assertContent(
                  "maven.execute(net.codjo.maven.mojo:maven-database-plugin:init-from-prod,"
                  + " target\\test-classes\\mojos\\compareFromProd\\app-dev\\app-sql, {versionInProduction=roses-2.00.00.00-h, assertDataserver=true})");
        }
        catch (MojoFailureException exception) {
            assertEquals("La comparaison de base n'est pas encore implementee pour ce type de base",
                         exception.getMessage());
        }
    }


    public void test_databaseAreTheSame_withExcludeFromComparison() throws Exception {
        try {
            fixture.executeUpdate("create proc sp_select_person as begin select 1 end");
            CompareFromProdMojo mojo = getCompareFromProdMojo("pom-withExcludeFromComparison.xml");
            mojo.setInitFromProdCommand(mockInitFromProd());

            try {
                mojo.execute();
            }
            catch (MojoFailureException exception) {
                assertEquals("La comparaison de base n'est pas encore implementee pour ce type de base",
                             exception.getMessage());
            }
        }
        finally {
            fixture.executeUpdate("drop proc sp_select_person");
        }
    }


    public void test_databasesAreDifferent() throws Exception {
        CompareFromProdMojo mojo = getCompareFromProdMojo("pom-database-differ.xml");
        mojo.setInitFromProdCommand(mockInitFromProd());
        try {
            mojo.execute();
            fail();
        }
        catch (MojoExecutionException exception) {
            assertEquals("La base de donnees de production est differente de celle de developpement.",
                         exception.getMessage());
        }
        catch (MojoFailureException exception) {
            assertEquals("La comparaison de base n'est pas encore implementee pour ce type de base",
                         exception.getMessage());
        }
    }


    public void test_withSqlBaseDirTest() throws Exception {
        CompareFromProdMojo mojo = getCompareFromProdMojo("pom-withSqlBaseDirTest.xml");
        mojo.setInitFromProdCommand(mockInitFromProd());

        try {
            mojo.execute();

            fixture.advanced().assertObjectExists("VU_REF_PERSON", ObjectType.VIEW);
            fixture.advanced().assertObjectExists("VU_REF_GROUP", ObjectType.VIEW);
            fixture.executeQuery("select ENABLED from REF_GROUP");

            assertEquals("roses-2.00.00.00-h", mojo.getVersionInProduction());
            log.assertContent(
                  "maven.execute(net.codjo.maven.mojo:maven-database-plugin:init-from-prod,"
                  + " target\\test-classes\\mojos\\compareFromProd\\app-dev\\app-sql, {versionInProduction=roses-2.00.00.00-h, assertDataserver=true})");
        }
        catch (MojoFailureException exception) {
            assertEquals("La comparaison de base n'est pas encore implementee pour ce type de base",
                         exception.getMessage());
        }
    }


    public void test_runReleaseScriptsTwice() throws Exception {
        CompareFromProdMojo mojo = getCompareFromProdMojo("pom-database-runReleaseScriptsTwice.xml");
        InitFromProdCommandMock initFromProdCommand = new InitFromProdCommandMock();
        initFromProdCommand.mockCreateTable("REF_GROUP", "GROUP_CODE      varchar(20)  not null");
        mojo.setInitFromProdCommand(initFromProdCommand);
        try {
            mojo.execute();
            fail();
        }
        catch (MojoExecutionException exception) {
            assertTrue(exception.getMessage().contains("Erreur SQL"));
        }
        catch (MojoFailureException exception) {
            assertEquals("La comparaison de base n'est pas encore implementee pour ce type de base",
                         exception.getMessage());
        }
    }


    private void doSetupDevDB() throws SQLException, IOException {
        fixture.advanced().dropAllObjects();

        fixture.create(SqlTable.table("REF_GROUP"), "   GROUP_CODE      varchar(20)  not null,"
                                                    + " ENABLED         char         null");
        fixture.create(SqlTable.table("REF_PERSON"), "   PERSON_CODE      varchar(20)  not null,"
                                                     + " PERSON_NAME      varchar(255) not null,"
                                                     + " SECTION_CODE     varchar(6)   null,"
                                                     + " EMAIL            varchar(50)  null");
        fixture.create(SqlTable.table("AP_GROUP_PERSON"), "   PERSON_CODE      varchar(20)  not null,"
                                                          + " GROUP_CODE       varchar(20)  not null,"
                                                          + " TITLE            varchar(20)  ");
        fixture.executeUpdate("create view VU_REF_PERSON\n"
                              + " as\n"
                              + "    select 1 as ID from REF_PERSON");
        fixture.executeUpdate("create view VU_REF_GROUP\n"
                              + " as\n"
                              + "    select 1 as ID from REF_GROUP");
    }


    private InitFromProdCommandMock mockInitFromProd() {
        InitFromProdCommandMock initFromProdCommandMock = new InitFromProdCommandMock();
        initFromProdCommandMock.mockCreateTable("REF_GROUP", "GROUP_CODE      varchar(20)  not null");
        initFromProdCommandMock.mockCreateTable("REF_PERSON", "   PERSON_CODE      varchar(20)  not null, "
                                                              + " PERSON_NAME      varchar(255) not null, "
                                                              + " SECTION_CODE     varchar(6)   null,"
                                                              + " EMAIL            varchar(50)  null");
        initFromProdCommandMock.mockCreateTable("AP_GROUP_PERSON",
                                                "    PERSON_CODE      varchar(20)  not null,"
                                                + "  GROUP_CODE       varchar(20)  not null, "
                                                + "  TITLE            varchar(20)  ");

        return initFromProdCommandMock;
    }


    private CompareFromProdMojo getCompareFromProdMojo(String testPom) throws Exception {
        return (CompareFromProdMojo)lookupMojo("compare-from-prod",
                                               "compareFromProd/app-prod/app-sql/" + testPom);
    }


    protected File getSqlFile(String path) {
        return getTestFile("target/test-classes/mojos/compareFromProd/" + path);
    }


    private class InitFromProdCommandMock extends MavenCommandMock {
        private final Map tables = new HashMap();


        InitFromProdCommandMock() {
            super(CompareFromProdMojoTest.this.log);
        }


        public void execute(String[] goals, File targetDirectory, Log log, boolean reactorMode)
              throws Exception {
            super.execute(goals, targetDirectory, log, reactorMode);
            initProdDatabase();
        }


        public void mockCreateTable(String tableName, String structure) {
            tables.put(tableName, structure);
        }


        private void initProdDatabase() throws SQLException, IOException {
            fixture.advanced().dropAllObjects();
            for (Iterator it = tables.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                fixture.create(SqlTable.table((String)entry.getKey()), (String)entry.getValue());
            }
        }
    }
}
