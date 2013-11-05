package net.codjo.maven.mojo.database.util;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.codjo.database.common.api.ExecSqlScript;
import net.codjo.maven.mojo.database.DatabaseConstants;
import net.codjo.maven.mojo.database.SqlDirectoryFilter;
import net.codjo.util.file.FileUtil;
public class ScriptUtil implements DatabaseConstants {

    private ScriptUtil() {
    }


    public static String computeCommonDir(String baseDir, String baseDirGenerated) {
        int lastCommonIndex = -1;
        int minLength = Math.min(baseDir.length(), baseDirGenerated.length());
        int index = 0;
        while (index < minLength) {
            if (baseDir.charAt(index) != baseDirGenerated.charAt(index)) {
                break;
            }
            if (new File(baseDir.substring(0, index)).exists()) {
                lastCommonIndex = index;
            }
            index++;
        }
        if (index == minLength && new File(baseDir.substring(0, minLength)).exists()) {
            lastCommonIndex = minLength;
        }
        return baseDir.substring(0, lastCommonIndex);
    }


    public static void executeSqlCommands(List sqlFileSets,
                                          SqlDirectoryFilter directoryFilter,
                                          String workingDir,
                                          ExecSqlScript execSqlScript) {
        List scripts = new ArrayList();
        String[] myList = new String[]{DIR_MESSAGE, DIR_TABLE, DIR_VIEW, DIR_GAP, DIR_INDEX, DIR_CONSTRAINT,
                                       DIR_RULE, DIR_TRIGGER, DIR_PERMISSION};
        if (sqlFileSets != null) {
            for (int pos = 0; pos < myList.length; pos++) {
                for (Iterator i = sqlFileSets.iterator(); i.hasNext(); ) {
                    SqlDirectory sqlDirectory = (SqlDirectory)i.next();
                    String baseDirectory = sqlDirectory.getDirectory().getPath();
                    initialiseIncludeCommands(scripts,
                                              myList[pos],
                                              baseDirectory,
                                              directoryFilter,
                                              workingDir);
                    if (DIR_RULE.equals(myList[pos])) {
                        for (Iterator j = sqlDirectory.getCommandFiles().iterator(); j.hasNext(); ) {
                            File commandOrderFile = (File)j.next();
                            initialiseIncludeCommandsForStoredProcedures(scripts,
                                                                         commandOrderFile,
                                                                         baseDirectory,
                                                                         workingDir);
                        }
                    }
                }
            }
        }

        execSqlScript.execute(new File(workingDir).getAbsolutePath(), scripts);
    }


    public static String createRelativePath(String relativePath, String databaseScript) {
        return relativePath + File.separator + databaseScript;
    }


    public static File[] collectFiles(File sqlFile, FileFilter filter) {
        List files = new ArrayList();
        collectFiles(files, sqlFile, filter);
        return (File[])files.toArray(new File[files.size()]);
    }


    private static void initialiseIncludeCommands(List scripts,
                                                  String currentDirectory,
                                                  String baseDirectory,
                                                  SqlDirectoryFilter sqlFilter,
                                                  String workDir) {
        File sqlFile = new File(createRelativePath(baseDirectory, currentDirectory));
        addFilesToMap(orderCommands(collectFiles(sqlFile, sqlFilter)), scripts, workDir);
    }


    private static void initialiseIncludeCommandsForStoredProcedures(List scripts,
                                                                     File procedureOrderFile,
                                                                     String baseDirectory,
                                                                     String workDir) {
        if (!procedureOrderFile.exists()) {
            return;
        }

        String[] procedures = FileUtil.loadContentAsLines(procedureOrderFile);
        for (int i = 0; i < procedures.length; i++) {
            String procedure = procedures[i];
            scripts.add(toRelativePath(new File(baseDirectory, procedure).getPath(), workDir));
        }
    }


    private static void addFilesToMap(File[] files, List scripts, String workDir) {
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isFile()) {
                    scripts.add(toRelativePath(file.getPath(), workDir));
                }
            }
        }
    }


    private static String toRelativePath(String filePath, String workDir) {
        return "./" + filePath.substring(workDir.length());
    }


    private static File[] orderCommands(File[] files) {
        if (files != null) {
            List result = Arrays.asList(files);
            Collections.sort(result, new CommandFileComparator());
            return (File[])result.toArray();
        }
        else {
            return files;
        }
    }


    private static void collectFiles(List sqlFiles, File file, FileFilter filter) {
        if (file.isFile()) {
            sqlFiles.add(file);
            return;
        }
        if (".svn".equals(file.getName())) {
            return;
        }
        File[] files = file.listFiles(filter);
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            collectFiles(sqlFiles, files[i], filter);
        }
    }


    private static class CommandFileComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            File file1 = (File)o1;
            File file2 = (File)o2;

            if (is(file1, "alter") || is(file1, "grant")) {
                return 1;
            }
            if (is(file2, "alter") || is(file2, "grant")) {
                return -1;
            }

            if (isDrop(file1)) {
                return 1;
            }

            if (isDrop(file2)) {
                return -1;
            }

            if (isTable(file1)) {
                return -1;
            }

            if (isTable(file2)) {
                return 1;
            }

            return file1.getName().compareTo(file2.getName());
        }


        private boolean isTable(File file1) {
            return file1.getName().endsWith(".tab");
        }


        private boolean is(File file, String type) {
            return file.getName().indexOf(type) >= 0;
        }


        private boolean isDrop(File file) {
            return file.getName().startsWith("drop");
        }
    }
}
