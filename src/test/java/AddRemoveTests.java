import Commands.AddCommand;
import Commands.InitCommand;
import Commands.RmCommand;
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

//TODO vblla liigutada ara originaalsesse Tests kausta
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddRemoveTests {

    @TempDir
    static Path tempDir;
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
}
