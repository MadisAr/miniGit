package Commands;

import Objects.DTO.ResultDTO;
import Objects.MiniGitRepository;

public class CommitCommand extends Command {

    /**
    * Vaatab et argumendid poleks tyhjad ning et argumentide listis oleks vahemalt yks element.
     * @return ResultDTO objekt, mis kannab endaga kaasas vastavaid vaartuseid
     */
    @Override
    public ResultDTO execute() {
        if (super.getArgs() == null || super.getArgs().length == 0) {
            return new ResultDTO(false, "Commit message is required", "null");
        }

        return new ResultDTO(true, "Commit successful", "hash??");
    }

    public CommitCommand(String[] args, MiniGitRepository miniGitRepository) {
        super(args, miniGitRepository);
    }
}
