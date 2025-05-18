import Commands.*;
import Objects.MiniGitRepository;

import java.nio.file.Paths;

public class CommandFactory {
    /**
     * Teeb antud command name'ist vastava nimega Command classi
     * @param commandName commandi nimi
     * @param arg lisaargumentide list
     * @return vastava Command isendi
     */
    public static Command createCommand(String commandName, String[] arg) {
        MiniGitRepository miniGitRepository = new MiniGitRepository(System.getProperty("user.dir"));

        return switch (commandName) {
            case "commit" -> new CommitCommand(arg, miniGitRepository);
            case "init" -> new InitCommand(arg, miniGitRepository);
            case "checkout" -> new CheckoutCommand(arg, miniGitRepository);
            case "tag" -> new TagCommand(arg, miniGitRepository);
            case "rm" -> new RmCommand(arg, miniGitRepository);
            case "add" -> new AddCommand(arg, miniGitRepository);
            case "status" -> new StatusCommand(arg, miniGitRepository);
            case "check-ignore" -> new CheckIgnoreCommand(arg, miniGitRepository);
            default -> {
                System.out.println("not working");
                yield null;
            }
        };
    }
}