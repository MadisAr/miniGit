package Objects;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MGitIndex {
    private MiniGitRepository repo;
    private List<MGitIndexEntry> entries;
    private int version;

    public MGitIndex(int version, List<MGitIndexEntry> entries, MiniGitRepository miniGitRepository) {
        this.repo = miniGitRepository;
        this.entries = entries;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void addEntry(MGitIndexEntry mGitIndexEntry) {
        entries.add(mGitIndexEntry);
    }

    public void removeEntry(String relativePath) {
        // asendame backslashid tavalistega sest git kasutab enda siseselt tavalisi slashe
        String normalized = relativePath.replace('\\', '/');
        entries.removeIf(entry -> entry.name().equals(normalized));
    }

    public List<MGitIndexEntry> getEntries() {
        return entries;
    }

    // funktsioon .git index failide lugemiseks, paris giti omade peal tootab
    public void read() throws IOException {
        File indexFile = repo.getGitDir().resolve("index").toFile();

        if (!indexFile.exists()) {
            entries = new ArrayList<>();
        }

        byte[] byteData = Files.readAllBytes(indexFile.toPath());

        ByteBuffer buf = ByteBuffer.wrap(byteData).order(ByteOrder.BIG_ENDIAN);

        int signature = buf.getInt();
        version = buf.getInt();
        int entryCount = buf.getInt();
        List<MGitIndexEntry> readEntries = new ArrayList<>();

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
            readEntries.add(mGitIndexEntry);
        }

        entries = readEntries;
    }

    public void write() {
        Path indexPath = repo.getGitDir().resolve("index");

        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
        ) {
            dos.write("DIRC".getBytes(StandardCharsets.US_ASCII));
            dos.writeInt(getVersion());
            dos.writeInt(getEntries().size());

            for (MGitIndexEntry entry : entries) {
                dos.writeInt(entry.cTimeSeconds());
                dos.writeInt(entry.cTimeNanoseconds());
                dos.writeInt(entry.mTimeSeconds());
                dos.writeInt(entry.mTimeNanoseconds());
                dos.writeInt(entry.dev());
                dos.writeInt(entry.ino());

                int mode = (entry.modeType() << 12) | entry.modePerms();
                dos.writeInt(mode);

                dos.writeInt(entry.uid());
                dos.writeInt(entry.gid());
                dos.writeInt(entry.fileSize());
                dos.write(entry.sha());

                int nameLength = Math.min(entry.name().getBytes(StandardCharsets.UTF_8).length, 0xFFF);

                dos.writeShort(entry.flags()); // ma pole kindel mida siin tegema peaks lol, arutab hiljem

                byte[] nameBytes = entry.name().getBytes(StandardCharsets.UTF_8);
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
}
