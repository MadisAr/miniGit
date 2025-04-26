package Commands;

import Objects.MGitObjects.MGitObject;
import Objects.MiniGitRepository;
import Objects.ResultDTO;
import UtilityMethods.CreateGitSubdirectories;
import UtilityMethods.ReadObject;

import java.io.IOException;

public class CheckoutCommand extends Command {

    public CheckoutCommand(String[] args) {
        super(args);
    }

    @Override
    public ResultDTO execute() {
        return null;
    }

    public static void commandCheckout(String[] args) throws IOException {
        MiniGitRepository repo = CreateGitSubdirectories.repoFind(".");

        MGitObject object = ReadObject.ReadObject(repo, args[0]);

//        if (object.getFormat().equals())
    }
}
