package Objects;

import UtilityMethods.CreateGitSubdirectories;
import org.mockito.Mockito;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static org.mockito.Mockito.when;

public class MGitIndex {
    List<MGitIndexEntry> entries;
    int version;

    public MGitIndex(int version, List<MGitIndexEntry> entries) {
        this.entries = entries;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void addEntry(MGitIndexEntry mGitIndexEntry) {
        entries.add(mGitIndexEntry);
    }

    // TODO kas peaks tegema u relativePath.replace('\', '/') et oleks koikidel nimedel samad separatorid?
    public void removeEntry(String relativePath) {
        entries.removeIf(entry -> entry.name.equals(relativePath));
    }

    public List<MGitIndexEntry> getEntries() {
        return entries;
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

    public void write(MiniGitRepository repo) {
        Path indexPath = repo.getGitDir().resolve("index");

        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
        ) {
            dos.write("DIRC".getBytes(StandardCharsets.US_ASCII));
            dos.writeInt(getVersion());
            dos.writeInt(getEntries().size());

            for (MGitIndexEntry entry : entries) {
                dos.writeInt(entry.cTimeSeconds);
                dos.writeInt(entry.cTimeNanoseconds);
                dos.writeInt(entry.mTimeSeconds);
                dos.writeInt(entry.mTimeNanoseconds);
                dos.writeInt(entry.dev);
                dos.writeInt(entry.ino);

                int mode = (entry.modeType << 12) | entry.modePerms;
                dos.writeInt(mode);

                dos.writeInt(entry.uid);
                dos.writeInt(entry.gid);
                dos.writeInt(entry.fileSize);
                dos.write(entry.sha);

                int nameLength = Math.min(entry.name.getBytes(StandardCharsets.UTF_8).length, 0xFFF);

                dos.writeShort(entry.flags); // ma pole kindel mida siin tegema peaks lol, arutab hiljem

                byte[] nameBytes = entry.name.getBytes(StandardCharsets.UTF_8);
                dos.write(nameBytes);
                dos.writeByte(0);

                int entryLen = 62 + nameBytes.length + 1;
                int padding = (8 - (entryLen % 8)) % 8;
                dos.write(new byte[padding]);
            }

            try (OutputStream out = Files.newOutputStream(indexPath)) {
                baos.writeTo(out);
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // ajutine
        }
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
