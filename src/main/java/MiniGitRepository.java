import java.nio.file.Path;
import java.nio.file.Paths;

public class MiniGitRepository {
    private final Path repoPath;

    public MiniGitRepository(String path) {
        this.repoPath = Paths.get(path);
    }
}