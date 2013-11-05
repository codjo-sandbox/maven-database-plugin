/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database;
import java.io.File;
import java.util.List;
import net.codjo.database.common.api.ConnectionMetadata;
import net.codjo.database.common.api.ExecSqlScript;
import net.codjo.test.common.LogString;
public class InitFromProdMojoTest extends DatabaseMojoTestCase {
    private static final String TARGET_PATH = "target\\test-classes\\mojos\\initFromProd";
    private LogString log = new LogString();


    protected void setUp() throws Exception {
        super.setUp();
        log.clear();
        System.getProperties().setProperty("process", "integration");
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        System.getProperties().remove("process");
    }


    public void test_versionInProductionMissing() throws Exception {
        InitFromProdMojo mojo = initMojo("initFromProd/pom-default.xml");

        execute(mojo, "initFromProd");

        log.assertContent("");
    }


    public void test_versionInProductionMissing_givenAsArgument() throws Exception {
        InitFromProdMojo mojo = initMojo("initFromProd/pom-default.xml");
        mojo.versionInProduction = "1.1";

        execute(mojo, "initFromProd");

        log.assertContent("maven.execute(" + InitFromProdMojo.CHECKOUT_PROD_GOAL + ", " + TARGET_PATH
                          + ", {versionInProduction=1.1}), "
                          + "maven.execute(install, " + TARGET_PATH + "\\checkout, "
                          + "{maven.datagen.skip.compile=true, "
                          + "maven.reactor.excludes=*-batch/pom.xml,*-gui/pom.xml,*-client/pom.xml,*-server/pom.xml,*-web/pom.xml,*-release-test/pom.xml, "
                          + "maven.reactor.includes=*/pom.xml, "
                          + "maven.test.skip.exec=true})");
    }


    public void test_fullInit() throws Exception {
        InitFromProdMojo mojo = initMojo("initFromProd/pom-fullInit.xml");
        mojo.versionInProduction = "1.1";

        execute(mojo, "initFromProd");

        log.assertContent("maven.execute(" + InitFromProdMojo.CHECKOUT_PROD_GOAL + ", " + TARGET_PATH
                          + ", {versionInProduction=1.1}), "
                          + "maven.execute(install, " + TARGET_PATH + "\\checkout, "
                          + "{maven.reactor.includes=*/pom.xml, "
                          + "maven.test.skip.exec=true})");
    }


    public void test_execute() throws Exception {
        InitFromProdMojo mojo = initMojo("initFromProd/pom-runReleaseScripts.xml");
        mojo.versionInProduction = "1.1";
        mojo.runReleaseScripts = true;
        mojo.releaseScriptsFile = new File(MockUtil.getInputFile("initFromProd/emptyReleaseScriptsFile.txt"));

        execute(mojo, "initFromProd");

        log.assertContent("maven.execute(" + InitFromProdMojo.CHECKOUT_PROD_GOAL + ", " + TARGET_PATH
                          + ", {versionInProduction=1.1}), "
                          + "maven.execute(install, " + TARGET_PATH + "\\checkout, "
                          + "{maven.reactor.includes=*/pom.xml, maven.test.skip.exec=true}), "
                          + "execute(procedureOne.sql), "
                          + "execute(procedureOne.sql)");
    }


    public void test_assertDataserver() throws Exception {
        InitFromProdMojo mojo = initMojo("initFromProd/pom-fullInit.xml");
        mojo.versionInProduction = "1.1";
        mojo.assertDataserver = true;

        execute(mojo, "initFromProd");

        log.assertContent("maven.execute(net.codjo.maven.mojo:maven-database-plugin:checkout-from-prod"
                          + ", target\\test-classes\\mojos\\initFromProd"
                          + ", {versionInProduction=1.1}), "
                          + "maven.execute(net.codjo.maven.mojo:maven-database-plugin:assert-dataserver"
                          + ", target\\test-classes\\mojos\\initFromProd\\checkout"
                          + ", {expectedDatabasePort=databasePort, expectedDatabaseServer=databaseServer"
                          + ", expectedDatabaseCatalog=databaseCatalog, expectedDatabaseBase=databaseBase}), "
                          + "maven.execute(install"
                          + ", target\\test-classes\\mojos\\initFromProd\\checkout"
                          + ", {maven.reactor.includes=*/pom.xml, maven.test.skip.exec=true})");
    }


    public void test_assertDataserver_withASpecifiedDatabaseType() throws Exception {
        InitFromProdMojo mojo = initMojo("initFromProd/pom-fullInit.xml");
        mojo.versionInProduction = "1.1";
        mojo.assertDataserver = true;
        mojo.setDatabaseType("sybase");

        execute(mojo, "initFromProd");

        log.assertContent("maven.execute(net.codjo.maven.mojo:maven-database-plugin:checkout-from-prod"
                          + ", target\\test-classes\\mojos\\initFromProd"
                          + ", {versionInProduction=1.1}), "
                          + "maven.execute(net.codjo.maven.mojo:maven-database-plugin:assert-dataserver"
                          + ", target\\test-classes\\mojos\\initFromProd\\checkout"
                          + ", {expectedDatabasePort=databasePort, expectedDatabaseServer=databaseServer"
                          + ", expectedDatabaseCatalog=databaseCatalog, expectedDatabaseBase=databaseBase"
                          + ", databaseType=sybase}), "
                          + "maven.execute(install"
                          + ", target\\test-classes\\mojos\\initFromProd\\checkout"
                          + ", {maven.reactor.includes=*/pom.xml, maven.test.skip.exec=true})");
    }


    private InitFromProdMojo initMojo(String pomFilePath) throws Exception {
        MockUtil.setupEnvironment(pomFilePath);
        InitFromProdMojo mojo = (InitFromProdMojo)lookupMojo("init-from-prod", pomFilePath);

        ExecSqlScript mockExecSqlScript = new ExecSqlScript() {
            public void setConnectionMetadata(ConnectionMetadata connectionMetadata) {
            }


            public void setLogger(Logger logger) {
            }


            public void executeContentOfFile(String deliveryFileName) {
                log.call("executeContentOfFile", deliveryFileName);
            }


            public void execute(String workingDirectory, String[] scriptFileNames) {
                log.call("execute", scriptFileNames);
            }


            public void execute(String workingDirectory, List scriptFileNames) {
                log.call("execute", scriptFileNames);
            }
        };
        mojo.setExecSqlScript(mockExecSqlScript);
        mojo.setCheckoutProjectMavenCommand(new MavenCommandMock(log));
        mojo.setAssertDataserverCommand(new MavenCommandMock(log));
        mojo.setInstallCommand(new MavenCommandMock(log));

        return mojo;
    }
}
