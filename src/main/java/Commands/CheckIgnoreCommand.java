package Commands;

import Objects.DTO.ResultDTO;
import Objects.MiniGitRepository;

import java.nio.file.Files;
import java.nio.file.Paths;

public class CheckIgnoreCommand extends Command {
    public CheckIgnoreCommand(String[] args, MiniGitRepository miniGitRepository) {
        super(args, miniGitRepository);
    }

    @Override
    public ResultDTO execute() {
        MiniGitRepository miniGitRepository = new MiniGitRepository(System.getProperty("user.dir"));

        for (String path : this.getArgs()) {
            if (Files.isRegularFile(Paths.get(path))) {
                if (!miniGitRepository.isFileIgnored(Paths.get(path))) {
                    System.out.println(path);
                }
            }
        }

        return new ResultDTO(true, "file(s) check-ignored", null);
    }
}
