package Commands;

import Objects.MiniGitRepository;
import Objects.DTO.ResultDTO;

import java.io.File;

import static UtilityMethods.CreateGitSubdirectories.createGitSubdirectories;

public class InitCommand extends Command {

    public InitCommand(String[] args) {
        super(args);
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
    MiniGitRepository createRepo(String path) {
        MiniGitRepository repo = new MiniGitRepository(path);

        File worktreeFile = new File(repo.getWorkTree());
        File gitDirFile = repo.getGitDir().toFile();

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

        createGitSubdirectories(gitDirFile, "branches");
        createGitSubdirectories(gitDirFile, "objects");
        createGitSubdirectories(gitDirFile, "refs", "tags");
        createGitSubdirectories(gitDirFile, "refs", "headers");

        return repo;
    }


}
