//import Objects.MGitObjects.CommitObject;

import Objects.MGitObjects.CommitObject;
import Objects.MGitObjects.MGitObject;
import Objects.MiniGitRepository;
import Objects.DTO.TreeDTO;
import Objects.Ref;
import UtilityMethods.FindFirstChar;
import UtilityMethods.KvlmParse;
import UtilityMethods.ParseTree;
import UtilityMethods.ReadObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

import static UtilityMethods.WriteObject.writeObject;
import static org.mockito.Mockito.when;


class Tests {

    @TempDir
    Path tempDir;

    @Test
    void testReadObject() throws IOException {
        // teeme ajutise directory testimiseks
        File repoDir = tempDir.toFile();
        File gitDir = new File(repoDir, ".mgit");
        gitDir.mkdir();

        MiniGitRepository repo = new MiniGitRepository(repoDir.getAbsolutePath());

        String testSha = "abcdef1234567890abcdef1234567890abcdef12";
        String format = "blob";
        String content = "test content";

        File objectsDir = new File(gitDir, "objects");
        objectsDir.mkdir();
        File shaDir = new File(objectsDir, testSha.substring(0, 2));
        shaDir.mkdir();
        File shaFile = new File(shaDir, testSha.substring(2));

        ByteArrayOutputStream rawContent = new ByteArrayOutputStream();
        rawContent.write(format.getBytes(StandardCharsets.US_ASCII));
        rawContent.write(' ');
        rawContent.write(Integer.toString(content.length()).getBytes(StandardCharsets.US_ASCII));
        rawContent.write(0);
        rawContent.write(content.getBytes(StandardCharsets.US_ASCII));

        // kompressime sisu ja kirjutame faili, mis laheb JUniti Temp kausta
        try (FileOutputStream fos = new FileOutputStream(shaFile);
             DeflaterOutputStream dos = new DeflaterOutputStream(fos)) {
            dos.write(rawContent.toByteArray());
        }

        MGitObject result = ReadObject.readObject(repo, testSha);

        // vaatame et sisud oleksid samavaarsed ja et pikkus oleks ka sama
        assert result.getContent().equals(content);
        assert result.getSize() == content.length();
    }

    @Test
    void testWriteObject() throws IOException, NoSuchAlgorithmException {
        String testData = "TestDataString";
//        File gitDir = new File(repoDir, ".mgit");

        MiniGitRepository mockRepo = Mockito.mock(MiniGitRepository.class);
        MGitObject mockMGitObject = Mockito.mock(MGitObject.class);

        when(mockMGitObject.serialize(mockRepo)).thenReturn(testData);
        when(mockMGitObject.getFormat()).thenReturn("blob");
        when(mockRepo.getGitDir()).thenReturn(tempDir.toString());

        String sha = writeObject(mockRepo, mockMGitObject);
        MGitObject mgitObject = ReadObject.readObject(mockRepo, sha);

        assert mgitObject.getContent().equals(testData);
    }

    @Test
    void testKvlmParseUnparse() {
        String gitCommitMessage = "213 tree 29ff16c9c14e2652b22f8b78bb08a5a07930c147\n" +
                "parent 206941306e8a8af65b66eaaaea388a7ae24d49a0\n" +
                "author Maizi Pulgad <maizipulgad@thb.lt> 1527025023 +0200\n" +
                "committer Maizi Pulgad <maizipulgad@thb.lt> 1527025044 +0200\n" +
                "\n" +
                "Test commit sonum";

        Map<String, String> vals = KvlmParse.KvlmParse(gitCommitMessage.getBytes(StandardCharsets.UTF_8));
        String unParsed = new String(KvlmParse.KvlmUnParse(vals), StandardCharsets.UTF_8);

        assert vals.get("message").equals("Test commit sonum");
        assert (gitCommitMessage).equals("213 " +unParsed);
    }

    @Test
    void testCommitObject() {
        String gitCommitMessage = "213 tree 29ff16c9c14e2652b22f8b78bb08a5a07930c147\n" +
                "parent 206941306e8a8af65b66eaaaea388a7ae24d49a0\n" +
                "author Maizi Pulgad <maizipulgad@thb.lt> 1527025023 +0200\n" +
                "committer Maizi Pulgad <maizipulgad@thb.lt> 1527025044 +0200\n" +
                "\n" +
                "Test commit sonum";
        CommitObject commitObject = new CommitObject(gitCommitMessage.getBytes());

        System.out.println("test");
        String x = new String(KvlmParse.KvlmUnParse(commitObject.getContent()));
        System.out.println("tete");
        assert commitObject.getContent().get("message").equals("Test commit sonum");
        assert commitObject.getSize() == "Test commit sonum".length();
        assert commitObject.getFormat().equals("commit");
    }

    @Test
    void TestParse() throws IOException, URISyntaxException {
        Path p = Paths.get(getClass().getClassLoader()
                .getResource("71/f40b7bafe4d0bb87c752dd52f9c47db21f56a6")
                .toURI());
        byte[] bytes = Files.readAllBytes(p);
        bytes = ReadObject.decompress(bytes);
        bytes = Arrays.copyOfRange(bytes, FindFirstChar.findFirstChar(bytes, (byte) 0, 0) + 1, bytes.length);
        List<TreeDTO> info = ParseTree.parseTree(bytes);
        assert Arrays.equals(info.getFirst().mode(), "100644".getBytes());
        assert Arrays.equals(info.getFirst().path(), "ArgParser.java".getBytes());
    }

    @Test
    void TestRef() {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        MiniGitRepository mockRepo = Mockito.mock(MiniGitRepository.class);
        when(mockRepo.getGitDir()).thenReturn(currentDir.resolve(".mgit").toString());
//        Ref ref = new Ref(mockRepo, mockRepo.getGitDir().re
    }
}
