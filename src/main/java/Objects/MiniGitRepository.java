package Objects;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MiniGitRepository {
    private final String workTree;
    private final Path gitDir;

    public String getWorkTree() {
        return workTree;
    }

    public Path getGitDir() {
        return gitDir;
    }

    public MiniGitRepository(String path) {
        this.workTree = path;
        this.gitDir = Paths.get(path).resolve(".mgit");
    }
}