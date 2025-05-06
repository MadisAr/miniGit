import Commands.Command;
import Objects.DTO.CommandInfoDTO;
import Objects.DTO.ResultDTO;


public class CommandHandler {

    /**
     * Votab commandInfoDTO-st commandi ja argumendid. Loob uue command isendi ja saadab selle CommandFactorysse.
     * Seejarel executib commandi ja salvestab tulemuse resultDTO objekti, mille pohjal otsustab mida kasutajale valjastada.
     * @param commandInfoDTO
     * return void
     */
    public static void executeCommand(CommandInfoDTO commandInfoDTO) {
        try {
            String commandName = commandInfoDTO.command();
            String[] args = commandInfoDTO.args();
            Command command = CommandFactory.createCommand(commandName, args);
            ResultDTO result = command.execute();

            if (result.isSuccess()) {
                System.out.println(result.message() + "\n");
            }
            else {
                System.out.println("error: " + result.message() + "\n");
            }
        } catch (Exception e) {
            System.out.println(":( " + e.getMessage());
        }
    }
}
