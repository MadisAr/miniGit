package Commands;

import Objects.DTO.ResultDTO;
import Objects.DTO.TreeDTO;
import Objects.MGitIndex;
import Objects.MGitIndexEntry;
import Objects.MGitObjects.TreeObject;
import Objects.MiniGitRepository;
import UtilityMethods.CreateGitSubdirectories;
import UtilityMethods.ReadObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class StatusCommand extends Command {
    public StatusCommand(String[] args, MiniGitRepository miniGitRepository) {
        super(args, miniGitRepository);
    }

    @Override
    public ResultDTO execute() {
        String result = statusCommand();

        if (result == null) {
            return new ResultDTO(true, "Status shown succesfully", null);
        }
        else {
            return new ResultDTO(false, result, null);
        }
    }

    private static String statusCommand() {
        MiniGitRepository miniGitRepository = new MiniGitRepository(System.getProperty("user.dir"));

        // versioon peaks meil alati 2 olema
        MGitIndex mGitIndex = new MGitIndex(2, new ArrayList<>(), miniGitRepository);
        try {
            mGitIndex.read();
        } catch (IOException e) {
            return "Error in reading Index:\n" + e.getMessage();
        }

        // display active branch status
        try {
            displayBranchStatus(miniGitRepository);
        } catch (IOException e) {
            return "Error in displaying branch status:\n" + e.getMessage();
        }

        // changes to be commited
        try {
            displayChangesToBeCommitted(miniGitRepository, mGitIndex);
        } catch (IOException e) {
            return "Error in displaying changes to be committed:\n" + e.getMessage();
        }
        System.out.println();

        // unstaged changes and untracked files
        try {
            displayUnstagedAndUntracked(miniGitRepository, mGitIndex);
        } catch (IOException e) {
            return "Error in displaying unstanged changes and untracked files:\n" + e.getMessage();
        }
        return null;
    }

    private static void displayUnstagedAndUntracked(MiniGitRepository miniGitRepository, MGitIndex mGitIndex) throws IOException {
        System.out.println("Changes not staged for commit:");

        String gitDirPrefix = miniGitRepository.getGitDir().toString() + File.separator;

        List<Path> allFiles = new ArrayList<>();

        Files.walkFileTree(Paths.get(miniGitRepository.getWorkTree()), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path currentPath, BasicFileAttributes attrs) {
                // Skip kui .mgitignore sees
                if (miniGitRepository.isFileIgnored(currentPath) || currentPath.toString().startsWith(gitDirPrefix)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path visitedPath, BasicFileAttributes attrs) {
                if (Files.isRegularFile(visitedPath)) {
                    // relatiivseks
                    Path relativePath = Paths.get(miniGitRepository.getWorkTree()).relativize(visitedPath);
                    allFiles.add(relativePath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // skipi kui permissione pole
                return FileVisitResult.CONTINUE;
            }

        });

        for (MGitIndexEntry entry : mGitIndex.getEntries()) {
            Path fullPath = Paths.get(miniGitRepository.getWorkTree()).resolve(Paths.get(entry.name()));

            if (!Files.exists(fullPath)) {
                System.out.println("  deleted: " + entry.name());
            } else {

                BasicFileAttributes basicFileAttributes = Files.readAttributes(fullPath, BasicFileAttributes.class);
                FileTime cTime = basicFileAttributes.creationTime();
                FileTime mTime = basicFileAttributes.lastModifiedTime();
                int cTimeSeconds = (int) cTime.toInstant().getEpochSecond();
                int cTimeNano = (int) cTime.toInstant().getNano();
                int mTimeSeconds = (int) mTime.toInstant().getEpochSecond();
                int mTimeNano = (int) mTime.toInstant().getNano();

                if (
                cTimeNano != entry.cTimeNanoseconds() ||
                cTimeSeconds != entry.cTimeNanoseconds() ||
                mTimeNano != entry.mTimeNanoseconds() ||
                mTimeSeconds != entry.mTimeSeconds()) {

                    byte[] sha;
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-1");
                        sha = digest.digest(Files.readAllBytes(fullPath));
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }

                    if (sha != entry.sha()) {
                        System.out.println("  modified:" + entry.name());
                    }
                }

            }
            if (allFiles.contains(entry.name())) {
                allFiles.remove(entry.name());
            }
        }
        System.out.println();
        System.out.println("Untracked files:");

        for (Path file : allFiles) {
            if (!miniGitRepository.isFileIgnored(file)) {
                System.out.println(file.toString());
            }
        }
    }

    private static void displayChangesToBeCommitted(MiniGitRepository miniGitRepository, MGitIndex mGitIndex) throws IOException {
        System.out.println("Changes to be committed:");

        Map<String, Object> flatDictionary = treeToDictionary(miniGitRepository, "HEAD", "");

        for (MGitIndexEntry entry : mGitIndex.getEntries()) {
            if (flatDictionary.containsKey(entry.name())) {
                if (!flatDictionary.get(entry.name()).equals(entry.sha())) { // kui sha erinev, siis on fail muudetud
                    System.out.println("  modified:" + entry.name());
                }
                flatDictionary.remove(entry.name());
            } else {
                System.out.println("  added:   " + entry.name()); // kui pole olemas siis lisatud
            }
        }

        for (MGitIndexEntry entry : mGitIndex.getEntries()) {
            System.out.println("  deleted: " + entry.name()); // kui ikka dictis, siis ei eksisteeri enam (sest eemaldasime kõik matchid)
        }
    }

    private static Map<String, Object> treeToDictionary(MiniGitRepository miniGitRepository, String reference, String prefix) throws IOException {
        Map<String, Object> flatDictionary = new HashMap<>();
        String treeSHA = miniGitRepository.findObject(reference, "tree");
        TreeObject tree = (TreeObject) ReadObject.readObject(miniGitRepository, treeSHA);

        for (TreeDTO leaf : tree.getContent()) { // if tree, then recurse
            String fullPath = Paths.get(prefix).resolve(Paths.get(Arrays.toString(leaf.path()))).toString();
            if (Arrays.equals(leaf.mode(), "040000".getBytes())) {
                flatDictionary.putAll(treeToDictionary(miniGitRepository, leaf.sha(), fullPath)); // vist oige
            } else { // if file then save as (fullpath : sha)
                flatDictionary.put(fullPath, leaf.sha());
            }
        }
        return flatDictionary;
    }

    private static void displayBranchStatus(MiniGitRepository miniGitRepository) throws IOException {
        String activeBranch = miniGitRepository.getActiveBranch();

        if (activeBranch != null) {
            System.out.println(String.format("On branch %", activeBranch));
        } else {
            System.out.println(String.format("HEAD detached at %", CreateGitSubdirectories.repoFind("HEAD")));
        }
    }

}

