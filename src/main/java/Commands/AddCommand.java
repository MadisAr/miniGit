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

public class AddCommand extends Command {
    public AddCommand(String[] args) {
        super(args);
    }

    // TODO siin tegelikult peaks arvestama ka ignoreeritud failidega
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
                MGitIndexEntry entry = createIndexEntry(filePath, miniGitRepository.getRepoDir());
                mGitIndex.addEntry(entry);
            } catch (IOException e) {
                return new ResultDTO(false, e.getMessage(), null);
            }
        } else {
            // polnud praegu aega lopuni teha
            // konnib kausta labi ja teeb mida vaja
        }


        mGitIndex.write();
        System.out.println(mGitIndex.getEntries());
        return new ResultDTO(true, "added file(s)", null);
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

        int dev = 0, ino = 0, uid = 0, gid = 0, modeType = 0, modePerms = 0;

        // TODO Siin on viga sest apparently windowsis ei toota PosixFileAttributes nii et peab midagi muud kasutama
        PosixFileAttributes posixA = Files.readAttributes(path, PosixFileAttributes.class);
        uid = posixA.owner().getName().hashCode();
        gid = posixA.group().getName().hashCode();
        modePerms = calculatePermissions(posixA.permissions());

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
        String name = workDirPath.relativize(path).toString();
        return new MGitIndexEntry(cTimeSeconds, cTimeNano, mTimeSeconds, mTimeNano, dev, ino, modeType, modePerms, uid, gid, fileSize, shaBytes, flags, name);
    }

    /**
     * vaatab permissioneid ja lisab vastavad bitid mode intile
     *
     * @param perms set permissionitest
     * @return tagastab saadud int vaartuse permissionitest
     */
    private int calculatePermissions(Set<PosixFilePermission> perms) {
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

    /**
     * eemaldab antud mGitIndexEntry mGitIndeksist, lisab uuesti listi ja kirjutab
     *
     * @param mGitIndexEntry kirjutatav entry
     * @param mGitIndex      muudetav mGitIndex
     */
    private void updateFile(MGitIndexEntry mGitIndexEntry, MGitIndex mGitIndex) {
        mGitIndex.removeEntry(mGitIndexEntry.name());
        mGitIndex.addEntry(mGitIndexEntry);
        mGitIndex.write();
    }
}
