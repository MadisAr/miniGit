package Commands;

import Objects.MGitObjects.CommitObject;
import Objects.MGitObjects.MGitObject;
import Objects.MGitObjects.TreeObject;
import Objects.MiniGitRepository;
import Objects.ResultDTO;
import Objects.TreeDTO;
import UtilityMethods.CreateGitSubdirectories;
import UtilityMethods.ReadObject;
import com.sun.source.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        if (repo == null) return;

        CommitObject object = (CommitObject) ReadObject.readObject(repo, args[0]);
        if (object == null) return;

        TreeObject object1 = (TreeObject) ReadObject.readObject(repo, object.getContent().get("tree"));
        if (object1 == null) return;

        Path path = Path.of(args[1]);

        if (Files.exists(path)) {
            if (!Files.isDirectory(path)) {
                System.out.println("Not a directory " + args[1]);
            }
//            if (Files.list(path) != null) {
//                System.out.println("Directory not empty " + args[1]);
//            }
        }
        else {
            System.out.println("Doesn't exist:("); // teoorias voib teha hiljem selle et kui kausta pole siis teeb uue ja jatkab
            return;
        }

//        treeCheckout(repo, object1, args[1]);
    }

//    public static void treeCheckout(MiniGitRepository repo, TreeObject tree, String path) throws IOException {
//        for (TreeDTO o : tree.getContent()) {
//            MGitObject obj = ReadObject.readObject(repo, o.sha());
//            File dest = Paths.get(path).resolve(new String(o.path())).toFile();
//
//            if (obj.getFormat().equals("tree")) {
//                dest.mkdir();
//            }
//            else if (obj.getFormat().equals("blob")) {
//
//            }
//        }
//    }
}
