package Commands;

import Objects.DTO.ResultDTO;
import Objects.MGitIndex;
import Objects.MiniGitRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

public class RmCommand extends Command {
    public RmCommand(String[] args) {
        super(args);
    }

    @Override
    public ResultDTO execute() {
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

        // kui antud path on fail siis removime ainult selle
        if (!Files.isDirectory(filePath)) {
            mGitIndex.removeEntry(pathString);
        } else {
            // Kaime koik alamfailid labi ja tavaliste failide puhul eemaldame
            try (Stream<Path> stream = Files.walk(filePath)) {
                stream.filter(Files::isRegularFile).forEach(path -> {
                    // teeme pathist relative pathi
                    Path relative = miniGitRepository.getRepoDir().relativize(path.toAbsolutePath());
                    mGitIndex.removeEntry(String.valueOf(relative));
                });
            } catch (Exception e) {
                return new ResultDTO(false, e.getMessage(), null);
            }
        }

        mGitIndex.write();
        System.out.println(mGitIndex.getEntries());
        return new ResultDTO(true, "File(s) removed", null);
    }
}
