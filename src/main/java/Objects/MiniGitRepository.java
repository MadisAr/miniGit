package Objects;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MiniGitRepository {
    private final String workTree;
    private final Path gitDir;
    private final Path repoDir;
    private List<Path> ignoredFiles;

    public String getWorkTree() {
        return workTree;
    }

    public Path getGitDir() {
        return gitDir;
    }

    public Path getRepoDir() {
        return repoDir;
    }

    public MiniGitRepository(String path) {
        this.workTree = path;
        this.gitDir = Paths.get(path).resolve(".mgit");
        this.repoDir = gitDir.getParent();
        this.ignoredFiles = new ArrayList<>();
    }

    /**
     * Loeb koik failid mGitIgnore failist ja moodustab nendest listi failidest mida ignoreerida
     *
     * @throws IOException Files.readAllLines error
     */
    public void findIgnored() throws IOException {
        List<String> mGitIgnoreLines = Files.readAllLines(repoDir.resolve(".mgitignore"));

        for (String mGitIgnoreLine : mGitIgnoreLines) {
            if (!mGitIgnoreLine.isEmpty() && !(mGitIgnoreLine.charAt(0) == '#')) {
                ignoredFiles.add(repoDir.resolve(mGitIgnoreLine));
            }
        }
    }

    /**
     * TODO peaks midagi muud tegema kui faili ei eksisteeri yldse
     * votab filePathi ja vaatab kas ta on ignoreeritud failide listis
     * enne funktsiooni peab jooksutama vajadusel
     *
     * @param filePath vaadeldav fail
     * @return tagastab true kui fail on ignoreeritud ja false kui ei ole ignoreeritud
     */
    public boolean isFileIgnored(Path filePath) {
        if (!Files.exists(filePath)) throw new RuntimeException("File not found");

        Path fileParent = filePath;
        if (ignoredFiles.contains(fileParent)) return true;

        while ((fileParent = fileParent.getParent()) != null) {
            // kui oleme joudnud repo programmi juurkaustani tagastame false
            if (fileParent == repoDir) return false;

            // kui faili parent on ignoreeritud failide listis tagastame true
            if (ignoredFiles.contains(fileParent)) return true;
        }

        // kui parenteid rohkem pole oleme joudnud failitee algusesse jarelikult polnud fail meie repo kaustas
        // hetkel tagastan false aga vblla peaks errori viskama?
        return false;
    }

    public static void main(String[] args) throws IOException {
        String currentDir = System.getProperty("user.dir");
        Path currentDirPath = Paths.get(currentDir);
        MiniGitRepository miniGitRepository = new MiniGitRepository(currentDir);

        miniGitRepository.findIgnored();
        System.out.println(miniGitRepository.isFileIgnored(currentDirPath.resolve("pompdede.xml")));
        System.out.println(miniGitRepository.isFileIgnored(currentDirPath.resolve("README.md")));
        System.out.println(miniGitRepository.isFileIgnored(currentDirPath.resolve("testDir").resolve("READMEk.md")));
        System.out.println(miniGitRepository.isFileIgnored(currentDirPath.resolve("pom.xml")));
        System.out.println(miniGitRepository.isFileIgnored(currentDirPath.resolve("testDir")));
        System.out.println(miniGitRepository.isFileIgnored(currentDirPath.resolve("testDir").resolve("README.md")));
    }
}