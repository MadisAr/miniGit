package Commands;

import Objects.DTO.ResultDTO;
import Objects.MGitIndex;
import Objects.MiniGitRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

public class RmCommand extends Command {
    public RmCommand(String[] args, MiniGitRepository miniGitRepository) {
        super(args, miniGitRepository);
    }

    @Override
    public ResultDTO execute() {
        MiniGitRepository miniGitRepository = super.getMinigitRepository();

        String pathString = getArgs()[0].equals(".") ? "" : getArgs()[0];
        Path filePath = miniGitRepository.getRepoDir().resolve(pathString);

        // versioon peaks meil alati 2 olema
        MGitIndex mGitIndex = new MGitIndex(2, new ArrayList<>(), miniGitRepository);
        try {
            mGitIndex.read();
        } catch (Exception e) {
            // kui fail on tyhi
            mGitIndex.write();
            return new ResultDTO(true, "Nothing added", null);
        }

        // kui antud path on fail siis removime ainult selle
        if (!Files.isDirectory(filePath)) {
            mGitIndex.removeEntry(pathString);
        } else {
            // Kaime koik alamfailid labi ja tavaliste failide puhul eemaldame
            try (Stream<Path> stream = Files.walk(filePath)) {
                stream.filter(Files::isRegularFile).forEach(path -> {
                    // teeme pathist relative pathi
                    if (!miniGitRepository.isFileIgnored(path)){
                        Path repoDir = miniGitRepository.getRepoDir().toAbsolutePath().normalize();
                        Path absPath = path.toAbsolutePath().normalize();
                        Path relative = repoDir.relativize(absPath);
                        mGitIndex.removeEntry(String.valueOf(relative));
                    }
                });
            } catch (Exception e) {
                return new ResultDTO(false, e.getMessage(), null);
            }
        }

        mGitIndex.write();
        return new ResultDTO(true, "File(s) removed", null);
    }
}
