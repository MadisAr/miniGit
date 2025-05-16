package Commands;

import Objects.MiniGitRepository;
import Objects.DTO.ResultDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static UtilityMethods.CreateGitSubdirectories.createGitSubdirectories;

public class InitCommand extends Command {

    public InitCommand(String[] args, MiniGitRepository miniGitRepository) {
        super(args, miniGitRepository);
    }

    /**
     * executib commandi ja tagastab resultDTO objekti
     *
     * @return resultDTO objekt
     */
    @Override
    public ResultDTO execute() {
        ResultDTO resultDTO;

        try {
            createRepo(super.getArgs()[0]);
            resultDTO = new ResultDTO(true, "Initialized repository", null);
        } catch (Exception e) {
            resultDTO = new ResultDTO(false, e.getMessage(), null);
        }

        return resultDTO;
    }

    /**
     * loob antud kausta .mgit kausta ja sinna sisse vajalikud alamkaustad
     *
     * @param path kaust kuhu luua .mgit
     * @return
     */
    void createRepo(String path) {
        if (path.equals(".")) path = "";

        File worktreeFile = super.getMinigitRepository().getRepoDir().resolve(path).toFile();
        File gitDirFile = getFile(worktreeFile);

        createGitSubdirectories(gitDirFile, "branches");
        createGitSubdirectories(gitDirFile, "objects");
        createGitSubdirectories(gitDirFile, "refs", "tags");
        createGitSubdirectories(gitDirFile, "refs", "headers");
        try {
            Files.writeString(super.getMinigitRepository().getRepoDir().resolve(".mgitignore"), ".mgit");
        } catch (IOException ignored) {
        }
    }

    private File getFile(File worktreeFile) {
        File gitDirFile = super.getMinigitRepository().getGitDir().toFile();

        // kui loodav kaust eksisteerib juba
        if (worktreeFile.exists()) {
            // kui worktreeFile directory pole kaust
            if (!worktreeFile.isDirectory()) {
                throw new RuntimeException("Is not a directory");
            }// kui .git directorys on juba faile
            else if (gitDirFile.exists() && gitDirFile.isDirectory() && gitDirFile.list().length > 0) {
                throw new RuntimeException(".mgit directory is not empty");
            }
        } else {
            if (!worktreeFile.mkdirs()) {
                throw new RuntimeException("Failed to create directory");
            }
        }
        return gitDirFile;
    }


}
