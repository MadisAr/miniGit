package Commands;

import Objects.DTO.ResultDTO;
import Objects.MGitObjects.MGitObject;
import Objects.MGitObjects.TagObject;
import Objects.MiniGitRepository;
import UtilityMethods.CreateGitSubdirectories;
import UtilityMethods.WriteObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class TagCommand extends Command{

    public TagCommand(String[] args) {
        super(args);
    }

    @Override
    public ResultDTO execute() throws IOException {

        return null;
    }

    public static String commandTag(String[] args) throws IOException, NoSuchAlgorithmException {
        MiniGitRepository repo = CreateGitSubdirectories.repoFind("");

        if (args.length == 0) {
            listTags(repo);
            return null;
        }

        // set the default values and override when we find the right flags from the args list
        boolean createTagObject = false;
        String name = null;
        String object = "HEAD";

        int i = 0;

        if (args[i].equals("-a")) {
            createTagObject = true;
            i++;
        }

        if (i < args.length) {
            name = args[i];
            i++;
        }

        if (i < args.length) {
            object = args[i];
        }

        if (name == null) {
            listTags(repo); // peaks olema ref.listRef class
        } else {
            tagCreate(repo, createTagObject, name, object);
        }

        return null;
    }

    private static void tagCreate(MiniGitRepository repo, boolean createTagObject, String name, String object) throws IOException, NoSuchAlgorithmException {

        if (createTagObject) {
            TagObject tagObject = new TagObject(null);

            tagObject.set("object", object);
            tagObject.set("type", "commit");
            tagObject.set("tag", name);
            tagObject.set("tagger", "Tag <tag@example.com>");
            tagObject.setMessage("Sample message, automatically generated...");

            String tagSha = WriteObject.writeObject(repo, tagObject);
            System.out.println(tagSha);
            // refCreate function

        } else {
            // refCreate lightweight tag
        }

    }

    private static void listTags(MiniGitRepository repo) {

    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        String[] args1 = new String[]{"-a", "testName", "testObject"};
        String result = commandTag(args1);
    }
}
