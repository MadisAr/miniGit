import Commands.*;

public class CommandFactory {
    public static Command createCommand(String commandName, String arg) throws Exception {

        if (commandName.equals("commit")) {
            return new CommitCommand(arg);
        }

        return null;
    }
}