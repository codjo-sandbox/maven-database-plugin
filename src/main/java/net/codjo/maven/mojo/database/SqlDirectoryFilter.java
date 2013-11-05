/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import net.codjo.database.api.Engine;
/**
 *
 */
public class SqlDirectoryFilter implements FileFilter, DatabaseConstants {
    private List badEngineList = new ArrayList();


    public boolean accept(File file) {
        if ((file.isDirectory())) {
            return !("alter".equals(file.getName()) || "drop".equals(file.getName()));
        }
        else {
            String name = file.getName().toUpperCase();

            if (!(name.endsWith(".TAB") || name.endsWith(".TXT") || name.endsWith(".SQL"))) {
                return false;
            }

            if (badEngineList.isEmpty()) {
                return true;
            }

            String[] split = name.split("\\.");
            String nameWithoutExtension = split[0];
            for (int i = 0; i < badEngineList.size(); i++) {
                Engine badEngine = (Engine)badEngineList.get(i);
                if (nameWithoutExtension.endsWith("_" + badEngine.toString().toUpperCase())) {
                    return false;
                }
            }

            return true;
        }
    }


    public void setEngine(String engine) {
        Engine theEngine = Engine.toEngine(engine);
        if (engine != null) {
            Engine[] values = Engine.values();
            for (int i = 0; i < values.length; i++) {
                Engine value = values[i];
                if (!theEngine.equals(value)) {
                    badEngineList.add(value);
                }
            }
        }
    }
}
