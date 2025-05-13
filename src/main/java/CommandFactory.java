import Commands.*;

public class CommandFactory {
    /**
     * Teeb antud command name'ist vastava nimega Command classi
     * @param commandName commandi nimi
     * @param arg lisaargumentide list
     * @return vastava Command isendi
     */
    public static Command createCommand(String commandName, String[] arg) {

        return switch (commandName) {
            case "commit" -> new CommitCommand(arg);
            case "init" -> new InitCommand(arg);
            case "checkout" -> new CheckoutCommand(arg);
            case "tag" -> new TagCommand(arg);
            case "rm" -> new RmCommand(arg);
            case "add" -> new AddCommand(arg);
            default -> {
                System.out.println("not working");
                yield null;
            }
        };
    }
}