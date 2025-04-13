package Objects;

import java.nio.file.Paths;

public class MiniGitRepository {
    private final String workTree;
    private final String gitDir;

    public String getWorkTree() {
        return workTree;
    }

    public String getGitDir() {
        return gitDir;
    }

    public MiniGitRepository(String path) {
        this.workTree = path;
        this.gitDir = Paths.get(path, ".mgit").toString();
    }

    public static void main(String[] args) {
        MiniGitRepository m =  new MiniGitRepository("\\c\\Users\\madismii\\UT\\oop\\miniGit");
    }
}