import Objects.CommandInfoDTO;

public class Main {
    public static void main(String[] args) {
        try {
            // C:\\Users\\madismii\\UT\\oop\\miniGit\\testDir
            ArgParser argParser = new ArgParser();
            CommandHandler commandHandler = new CommandHandler();

            CommandInfoDTO commandInfoDTO = argParser.parse(args);

            commandHandler.executeCommand(commandInfoDTO);
        } catch (Exception e) {
            System.out.println("womp womp " + e.getMessage());
        }
    }
}
