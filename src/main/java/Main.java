import Commands.Command;
import Objects.DTO.ResultDTO;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String commandName = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);
        Command command = CommandFactory.createCommand(commandName, args);
        ResultDTO result = command != null ? command.execute() : new ResultDTO(false, "Command not found", null);

        if (result.isSuccess()) {
            System.out.println(result.message() + "\n");
        } else {
            System.out.println("error: " + result.message() + "\n");
        }
    }
}
