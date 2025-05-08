package Objects;

import org.w3c.dom.traversal.TreeWalker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * Refid on gitis viited git objektidele
 */

// hetkel iga ref saab teoorias listida kogu .mgit refs kausta
// tegelt voiks kuidagi muudmoodi teha seda, TODO

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
        String data = new String(Files.readAllBytes(activePath));

        while (data.startsWith("ref: ")) {
            String newPath = data.split(" ", 2)[1];
            activePath = miniGitRepository.getGitDir().resolve(newPath);
            data = new String(Files.readAllBytes(activePath));
        }

        sha = data;
    }

    public Map<String, Object> listRefsRecursive(MiniGitRepository repo, File dir) {
        Map<String, Object> refs = new TreeMap<>();

        File[] children = dir.listFiles();
        if (children == null) return refs;

        for (File file : children) {
            if (file.isDirectory()) {
                refs.put(file.getName(), listRefsRecursive(repo, file));
            } else {
                try {
                    Ref ref = new Ref(repo, file.toPath());
                    ref.findSha();
                    refs.put(file.getName(), ref.getSha().trim());
                } catch (IOException e) {
                    System.out.println("Failed to resolve ref at " + file + ": " + e.getMessage());
                }
            }
        }
        return refs;
    }

    public Map<String, Object> listRefs(MiniGitRepository repo) {
        File refsDir = repo.getGitDir().resolve("refs").toFile();
        return listRefsRecursive(repo, refsDir);
    }
}
