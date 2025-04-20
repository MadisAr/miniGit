import Objects.MGitObject;
import Objects.MiniGitRepository;
import UtilityMethods.ReadObject;
import UtilityMethods.WriteObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
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

        MGitObject result = ReadObject.ReadObject(repo, testSha);

        // vaatame et sisud oleksid samavaarsed ja et pikkus oleks ka sama
        assert result.getContent().equals(content);
        assert Integer.parseInt(result.getSize()) == content.length();
    }

    @Test
    void testWriteObject() throws IOException, NoSuchAlgorithmException {
        String testData = "TestDataString";
//        File gitDir = new File(repoDir, ".mgit");

        MiniGitRepository mockRepo = Mockito.mock(MiniGitRepository.class);
        MGitObject mockMGitObject = Mockito.mock(MGitObject.class);

        when(mockMGitObject.serialize(mockRepo)).thenReturn(testData);
        when(mockMGitObject.getFormat()).thenReturn("Commit");
        when(mockRepo.getGitDir()).thenReturn(tempDir.toString());

        String sha = writeObject(mockRepo, mockMGitObject);
        MGitObject mgitObject = ReadObject.ReadObject(mockRepo, sha);

        assert mgitObject.getContent().equals(testData);
    }
}
