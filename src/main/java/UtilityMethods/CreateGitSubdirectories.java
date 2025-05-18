package UtilityMethods;

import Objects.MiniGitRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
     * Leiab parameetrina antud pathist repositooriumi .mgit directory
     *
     * @param path kaust millest liigume parent kaustadesse
     * @return uus repo objekt
     */
    public static MiniGitRepository repoFind(String path) {
        Path current = Paths.get(path).toAbsolutePath();

        while (current != null) {
            Path mgitDir = current.resolve(".mgit");
            if (Files.isDirectory(mgitDir)) {
                return new MiniGitRepository(current.toString());
            }
            current = current.getParent();
        }

        return null;

    }

    /**
     * teeb vastava nimega alamkausta
     *
     * @param DirFile kaust kuhu teha alamkaust
     * @param path    alamkaustad mida teha, voib anda mitu kausta siis tehakse yksteise sisse
     */
    public static void createGitSubdirectories(File DirFile, String... path) {
        File subFile = repoFile(DirFile.toPath(), path);
        if (!subFile.mkdirs()) throw new RuntimeException("Failed to create subdirectory at path: " + subFile);
    }

    public static File createGitDirsAndFile(File DirFile, String... paths) throws IOException {
        String[] dirs = Arrays.copyOfRange(paths, 0, paths.length - 1);
        File subFile = repoFile(DirFile.toPath(), dirs);
        subFile.mkdirs();
        //if (!subFile.mkdirs()) throw new RuntimeException("Failed to create subdirectory at path: " + subFile);
        File file = new File(subFile, paths[paths.length - 1]);
        file.createNewFile();
        return file;
    }
}
