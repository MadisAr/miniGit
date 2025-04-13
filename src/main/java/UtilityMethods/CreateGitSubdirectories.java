package UtilityMethods;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


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
     * @param DirFile kaust kuhu teha kaust
     * @param path       alamkaustad mida teha, voib anda mitu kausta siis tehakse yksteise sisse
     */
    public static void createGitSubdirectories(File DirFile, String... path) {
        File subFile = repoFile(DirFile.toPath(), path);
        if (!subFile.mkdirs()) throw new RuntimeException("Failed to crea`te subdirectory at path: " + subFile);
    }
}
