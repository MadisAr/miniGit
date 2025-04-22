package Objects.MGitObjects;

import Objects.MiniGitRepository;

public class BlobObject extends MGitObject {
    String format = "blob"; // lopuks akki teeme baitidega
    String content;
    Integer size;

    public BlobObject(String data) {
        super(data);
    }

    @Override
    public int getSize() {
        return size;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public String serialize(MiniGitRepository repo) {
        return content;
    }

    @Override
    public void deserialize(String data) {
        this.content = data;
        String[] stuff = data.split(" ", 2);
        this.size = Integer.valueOf(stuff[0]);
        this.content = stuff[1];
    }
}
