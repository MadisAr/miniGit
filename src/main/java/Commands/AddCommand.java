package Commands;

import Objects.DTO.ResultDTO;
import Objects.MGitIndex;
import Objects.MGitIndexEntry;
import Objects.MiniGitRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Stream;

public class AddCommand extends Command {
    public AddCommand(String[] args) {
        super(args);
    }

    @Override
    public ResultDTO execute() {
        // sama kood mis RmCommandis ma ei tea kas peaks tegema funktsiooni? aga kuhu??
        MiniGitRepository miniGitRepository = new MiniGitRepository(System.getProperty("user.dir"));

        String pathString = getArgs()[0];
        Path filePath = miniGitRepository.getRepoDir().resolve(pathString);

        // versioon peaks meil alati 2 olema
        MGitIndex mGitIndex = new MGitIndex(2, new ArrayList<>(), miniGitRepository);
        try {
            mGitIndex.read();
        } catch (Exception e) {
            return new ResultDTO(false, e.getMessage(), null);
        }

        System.out.println(mGitIndex.getEntries());
        if (!Files.isDirectory(filePath)) {
            try {
                updateFile(filePath, mGitIndex, miniGitRepository);
            } catch (IOException e) {
                return new ResultDTO(false, e.getMessage(), null);
            }
        } else {
            try (Stream<Path> stream = Files.walk(filePath)) {
                stream.filter(Files::isRegularFile).forEach(path -> {
                    try {
                        updateFile(path, mGitIndex, miniGitRepository);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                return new ResultDTO(false, e.getMessage(), null);
            }
        }


        mGitIndex.write();
        System.out.println(mGitIndex.getEntries());
        return new

                ResultDTO(true, "added file(s)", null);
    }


    /**
     * eemaldab antud mGitIndexEntry mGitIndeksist, lisab uuesti listi ja kirjutab
     *
     * @param filePath  antud faili path
     * @param mGitIndex muudetav mGitIndex
     */
    private void updateFile(Path filePath, MGitIndex mGitIndex, MiniGitRepository mGitRepo) throws IOException {
        // kui pole ignoreeritud failide hulgas siis eemaldame vana entry ja lisame uue
        if (!mGitRepo.isFileIgnored(filePath)) {
            MGitIndexEntry entry = createIndexEntry(filePath, mGitRepo.getRepoDir());
            mGitIndex.removeEntry(entry.name());
            mGitIndex.addEntry(entry);
        }
    }

    /**
     * Leiab antud pathist ja alg pathist vajalikud andmed
     * moned vaartused ei ole voibolla praegu taiesti tapsed aga meile loodetavasti piisab
     *
     * @param path        faili path
     * @param workDirPath work dir path ehk kaust kus sees on .mgit kaust
     * @return tagastab MGitIndexEntry
     * @throws IOException Files errorid
     */
    private MGitIndexEntry createIndexEntry(Path path, Path workDirPath) throws IOException {
        if (!Files.exists(path)) {
            throw new NoSuchFileException("File does not exist");
        }

        BasicFileAttributes basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
        FileTime cTime = basicFileAttributes.creationTime();
        FileTime mTime = basicFileAttributes.lastModifiedTime();
        int cTimeSeconds = (int) cTime.toInstant().getEpochSecond();
        int cTimeNano = (int) cTime.toInstant().getNano();
        int mTimeSeconds = (int) cTime.toInstant().getEpochSecond();
        int mTimeNano = (int) cTime.toInstant().getNano();

        // seame default vaartused, ma ei ole kindel kuidas dev ja ino leida
        int dev = 0, ino = 0, uid = 0, gid = 0, modeType = 0, modePerms = 0;

        try {
            PosixFileAttributes posixAttributes = Files.readAttributes(path, PosixFileAttributes.class);
            uid = posixAttributes.owner().getName().hashCode();
            gid = posixAttributes.group().getName().hashCode();
            modePerms = calculateUnixPermissions(posixAttributes.permissions());
        } catch (Exception e) {
            // windowsi korral
            uid = Files.getOwner(path).getName().hashCode();
            gid = -1;
            modePerms = calculateWinPermissions(path);
        }

        // giti tahised failityypidele hex koodis
        if (Files.isRegularFile(path)) {
            modeType = 0x8000;
        } else if (Files.isSymbolicLink(path)) {
            modeType = 0xA000;
        } else if (Files.isDirectory(path)) {
            modeType = 0x4000;
        }

        int fileSize = (int) basicFileAttributes.size();

        // leiame sha
        byte[] shaBytes;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            shaBytes = digest.digest(Files.readAllBytes(path));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // meil vist ei ole vaja flage panna
        short flags = 0;

        // teeme relative pathi globalist
        String name = workDirPath.relativize(path).toString().replace("\\", "/");
        return new MGitIndexEntry(cTimeSeconds, cTimeNano, mTimeSeconds, mTimeNano, dev, ino, modeType, modePerms, uid, gid, fileSize, shaBytes, flags, name);
    }

    /**
     * arvutab windowsi jaoks permissionid
     *
     * @param path antud faili path
     * @return tagastab int vaartuse permissionitest
     */
    private int calculateWinPermissions(Path path) {
        int mode = 0;

        if (Files.isWritable(path)) mode |= 0x100;
        if (Files.isReadable(path)) mode |= 0x80;
        if (Files.isExecutable(path)) mode |= 0x40;

        return mode;
    }

    /**
     * vaatab permissioneid ja lisab vastavad bitid mode intile
     * seda saab teha ainult unixi operatsioonisysteemide puhul
     *
     * @param perms set permissionitest
     * @return tagastab saadud int vaartuse permissionitest
     */
    private int calculateUnixPermissions(Set<PosixFilePermission> perms) {
        int mode = 0;

        if (perms.contains(PosixFilePermission.OWNER_READ)) mode |= 0x100;
        if (perms.contains(PosixFilePermission.OTHERS_WRITE)) mode |= 0x80;
        if (perms.contains(PosixFilePermission.OWNER_EXECUTE)) mode |= 0x40;

        if (perms.contains(PosixFilePermission.GROUP_READ)) mode |= 0x20;
        if (perms.contains(PosixFilePermission.GROUP_WRITE)) mode |= 0x10;
        if (perms.contains(PosixFilePermission.GROUP_EXECUTE)) mode |= 0x8;

        if (perms.contains(PosixFilePermission.OTHERS_READ)) mode |= 0x4;
        if (perms.contains(PosixFilePermission.OTHERS_WRITE)) mode |= 0x2;
        if (perms.contains(PosixFilePermission.OTHERS_EXECUTE)) mode |= 0x1;

        return mode;
    }


}
