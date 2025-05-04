package Objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Refid on gitis viited git objektidele
 */
public class Ref {
    private MiniGitRepository miniGitRepository;
    private String sha;
    private Path path;

    public Ref(MiniGitRepository miniGitRepository, Path path) {
        this.miniGitRepository = miniGitRepository;
        this.path = path;
    }

    public String getSha() {
        return sha;
    }

    public Path getPath() {
        return path;
    }

    /**
     * kui antud ref on viide mingile objektile otsib kuni leiab mitte viid objektile
     *
     * @throws IOException readAllBytes error
     */
    public void findSha() throws IOException {
        Path activePath = path.resolve("");
        String data = String.valueOf(new String(Files.readAllBytes(activePath)));

        while (data.startsWith("ref: ")) {
            String newPath = data.split(" ", 2)[1];
            activePath = miniGitRepository.getGitDir().resolve(newPath);
            data = new String(Files.readAllBytes(activePath));
        }

        sha = data;
    }
}
