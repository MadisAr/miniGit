package Commands;

import Objects.DTO.ResultDTO;
import Objects.DTO.TreeDTO;
import Objects.MGitIndex;
import Objects.MGitIndexEntry;
import Objects.MGitObjects.CommitObject;
import Objects.MGitObjects.TreeObject;
import Objects.MiniGitRepository;
import UtilityMethods.CreateGitSubdirectories;
import UtilityMethods.WriteObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CommitCommand extends Command {

    /**
    * Vaatab et argumendid poleks tyhjad ning et argumentide listis oleks vahemalt yks element.
     * @return ResultDTO objekt, mis kannab endaga kaasas vastavaid vaartuseid
     */
    @Override
    public ResultDTO execute() {
        if (super.getArgs() == null || super.getArgs().length == 0) {
            return new ResultDTO(false, "Commit message is required", null);
        }

        // ei ole kindel mis korrektne lahendus oleks selle erindipyydmisel
        try {
            commitCommand(getArgs());
        } catch (Exception e ) {
            return new ResultDTO(false, e.getMessage(), null);
        }

        return new ResultDTO(true, "Commit successful", null);
    }

    private void commitCommand(String[] args) throws IOException, NoSuchAlgorithmException {

        // sama kood mis RmCommandis ma ei tea kas peaks tegema funktsiooni? aga kuhu??
        MiniGitRepository miniGitRepository = new MiniGitRepository(System.getProperty("user.dir"));

        // versioon peaks meil alati 2 olema
        MGitIndex mGitIndex = new MGitIndex(2, new ArrayList<>(), miniGitRepository);
        mGitIndex.read();

        // loob puu, tagastatakse rooti SHA-1
        String rootTreeSHA = treeFromIndex(miniGitRepository, mGitIndex);


        // hetkeaeg
        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedTime = formatter.format(now);

        String message = super.getArgs()[0];

        // loob CommitObjecti
        String commit = createCommit(
                miniGitRepository,
                rootTreeSHA,
                miniGitRepository.findObject("HEAD", null),
                formattedTime,
                message);

        // updateib HEAD-i, see commit nüüd lõpus
        String activeBranch = miniGitRepository.getActiveBranch();
        if (activeBranch != null) { // kui branchil, siis update refs/heads/BRANCH
            Path path = Paths.get("refs/heads").resolve(Paths.get(activeBranch)).toAbsolutePath();
            File repoFile = CreateGitSubdirectories.repoFile(miniGitRepository.getRepoDir(), path.toString());
            Files.writeString(repoFile.toPath(), commit + "\n");
        } else { // muidu update HEAD
            File repoFile = CreateGitSubdirectories.repoFile(miniGitRepository.getRepoDir(), "HEAD");
            Files.writeString(repoFile.toPath(),"\n");
        }
    }

    private String createCommit(MiniGitRepository miniGitRepository, String tree, String parent, String timeStamp, String message) throws IOException, NoSuchAlgorithmException {
        CommitObject commitObject = new CommitObject(null);

        message = message.strip() + "\n";

        commitObject.set("tree", tree);
        commitObject.set("parent", parent);
        commitObject.set("timestamp", timeStamp);
        commitObject.set(null, message);

        return WriteObject.writeObject(miniGitRepository, commitObject);
    }

    /**
     * Loob puud, lisab need reposse, tagastab rooti SHA
     * @param miniGitRepository
     * @param mGitIndex
     * @return root SHA (String)
     */
    private String treeFromIndex(MiniGitRepository miniGitRepository, MGitIndex mGitIndex) throws IOException, NoSuchAlgorithmException {
        Map<String, List<Object>> contents = new HashMap<>();

        contents.put("", new ArrayList<>());

        // loob sellise struktuuriga puu
        // struktuur on nt path1 :
        //                          { path2 : failinimi.txt,
        //                           path3 :
        //                                     { path4 : failinimi2.py }
        //                          }
        // failinimi2.py asemel voib olla järgmine puu
        for (MGitIndexEntry mGitIndexEntry : mGitIndex.getEntries()) {
            String dirname = getParentDirectory(mGitIndexEntry.name());
            String key = dirname;
            while (!key.equals("")) {
                if (!contents.containsKey(key)) {
                    contents.put(key, new ArrayList<>());
                }
                key = dirname;
            }
            contents.get(dirname).add(mGitIndexEntry);
        }

        List<String> sortedPaths = (List<String>) contents.keySet();
        sortedPaths.sort((a, b) -> Integer.compare(b.length(), a.length()));

        String SHA = null;

        // teeb puudest hashi, ehk nt path4 võib olla suvakas hash 3049380498309482 vms, ja nii genetakse rootini
        for (String path : sortedPaths) {
            TreeObject treeObject = null;
            for (Object entry : contents.get(path)) {
                TreeDTO leaf;

                // kui on MGitIndexEntry tyypi, ehk leaf
                if (entry instanceof MGitIndexEntry) {
                    String leafMode = Integer.toOctalString(((MGitIndexEntry) entry).modeType()) + Integer.toOctalString(((MGitIndexEntry) entry).modePerms());
                    leaf = new TreeDTO(leafMode.getBytes(), getFileBasename(path).getBytes(), Arrays.toString(((MGitIndexEntry) entry).sha()));
                } else { // kui on (path, sha) tyypi, ehk tree
                    leaf = new TreeDTO("040000".getBytes(), (((List<String>) entry).get(0)).getBytes(), ((List<String>) entry).get(1));
                }
                treeObject.addItem(leaf);
            }

            String sha = WriteObject.writeObject(miniGitRepository, treeObject);

            // lisab parentile hetkelise puu ehk (basename, sha)
            String parent = getParentDirectory(path);
            String basename = getFileBasename(path);
            List<String> valuePair = new ArrayList<>();
            valuePair.add(basename);
            valuePair.add(sha);
            contents.get(parent).add(valuePair);
        }

        return SHA;
    }

    private static String getParentDirectory(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        return parent != null ? parent.toString() : "";
    }

    private static String getFileBasename(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        Path path = Paths.get(filePath);
        Path basename = path.getFileName();
        return basename != null ? basename.toString() : "";
    }


    public CommitCommand(String[] args, MiniGitRepository miniGitRepository) {
        super(args, miniGitRepository);
    }

}
