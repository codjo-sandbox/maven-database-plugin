/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database;
import java.io.File;
import java.util.Iterator;
import net.codjo.maven.mojo.database.scm.CheckOutCommand;
import net.codjo.maven.mojo.database.scm.CheckOutConfig;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.StringUtils;
/**
 * Récupère la version de production spécifiée.
 *
 * @goal checkout-from-prod
 * @phase pre-integration-test
 * @aggregator
 * @noinspection UNUSED_SYMBOL
 */
public class CheckoutFromProdMojo extends AbstractMojo implements CheckOutConfig {
    // ********************************************************************************* From AbstractScmMojo
    /**
     * The SCM connection URL.
     *
     * @parameter expression="${connectionUrl}" default-value="${project.scm.connection}"
     */
    private String connectionUrl;
    /**
     * @parameter expression="${connectionUrl}" default-value="${project.scm.developerConnection}"
     */
    private String developerConnectionUrl;
    /**
     * The type of connection to use (connection or developerConnection).
     *
     * @parameter expression="${connectionType}" default-value="connection"
     */
    private String connectionType;
    /**
     * The user name (used by svn and starteam protocol).
     *
     * @parameter expression="${username}"
     */
    private String username;
    /**
     * The user password (used by svn and starteam protocol).
     *
     * @parameter expression="${password}"
     */
    private String password;
    /**
     * The private key (used by java svn).
     *
     * @parameter expression="${privateKey}"
     */
    private String privateKey;
    /**
     * The passphrase (used by java svn).
     *
     * @parameter expression="${passphrase}"
     */
    private String passphrase;
    /**
     * The url of tags base directory (used by svn protocol). Not necessary to set it if you use standard svn layout
     * (branches/tags).
     *
     * @parameter expression="${tagBase}"
     */
    private String tagBase;
    /**
     * @parameter expression="${component.org.apache.maven.scm.manager.ScmManager}"
     * @required
     * @readonly
     */
    private ScmManager manager;
    /**
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    // ********************************************************************************* From CheckoutMojo
    /**
     * The directory to checkout the sources to for the bootstrap and checkout goals
     *
     * @parameter expression="${checkoutDirectory}" default-value="${project.build.directory}/checkout"
     */
    private File checkoutDirectory;
    /**
     * Skip checkout if checkoutDirectory exists.
     *
     * @parameter expression="${skipCheckoutIfExists}" default-value="false"
     */
    private boolean skipCheckoutIfExists;

    /**
     * The production tag to be compared with the development version
     *
     * @parameter expression="${maven.database.versionInProduction}" default-value="${versionInProduction}"
     * @noinspection UnusedDeclaration
     */
    private String versionInProduction;

    /**
     * @parameter expression="${project}"
     * @required
     * @noinspection UNUSED_SYMBOL
     */
    protected MavenProject project;

    private CheckOutCommand checkOutCommand = new CheckOutCommand();


    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Working on " + getTag() + " (" + getTag().getClass().getSimpleName() + ")");
            executeImpl();
        }
        catch (MojoExecutionException exception) {
            throw exception;
        }
        catch (MojoFailureException exception) {
            throw exception;
        }
        catch (Exception exception) {
            throw new MojoExecutionException(exception.getLocalizedMessage(), exception);
        }
    }


    protected String getGoalName() {
        return "checkout-from-prod";
    }


    public void setCheckOutCommand(CheckOutCommand command) {
        checkOutCommand = command;
    }


    public void executeImpl() throws Exception {
        checkOutCommand.execute(this);
        getLog().info("");
        getLog().info("Checkout of version '" + getTag() + "' done");
        getLog().info("");
    }


    public String getConnectionUrl() {
        return connectionUrl;
    }


    public String getDeveloperConnectionUrl() {
        return developerConnectionUrl;
    }


    public String getConnectionType() {
        return connectionType;
    }


    public ScmManager getManager() {
        return manager;
    }


    public ScmVersion getTag() {
        if (StringUtils.isEmpty(tagBase)) {
            return new ScmTag(versionInProduction);
        }
        else if ("tags".equals(tagBase)) {
            return new ScmTag(versionInProduction);
        }
        else if ("branches".equals(tagBase)) {
            return new ScmBranch(versionInProduction);
        }
        else {
            throw new InternalError(
                  "TagBase value is invalid should be empty, 'tags' or 'branches' but was " + tagBase);
        }
    }


    public File getCheckoutDirectory() {
        return checkoutDirectory;
    }


    public boolean isSkipCheckoutIfExists() {
        return skipCheckoutIfExists;
    }


    public ScmRepository getScmRepository() throws ScmException {
        ScmRepository repository;

        try {
            repository = getManager().makeScmRepository(getConnectionUrl());

            ScmProviderRepository providerRepo = repository.getProviderRepository();

            if (!StringUtils.isEmpty(username)) {
                providerRepo.setUser(username);
            }

            if (!StringUtils.isEmpty(password)) {
                providerRepo.setPassword(password);
            }

            if (repository.getProviderRepository() instanceof ScmProviderRepositoryWithHost) {
                initRepositoryHost(repository);
            }
        }
        catch (ScmRepositoryException e) {
            if (!e.getValidationMessages().isEmpty()) {
                for (Iterator i = e.getValidationMessages().iterator(); i.hasNext(); ) {
                    String message = (String)i.next();
                    getLog().error(message);
                }
            }

            throw new ScmException("Can't load the scm provider.", e);
        }
        catch (Exception e) {
            throw new ScmException("Can't load the scm provider.", e);
        }

        return repository;
    }


    private void initRepositoryHost(ScmRepository repository) {
        ScmProviderRepositoryWithHost repo =
              (ScmProviderRepositoryWithHost)repository.getProviderRepository();

        loadInfosFromSettings(repo);

        if (!StringUtils.isEmpty(username)) {
            repo.setUser(username);
        }

        if (!StringUtils.isEmpty(password)) {
            repo.setPassword(password);
        }

        if (!StringUtils.isEmpty(privateKey)) {
            repo.setPrivateKey(privateKey);
        }

        if (!StringUtils.isEmpty(passphrase)) {
            repo.setPassphrase(passphrase);
        }
    }


    /**
     * Load username password from settings if user has not set them in JVM properties
     */
    private void loadInfosFromSettings(ScmProviderRepositoryWithHost repo) {
        if (username == null || password == null) {
            String host = repo.getHost();

            int port = repo.getPort();

            if (port > 0) {
                host += ":" + port;
            }

            Server server = settings.getServer(host);

            if (server != null) {
                if (username == null) {
                    username = settings.getServer(host).getUsername();
                }

                if (password == null) {
                    password = settings.getServer(host).getPassword();
                }

                if (privateKey == null) {
                    privateKey = settings.getServer(host).getPrivateKey();
                }

                if (passphrase == null) {
                    passphrase = settings.getServer(host).getPassphrase();
                }
            }
        }
    }
}
