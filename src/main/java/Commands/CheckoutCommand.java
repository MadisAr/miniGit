package Commands;

import Objects.MGitObjects.CommitObject;
import Objects.MGitObjects.MGitObject;
import Objects.MGitObjects.TreeObject;
import Objects.MiniGitRepository;
import Objects.DTO.ResultDTO;
import Objects.DTO.TreeDTO;
import UtilityMethods.CreateGitSubdirectories;
import UtilityMethods.ReadObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// TODO teha nii et ei tule alati NULL lopuks return, praegu treecheckout errorid neelatakse alla
public class CheckoutCommand extends Command {

    public CheckoutCommand(String[] args) {
        super(args);
    }

    @Override
    public ResultDTO execute() {
        String result = commandCheckout(getArgs());
        if (result == null) {
            return new ResultDTO(true, "Checkout succesful, commit instantiated at " + getArgs()[1], null);
        }
        else {
            return new ResultDTO(false, result, null);
        }
    }

    public static String commandCheckout(String[] args)  {
        MiniGitRepository repo = CreateGitSubdirectories.repoFind("");
        if (repo == null) return "Couldn't find .mgit directory.";

        CommitObject commitObject = (CommitObject) ReadObject.readObject(repo, args[0]);
        if (commitObject == null) return "Couldn't find commit hash from .mgit directory.";

        TreeObject treeObject = (TreeObject) ReadObject.readObject(repo, commitObject.getContent().get("tree"));
        if (treeObject == null) return "Something went wrong, sorry :)";

        Path path = Path.of(args[1]);

        if (Files.exists(path)) {
            if (!Files.isDirectory(path)) {
                return ".mgit is not a directory.";
            }
            String[] files = path.toFile().list();
            if (files != null && files.length != 0) {
                return args[1] + " directory is not empty.";
            }
        } else {
            return "Given directory " + args[1] + " doesn't exist.";
        }

        try {
            return treeCheckout(repo, treeObject, args[1]);
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public static String treeCheckout(MiniGitRepository repo, TreeObject tree, String path) throws IOException {
        for (TreeDTO o : tree.getContent()) {
            MGitObject obj = ReadObject.readObject(repo, o.sha());
            File dest = Paths.get(path).resolve(new String(o.path())).toFile();// vblla peaks olema global path?

            if (obj.getFormat().equals("tree")) {
                dest.mkdir();
                treeCheckout(repo, (TreeObject) obj, dest.toString());
            } else if (obj.getFormat().equals("blob")) {
                String content = (String) obj.getContent();
                try (FileWriter fileWriter = new FileWriter(dest)) {
                    fileWriter.write(content);
                } catch (IOException e) {
                    return "Something went wrong with writing files.";
                }
            }
        }
        return null;
    }
}
