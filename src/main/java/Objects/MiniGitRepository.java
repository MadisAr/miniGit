package Objects;

import Objects.MGitObjects.MGitObject;
import UtilityMethods.CreateGitSubdirectories;
import UtilityMethods.ReadObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniGitRepository {
    private final String workTree;
    private final Path gitDir;
    private final Path repoDir;
    private Set<Path> ignoredFiles;

    public String getWorkTree() {
        return workTree;
    }

    public Path getGitDir() {
        return gitDir;
    }

    public Path getRepoDir() {
        return repoDir;
    }

    public MiniGitRepository(String path) {
        this.workTree = path;
        this.gitDir = Paths.get(path).resolve(".mgit");
        this.repoDir = gitDir.getParent();
        this.ignoredFiles = new HashSet<>();
    }


    /**
     * TODO praegu pole error handlimine eriti hea (iga kord manuaalselt kirjutada error message) vblla pigem teha ise exception?
     * votab string ja valikulise parameetri fmt
     *
     * @param name otsitav nimi
     * @param fmt  kui soovitakse mingit kindlat tyypi objekti
     * @return tagastab leitud String sha
     * @throws IOException readObject error
     */
    public String findObject(String name, String fmt) throws IOException {
        List<String> res = findCandidates(name);
        if (res == null || res.isEmpty()) throw new RuntimeException("Object not found");

        if (res.size() > 1) throw new RuntimeException("Multiple objects with same name");

        String sha = res.getFirst();

        if (fmt == null) return sha;

        MGitObject obj = ReadObject.readObject(this, sha);
        if (obj == null) throw new RuntimeException("Error parsing object");

        return obj.getFormat().equals(fmt) ? sha : null;
    }

    /**
     * TODO lisada et saaks ka sha algusega viidata objektidele, praegu tootab ainult tais shaga
     * otsib nimele vastavad kandidaadid
     * kontrollib refs kausta, vaatab kas tegu voib olla shaga
     *
     * @param name antud nimi
     * @return tagastav listi kandidaatidest mis peaks olema 1 elemendi pikkune
     */
    private List<String> findCandidates(String name) throws IOException {
        if (name.isEmpty()) {
            return null;
        }

        List<String> matches = new ArrayList<>();

        if (name.equals("HEAD")) {
            Path headPath = gitDir.resolve("HEAD");
            Ref ref = new Ref(this, headPath);
            ref.findSha();
            matches.add(ref.getSha());
            return matches;
        }

        // vaatame regexiga kas antud nimi voib potentsiaalselt olla sha
        Pattern pattern = Pattern.compile("^[0-9A-Fa-f]{4,40}$");
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            String prefix = name.substring(0, 2);
            String sha = name.substring(2);
            if (Files.exists(gitDir.resolve("objects").resolve(prefix).resolve(sha))) matches.add(name);
        }

        // vaatame kas refs tags heads voi remotes kaustas on meie nimega asju
        addRefsPaths("tags", name, matches);
        addRefsPaths("heads", name, matches);
        addRefsPaths("remotes", name, matches);

        return matches;
    }

    /**
     * Otsib antud kaustast name nimega objekti ja lisab leitud sha'd matches Listi
     *
     * @param refsPath .mgit/refs kaustast lahtuv path
     * @param name     otsitava elemendi nimi
     * @param matches  List kuhu leitud elemendid lisatakse
     */
    private void addRefsPaths(String refsPath, String name, List<String> matches) {
        Path tagPath = gitDir.resolve("refs").resolve(refsPath).resolve(name);
        if (Files.exists(tagPath)) {
            Ref ref = new Ref(this, tagPath);
            try {
                ref.findSha();
                matches.add(ref.getSha());
            } catch (IOException e) {
                throw new RuntimeException("Error finding candidates");
            }
        }
    }

    /**
     * Loeb koik failid mGitIgnore failist ja moodustab nendest listi failidest mida ignoreerida
     *
     * @throws IOException Files.readAllLines error
     */
    public void findIgnored() throws IOException {
        List<String> mGitIgnoreLines = Files.readAllLines(repoDir.resolve(".mgitignore"));

        for (String mGitIgnoreLine : mGitIgnoreLines) {
            if (!mGitIgnoreLine.isEmpty() && mGitIgnoreLine.charAt(0) != '#') {
                ignoredFiles.add(repoDir.resolve(mGitIgnoreLine));
            }
        }
    }

    /**
     * TODO peaks midagi muud tegema kui faili ei eksisteeri yldse
     * votab filePathi ja vaatab kas ta on ignoreeritud failide listis
     * enne funktsiooni peab jooksutama vajadusel
     *
     * @param filePath vaadeldav fail
     * @return tagastab true kui fail on ignoreeritud ja false kui ei ole ignoreeritud
     */
    public boolean isFileIgnored(Path filePath) {
        if (!Files.exists(filePath)) throw new RuntimeException("File not found");

        Path fileParent = filePath;
        if (ignoredFiles.contains(fileParent)) return true; // TODO MIKS SEE EI TOOTA!!!???!! TOOTAB KULL?? ?

        while ((fileParent = fileParent.getParent()) != null) {
            // kui oleme joudnud repo programmi juurkaustani tagastame false
            if (fileParent == repoDir) return false;

            // kui faili parent on ignoreeritud failide listis tagastame true
            if (ignoredFiles.contains(fileParent)) return true;
        }

        // kui parenteid rohkem pole oleme joudnud failitee algusesse jarelikult polnud fail meie repo kaustas
        // hetkel tagastan false aga vblla peaks errori viskama?
        return false;
    }


    /**
     * Tagastab hetkel aktiivse branchi
     * @return aktiivne branch
     * @throws IOException
     */
    public String getActiveBranch() throws IOException {
        File repoFile = CreateGitSubdirectories.repoFile(gitDir, "HEAD");
        String data = "";
        if (Files.isRegularFile(repoFile.toPath())) {
            data = Files.readString(repoFile.toPath());
        }

        if (data.startsWith("ref: refs/heads/")) {
            return data.substring(16, data.length() - 1);
        } else return null;
    }
}