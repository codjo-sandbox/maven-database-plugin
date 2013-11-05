/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database;
import java.io.File;
import net.codjo.maven.mojo.database.scm.CheckOutCommand;
import net.codjo.maven.mojo.database.scm.CheckOutConfig;
import net.codjo.test.common.LogString;
import org.apache.maven.plugin.MojoExecutionException;
/**
 *
 */
public class CheckoutFromProdMojoTest extends DatabaseMojoTestCase {
    private LogString log = new LogString();


    public void test_initFromProd() throws Exception {
        CheckoutFromProdMojo mojo = getCheckoutFromProdMojo("pom-default.xml");

        mojo.setCheckOutCommand(new CheckOutCommandMock());

        mojo.execute();

        log.assertContent("checkout.execute(roses-2.00.00.00-h, target\\checkout)");
    }


    private CheckoutFromProdMojo getCheckoutFromProdMojo(String testPom)
          throws Exception {
        return (CheckoutFromProdMojo)lookupMojo("checkout-from-prod", "checkoutFromProd/" + testPom);
    }


    protected File getSqlFile(String path) {
        return getTestFile("src/test/resources-filtered/mojos/checkoutFromProd/" + path);
    }


    private class CheckOutCommandMock extends CheckOutCommand {

        public void execute(CheckOutConfig config)
              throws MojoExecutionException {
            log.call("checkout.execute", config.getTag(), config.getCheckoutDirectory());
        }
    }


    protected void tearDown() throws Exception {
        fixture.advanced().dropAllObjects();
        super.tearDown();
    }
}
