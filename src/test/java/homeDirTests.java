import Commands.AddCommand;
import Commands.InitCommand;
import Commands.RmCommand;
import Commands.TagCommand;
import Objects.DTO.ResultDTO;
import Objects.MGitIndex;
import Objects.MGitIndexEntry;
import Objects.MiniGitRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// Panen siia veel teste mida on hea local .mgit kaustaga teha
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class homeDirTests {

    @TempDir
    static Path tempDir;

    Path realMgit = Paths.get(".mgit");
    Path backupMgit = Paths.get(".mgit.backup");
    private MiniGitRepository miniGitRepository;


    @BeforeAll
    void setupTestDirectory() throws IOException {
        miniGitRepository = new MiniGitRepository(tempDir.toString());

        // Clean up tempDir if it exists
        if (Files.exists(tempDir)) {
            try (Stream<Path> contents = Files.walk(tempDir)) {
                contents
                        .sorted(Comparator.reverseOrder())
                        .filter(path -> !path.equals(tempDir))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            }
        } else {
            Files.createDirectories(tempDir);
        }

        Path testFile = tempDir.resolve("testFile.txt");
        writeTestSentence(testFile);

        Path subDir1 = tempDir.resolve("folder1");
        Path subDir2 = tempDir.resolve("folder2");
        Files.createDirectories(subDir1);
        Files.createDirectories(subDir2);

        writeTestSentence(subDir1.resolve("file1.txt"));
        writeTestSentence(subDir1.resolve("file2.txt"));
        writeTestSentence(subDir2.resolve("file1.txt"));
        writeTestSentence(subDir2.resolve("file2.txt"));
    }

    private void writeTestSentence(Path file) throws IOException {
        String sentence = "testlause 123";
        Files.writeString(file, sentence + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

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

    //abifunktsioonid et enne addimist voi removimist jooksutada
    ResultDTO executeInit(String[] args) {
        InitCommand initCommand = new InitCommand(args, miniGitRepository);
        return initCommand.execute();
    }

    ResultDTO executeAdd(String[] args) throws IOException {
        executeInit(new String[]{"."});
        // kirjutan folder2 mgitignore'i et testida kas pariselt ignoreeritakse
        Files.write(miniGitRepository.getRepoDir().resolve(".mgitignore"), "\nfolder2\n".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        AddCommand add = new AddCommand(args, miniGitRepository);
        return add.execute();
    }

    @Test
    void testInit() throws IOException {
        executeInit(new String[]{"."});
        assert Files.exists(miniGitRepository.getGitDir());
        assert Files.exists(miniGitRepository.getRepoDir().resolve(".mgitignore"));
    }

    @Test
    void testAdd() throws IOException {
        String[] args = new String[]{"."};
        executeInit(args);
        executeAdd(args);

        MGitIndex mGitIndex = new MGitIndex(2, new ArrayList<>(), miniGitRepository);
        mGitIndex.read();

        boolean folder1file1 = false;
        boolean folder2file1 = false;
        for (MGitIndexEntry entry : mGitIndex.getEntries()) {
            if (entry.name().equals("folder1/file1.txt")) folder1file1 = true;
            else if (entry.name().equals("folder2/file1.txt")) folder2file1 = true;
        }

        assert folder1file1;
        assert !folder2file1;
    }

    @Test
    void testRemove() throws IOException {
        String[] args = {"."};
        executeInit(args);
        executeAdd(args);

        MGitIndex mGitIndex = new MGitIndex(2, new ArrayList<>(), miniGitRepository);
        RmCommand rm = new RmCommand(args, miniGitRepository);
        ResultDTO rs = rm.execute();
        mGitIndex.read();

        boolean folder1file1 = false;
        boolean folder2file1 = false;
        for (MGitIndexEntry entry : mGitIndex.getEntries()) {
            if (entry.name().equals("folder1/file1.txt")) folder1file1 = true;
            else if (entry.name().equals("folder2/file1.txt")) folder2file1 = true;
        }

        assert !folder1file1;
        assert !folder2file1;
    }

    @Test
    public void testCreateLightweightTag() throws Exception {
        String[] args = {"v-test-lightweight"};
        TagCommand tagCommand = new TagCommand(args, null);
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
        TagCommand tagCommand = new TagCommand(args, null);
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
        TagCommand tagCommand = new TagCommand(args, null);
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
