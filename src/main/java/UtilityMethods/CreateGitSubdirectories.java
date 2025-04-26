package UtilityMethods;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


public class CreateGitSubdirectories {
    /**
     * Teeb antud pathist ja lisatud pathidest kokku yhe pathi
     *
     * @param GitDirPath Juurpath
     * @param path       loodavad kaustad
     * @return Fail objekt loodud pathist
     */
    public static File repoFile(Path GitDirPath, String... path) {
        return GitDirPath.resolve(Paths.get("", path)).toFile();
    }

    /**
     * teeb vastava nimega alamkausta
     *
     * @param DirFile kaust kuhu teha alamkaust
     * @param path       alamkaustad mida teha, voib anda mitu kausta siis tehakse yksteise sisse
     */
    public static void createGitSubdirectories(File DirFile, String... path) {
        File subFile = repoFile(DirFile.toPath(), path);
        if (!subFile.mkdirs()) throw new RuntimeException("Failed to create subdirectory at path: " + subFile);
    }

    public static File createGitDirsAndFile(File DirFile, String... paths) throws IOException {
        String[] dirs = Arrays.copyOfRange(paths, 0, paths.length-1);
        File subFile = repoFile(DirFile.toPath(), dirs);
        if (!subFile.mkdirs()) throw new RuntimeException("Failed to create subdirectory at path: " + subFile);
        File file = new File(subFile, paths[paths.length-1]);
        file.createNewFile();
        return file;
    }
}
