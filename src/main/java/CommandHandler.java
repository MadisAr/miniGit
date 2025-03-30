import Commands.Command;
import Objects.CommandInfoObject;
import Objects.ResultObject;


public class CommandHandler {
    public static void executeCommand(CommandInfoObject commandInfoObject) {
        try {
            String commandName = commandInfoObject.command();
            String arg = commandInfoObject.args(); // muutsin ara et commandinfoObjecti sisend on String mitte String[]
            Command command = CommandFactory.createCommand(commandName, arg);
            ResultObject result = command.execute();

            if (result.isSuccess()) {
                System.out.println("TOOTAB " + result.message());
            }
            else {
                System.out.println("ei toota " + result.message());
            }
        } catch (Exception e) {
            System.out.println(":( " + e.getMessage());
        }
    }
}
