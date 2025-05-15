import Commands.TagCommand;
import Objects.DTO.ResultDTO;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TagCommandTest {

    Path realMgit = Paths.get(".mgit");
    Path backupMgit = Paths.get(".mgit.backup");

    @BeforeEach
    void backupRealRepo() throws IOException {
        if (Files.exists(realMgit)) {
            if (Files.exists(backupMgit)) deleteRecursively(backupMgit);
            Files.move(realMgit, backupMgit);
        }
        setupTestRepo();
    }

    @AfterEach
    void restoreRealRepo() throws IOException {
        deleteRecursively(realMgit);
        if (Files.exists(backupMgit)) {
            Files.move(backupMgit, realMgit);
        }
    }

    @Test
    public void testCreateLightweightTag() throws Exception {
        String[] args = {"v-test-lightweight"};
        TagCommand tagCommand = new TagCommand(args);
        ResultDTO result = tagCommand.execute();

        assertTrue(result.isSuccess());

        Path tagFile = realMgit.resolve("refs").resolve("tags").resolve("v-test-lightweight");
        assertTrue(Files.exists(tagFile));

        String sha = Files.readString(tagFile).trim();
        assertEquals("1234567890abcdef", sha);
    }

    @Test
    public void testCreateAnnotatedTag() throws Exception {
        String[] args = {"-a", "v-test-annotated"};
        TagCommand tagCommand = new TagCommand(args);
        ResultDTO result = tagCommand.execute();

        assertTrue(result.isSuccess());

        Path tagRefPath = realMgit.resolve("refs").resolve("tags").resolve("v-test-annotated");
        assertTrue(Files.exists(tagRefPath));

        String tagSha = Files.readString(tagRefPath).trim();
        assertNotEquals("1234567890abcdef", tagSha);  // Annotated tag SHA should be different

        // Check object file exists
        Path tagObjectPath = findObjectPath(tagSha);
        assertNotNull(tagObjectPath);
        assertTrue(Files.exists(tagObjectPath));
    }

    @Test
    public void testListTags() throws Exception {
        // Setup: create dummy tag
        Path tagsDir = realMgit.resolve("refs").resolve("tags");
        Files.createDirectories(tagsDir);
        Path tagFile = tagsDir.resolve("v-list-me");
        Files.writeString(tagFile, "1234567890abcdef\n");

        String[] args = {};
        TagCommand tagCommand = new TagCommand(args);
        ResultDTO result = tagCommand.execute();

        assertTrue(result.isSuccess());
    }

    // ---------- Helpers ----------

    private void setupTestRepo() throws IOException {
        Path headPath = realMgit.resolve("HEAD");
        Path refsDir = realMgit.resolve("refs").resolve("heads");
        Path tagsDir = realMgit.resolve("refs").resolve("tags");
        Files.createDirectories(tagsDir);
        Files.createDirectories(refsDir);

        // HEAD points to master
        Files.writeString(headPath, "ref: refs/heads/master\n");

        // Write dummy SHA to master
        Path master = refsDir.resolve("master");
        Files.writeString(master, "1234567890abcdef\n");

        // Ensure objects directory exists
        Files.createDirectories(realMgit.resolve("objects"));
    }

    private void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path)
                .sorted((a, b) -> b.compareTo(a)) // delete children before parent
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        System.err.println("Failed to delete: " + p);
                    }
                });
    }

    private Path findObjectPath(String sha) {
        if (sha == null || sha.length() < 2) return null;

        String dir = sha.substring(0, 2);
        String file = sha.substring(2);
        Path objectPath = realMgit.resolve("objects").resolve(dir).resolve(file);
        return Files.exists(objectPath) ? objectPath : null;
    }
}
