package Commands;

import Objects.DTO.ResultDTO;
import Objects.MGitIndex;
import Objects.MiniGitRepository;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.stream.Stream;

public class RmCommand extends Command {
    public RmCommand(String[] args) {
        super(args);
    }

    @Override
    public ResultDTO execute() throws IOException, NoSuchAlgorithmException {
        MiniGitRepository miniGitRepository = new MiniGitRepository(System.getProperty("user.dir"));

        // versioon peaks meil alati 2 olema
        MGitIndex mGitIndex = new MGitIndex(2, new ArrayList<>(), miniGitRepository);
        mGitIndex.read();

        Path p = Path.of(getArgs()[0]);
        // kui antud path on fail siis removime ainult selle
        if (!Files.isDirectory(p)) {
            mGitIndex.removeEntry(getArgs()[0]);
        } else {
            // Kaime koik alamfailid labi ja tavaliste failide puhul eemaldame
            try (Stream<Path> stream = Files.walk(p)) {
                stream.filter(Files::isRegularFile).forEach(path -> {
                    // teeme pathist relative pathi
                    Path relative = miniGitRepository.getRepoDir().relativize(path.toAbsolutePath());
                    mGitIndex.removeEntry(String.valueOf(relative));
                });
            }
        }

        mGitIndex.write();
        return null;
    }
}
