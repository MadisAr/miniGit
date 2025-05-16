package Commands;

import Objects.DTO.ResultDTO;
import Objects.MiniGitRepository;

public abstract class Command {
    private final String[] args;

    public MiniGitRepository getMinigitRepository() {
        return minigitRepository;
    }

    private final MiniGitRepository minigitRepository;

    public Command(String[] args, MiniGitRepository miniGitRepository) {
        this.args = args;
        this.minigitRepository = miniGitRepository;
    }

    public String[] getArgs() {
        return args;
    }

    abstract public ResultDTO execute();
}
