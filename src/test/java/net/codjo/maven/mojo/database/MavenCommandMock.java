package net.codjo.maven.mojo.database;
import net.codjo.maven.common.embedder.MavenCommand;
import net.codjo.test.common.LogString;
import java.io.File;
import org.apache.maven.plugin.logging.Log;
/**
 *
 */
public class MavenCommandMock extends MavenCommand {

    private LogString log;


    public MavenCommandMock(LogString log) {
        this.log = log;
    }


    public LogString getLog() {
        return log;
    }


    public void setLog(LogString log) {
        this.log = log;
    }


    public void execute(String[] goals, File targetDirectory, Log logger, boolean reactorMode)
          throws Exception {
        for (int i = 0; i < goals.length; i++) {
            this.log.call("maven.execute", goals[i], targetDirectory, getProperties());
        }
    }
}