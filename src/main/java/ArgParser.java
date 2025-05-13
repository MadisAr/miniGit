import Objects.DTO.CommandInfoDTO;

import java.util.Arrays;

public class ArgParser {
    private static final String[] commands = {"init", "add", "commit", "checkout", "rm", "add"};


    /**
     * võtab argumendid ja eraldab funktsiooninimeks
     * @param args commandline argumendid
     * @return CommandInfoDTO objekti
     */
    public CommandInfoDTO parse(String[] args) {
        for (String command: commands) {
            if (args[0].equals(command)) {
                return new CommandInfoDTO(args[0], Arrays.copyOfRange(args, 1, args.length)); // args[0] on command ja teine param on argumentide list
            }
        }
        throw new RuntimeException("command NOT found");
    }
}
