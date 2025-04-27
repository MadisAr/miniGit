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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CheckoutCommand extends Command {

    public CheckoutCommand(String[] args) {
        super(args);
    }

    @Override
    public ResultDTO execute() throws IOException {
        ResultDTO resultDTO;

        commandCheckout(getArgs());
        return null;
    }

    public static void commandCheckout(String[] args) throws IOException {
        MiniGitRepository repo = CreateGitSubdirectories.repoFind("");
        if (repo == null) return;

        CommitObject commitObject = (CommitObject) ReadObject.readObject(repo, args[0]);
        if (commitObject == null) return;

        TreeObject treeObject = (TreeObject) ReadObject.readObject(repo, commitObject.getContent().get("tree"));
        if (treeObject == null) return;

        Path path = Path.of(args[1]);

        if (Files.exists(path)) {
            if (!Files.isDirectory(path)) {
                System.out.println("Not a directory " + args[1]);
            }
//            if (Files.list(path) != null) {
//                System.out.println("Directory not empty " + args[1]);
//            }
        } else {
            System.out.println("Doesn't exist:("); // teoorias voib teha hiljem selle et kui kausta pole siis teeb uue ja jatkab
            return;
        }

        treeCheckout(repo, treeObject, args[1]);
    }

    public static void treeCheckout(MiniGitRepository repo, TreeObject tree, String path) throws IOException {
        for (TreeDTO o : tree.getContent()) {
            MGitObject obj = ReadObject.readObject(repo, o.sha());
            File dest = Paths.get(path).resolve(new String(o.path())).toFile();// vblla peaks olema global path?

            if (obj.getFormat().equals("tree")) {
                dest.mkdir();
            } else if (obj.getFormat().equals("blob")) {
                String content = (String) obj.getContent();
                try (FileWriter fileWriter = new FileWriter(dest)) {
                    fileWriter.write(content);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
