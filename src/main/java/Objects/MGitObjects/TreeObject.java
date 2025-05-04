package Objects.MGitObjects;

import Objects.MiniGitRepository;
import Objects.DTO.TreeDTO;
import UtilityMethods.ParseTree;

import java.util.List;

public class TreeObject extends MGitObject{
    private final String format = "tree";
    private List<TreeDTO> items;

    public TreeObject(byte[] data) {
        super(data);
    }

    @Override
    public String serialize(MiniGitRepository repo) {
        return ""; // todo hetkel katki vist
    }

    @Override
    public void deserialize(byte[] data) {
        this.items = ParseTree.parseTree(data);
    }

    @Override
    public List<TreeDTO> getContent() {
        return items;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public String getFormat() {
        return format;
    }
}
