import Objects.CommandInfoObject;

public class ArgParser {
    private static final String[] commands = {"init", "add", "commit"};

    public CommandInfoObject parse(String[] args) {
        for (String command: commands) {
            if (args[0].equals(command)) {
                return new CommandInfoObject(args[0], "sind ei ole");
            }
        }
        throw new RuntimeException("command NOT found");
    }
}
