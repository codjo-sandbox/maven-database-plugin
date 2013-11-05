/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database.scm;
import java.io.File;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
/**
 *
 */
public interface CheckOutConfig {
    ScmVersion getTag();


    File getCheckoutDirectory();


    boolean isSkipCheckoutIfExists();


    ScmRepository getScmRepository() throws ScmException;


    ScmManager getManager();
}
