package Commands;

import Objects.DTO.ResultDTO;
import Objects.MiniGitRepository;

import java.nio.file.Paths;

public class CheckIgnoreCommand extends Command {
    public CheckIgnoreCommand(String[] args) {
        super(args);
    }

    @Override
    public ResultDTO execute() {
        MiniGitRepository miniGitRepository = new MiniGitRepository(System.getProperty("user.dir"));

        for (String path : this.getArgs()) {
            if (!miniGitRepository.isFileIgnored(Paths.get(path))) {
                System.out.println(path);
            }
        }

        return null;
    }
}
