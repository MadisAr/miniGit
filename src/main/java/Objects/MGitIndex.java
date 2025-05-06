package Objects;

import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class MGitIndex {
    List<MGitIndexEntry> entries;
    int version;

    public MGitIndex(int version, List<MGitIndexEntry> entries) {
        this.entries = entries;
        this.version = version;
    }

    // funktsioon .git index failide lugemiseks, paris giti omade peal tootab
    public static MGitIndex read(MiniGitRepository repo) throws IOException {
        File indexFile = repo.getGitDir().resolve("index").toFile();

        if (!indexFile.exists()) {
            return new MGitIndex(2, new ArrayList<>());
        }

        byte[] byteData = Files.readAllBytes(indexFile.toPath());

        ByteBuffer buf = ByteBuffer.wrap(byteData).order(ByteOrder.BIG_ENDIAN);

        int signature = buf.getInt();
        int version = buf.getInt();
        int entryCount = buf.getInt();
        List<MGitIndexEntry> entries = new ArrayList<>();

        for (int i = 0; i < entryCount; i++) {
            // alguspositsioon hiljemaks bufferi pikkuse arvutamiseks
            int startPos = buf.position();

            // votame jarjest index faili sisu
            int cTime = buf.getInt();
            int cTimeNs = buf.getInt();
            int mTime = buf.getInt();
            int mTimeNs = buf.getInt();
            int dev = buf.getInt();
            int ino = buf.getInt();
            int mode = buf.getInt();
            int uid = buf.getInt();
            int gid = buf.getInt();
            int fileSize = buf.getInt();
            byte[] sha = new byte[20];
            buf.get(sha);
            short flags = buf.getShort();

            // koostame failinime byte'idest kuni jouame nullbytini
            StringBuilder sb = new StringBuilder();
            byte b;
            while ((b = buf.get()) != 0) {
                sb.append((char) b);
            }
            String name = sb.toString();

            // arvutame positsiooni peale paddingut
            int padding = (8 - ((buf.position() - startPos) % 8)) % 8;
            if (padding > 0 && buf.hasRemaining()) {
                int padPos = buf.position();
                buf.position(padPos + padding);
            }

            MGitIndexEntry mGitIndexEntry = new MGitIndexEntry(cTime, cTimeNs, mTime, mTimeNs, dev, ino, mode >> 12, mode & 0xFFF, uid, gid, fileSize, sha, flags, name);
            entries.add(mGitIndexEntry);
        }

        return new MGitIndex(version, entries);
    }

// ajutine testimiseks, parast teen testi sellele
//    public static void main(String[] args) throws IOException {
//        MiniGitRepository miniGitRepository = Mockito.mock(MiniGitRepository.class);
//        when(miniGitRepository.getGitDir()).thenReturn(Paths.get(""));
//        byte[] b = Files.readAllBytes(miniGitRepository.getGitDir().resolve("index"));
//        MGitIndex mGitIndex = read(miniGitRepository);
//        System.out.println("tada");
//    }
}
