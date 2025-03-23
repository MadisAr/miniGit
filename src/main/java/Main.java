import Objects.CommandInfoObject;
import Objects.ResultObject;

public class Main {
    public static void main(String[] args) {
        try {
            ArgParser argParser = new ArgParser();
            CommandHandler commandHandler = new CommandHandler();

            CommandInfoObject commandInfoObject = argParser.parse(args);

            commandHandler.executeCommand(commandInfoObject);
        } catch (Exception e) {
            System.out.println("womp womp " + e.getMessage());
        }
    }
}
