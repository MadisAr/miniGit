package Objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Ref {
    private MiniGitRepository miniGitRepository;
    private String sha;
    private Path path;

    public Ref(MiniGitRepository miniGitRepository, String path) {
        this.miniGitRepository = miniGitRepository;
        this.path = Paths.get(path);
    }

    private void findSha() throws IOException {
        Path activePath = path;
        String data = String.valueOf(Files.readAllLines(activePath));

        while (data.startsWith("ref: ")) {
            String newPath = data.split(" ", 2)[1];
            activePath = Paths.get(miniGitRepository.getGitDir()).resolve(newPath);
            data = String.valueOf(Files.readAllLines(activePath));
        }

        sha = data;
    }
}
