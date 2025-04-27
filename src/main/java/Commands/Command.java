package Commands;

import Objects.ResultDTO;

import java.io.IOException;

public abstract class Command {
    private final String[] args;

    public Command(String[] args) {
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }

    abstract public ResultDTO execute() throws IOException;
}
