/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
/**
 *
 */
public class MavenProjectMock extends MavenProject {
    public MavenProjectMock() {
        super(new Model());
        MockUtil.singleton.setProject(this);

        setPluginArtifacts(new HashSet());
        setReportArtifacts(new HashSet());
        setExtensionArtifacts(new HashSet());
        setPluginArtifactRepositories(new ArrayList());
        getBuild().setDirectory(MockUtil.singleton.getTargetDir().getAbsolutePath());
    }

    public void addPluginDependency(Dependency dependency) {
        if (getDependencyManagement() == null) {
            getModel().setDependencyManagement(new DependencyManagement());
        }
        Plugin plugin = new Plugin();
        plugin.setGroupId("test");
        plugin.setVersion("1.0");
        plugin.setArtifactId("test");
        plugin.addDependency(dependency);
        getBuildPlugins().add(plugin);
    }
}
