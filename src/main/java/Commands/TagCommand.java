package Commands;

import Objects.DTO.ResultDTO;
import Objects.MGitObjects.TagObject;
import Objects.MiniGitRepository;
import Objects.Ref;
import UtilityMethods.CreateGitSubdirectories;
import UtilityMethods.WriteObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class TagCommand extends Command{

    public TagCommand(String[] args) {
        super(args);
    }

    @Override
    public ResultDTO execute() {
        String result = commandTag(getArgs());

        if (result == null) {
            return new ResultDTO(true, "SAMPLE SUCCESS", null);
        } else {
            return new ResultDTO(false, result, null);
        }
    }

    public static String commandTag(String[] args){
        MiniGitRepository repo = CreateGitSubdirectories.repoFind("");

        if (args.length == 0) {
            Ref ref = new Ref(null, null);
            System.out.println(ref.listRefs(repo));
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
            Ref ref = new Ref(null, null);
            System.out.println(ref.listRefs(repo));
            return null;
        } else {
            try {
                return tagCreate(repo, createTagObject, name, object);
            } catch (Exception e ) {
                return e.getMessage();
            }
        }
    }

    private static String tagCreate(MiniGitRepository repo, boolean createTagObject, String name, String object) throws IOException, NoSuchAlgorithmException {
        String sha = repo.findObject(object, null);


        Path tags = Paths.get("tags", name);

        if (createTagObject) {
            TagObject tagObject = new TagObject(null);

            tagObject.set("object", sha);
            tagObject.set("type", "commit");
            tagObject.set("tag", name);
            tagObject.set("tagger", "Tag <tag@example.com>");
            tagObject.setMessage("Sample message, automatically generated...");

            String tagSha = WriteObject.writeObject(repo, tagObject);
            System.out.println(tagSha);

            // refCreate function
            return refCreate(repo, String.valueOf(tags), tagSha);

        } else {
            // refCreate lightweight tag
            return refCreate(repo, String.valueOf(tags), sha);
        }
    }

    private static String refCreate(MiniGitRepository repo, String refName, String sha) throws IOException { // vb panna Ref classi meetodiks pigem. praegu panen siia, kergem hallata
        Path refPath = repo.getGitDir().resolve("refs").resolve(refName);
        File refFile = new File(String.valueOf(CreateGitSubdirectories.repoFile(repo.getGitDir(), String.valueOf(refPath))));

        try (FileWriter fileWriter = new FileWriter(refFile)) {
            fileWriter.write(sha + "\n");
            return null;
        } catch (Exception e) {
            return "Error writing SHA to reference file at: " + refFile;
        }
    }
}
