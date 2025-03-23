package Commands;

import Objects.ResultObject;

public class CommitCommand implements Command {
    private String message;

    @Override
    public ResultObject execute() {
        if (message == null || message.isEmpty()) {
            return new ResultObject(false, "Commit message is required", "null");
        }

        return new ResultObject(true, "Commit successful", "hash??");
    }

    public CommitCommand(String message) {
        this.message = message;
    }
}
