/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database.scm;
import java.io.IOException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
/**
 *
 */
public class CheckOutCommand {
    private Log log = new SystemStreamLog();


    public void setLog(Log log) {
        this.log = log;
    }


    public void execute(CheckOutConfig config) throws MojoExecutionException {
        if (!config.getCheckoutDirectory().isDirectory() || !config.isSkipCheckoutIfExists()) {
            checkout(config);
        }
    }


    private void checkout(CheckOutConfig config) throws MojoExecutionException {
        cleanUpTargetDirectory(config);

        try {
            ScmRepository repository = config.getScmRepository();
            ScmProvider provider = config.getManager().getProviderByRepository(repository);

            CheckOutScmResult result =
                  provider.checkOut(repository,
                                    new ScmFileSet(config.getCheckoutDirectory().getAbsoluteFile()),
                                    config.getTag());

            checkResult(result);
        }
        catch (ScmException e) {
            log.info(e);
            throw new MojoExecutionException("Cannot run checkout command : ", e);
        }
    }


    private void cleanUpTargetDirectory(CheckOutConfig config)
          throws MojoExecutionException {
        try {
            log.info("Removing " + config.getCheckoutDirectory());

            FileUtils.deleteDirectory(config.getCheckoutDirectory());
        }
        catch (IOException e) {
            throw new MojoExecutionException("Cannot remove " + config.getCheckoutDirectory());
        }
        if (!config.getCheckoutDirectory().mkdirs()) {
            throw new MojoExecutionException("Cannot create " + config.getCheckoutDirectory());
        }
    }


    private void checkResult(ScmResult result) throws MojoExecutionException {
        if (!result.isSuccess()) {
            log.error("Provider message:");

            log.error(result.getProviderMessage() == null ? "" : result.getProviderMessage());

            log.error("Command output:");

            log.error(result.getCommandOutput() == null ? "" : result.getCommandOutput());

            throw new MojoExecutionException("Command failed."
                                             + StringUtils.defaultString(result.getProviderMessage()));
        }
    }
}
