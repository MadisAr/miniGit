package Objects.MGitObjects;

import Objects.MiniGitRepository;
import Objects.TreeDTO;
import UtilityMethods.ParseTree;

import java.util.ArrayList;
import java.util.List;

public class TreeObject extends MGitObject{
    private final String format = "tree";
    private List<TreeDTO> items = new ArrayList<>();

    public TreeObject(String data) {
        super(data);
    }

    @Override
    public String serialize(MiniGitRepository repo) {
        return ""; // todo hetkel katki vist
    }

    @Override
    public void deserialize(String data) {
        this.items = ParseTree.parseTree(data.getBytes());
    }

    @Override
    public Object getContent() {
        return null;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public String getFormat() {
        return "";
    }
}
