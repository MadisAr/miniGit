package Objects.MGitObjects;

import Objects.MiniGitRepository;

public class TreeObject extends MGitObject{
    private final String format = "tree";

    public TreeObject(String data) {
        super(data);
    }

    @Override
    public String serialize(MiniGitRepository repo) {
        return "";
    }

    @Override
    public void deserialize(String data) {

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
