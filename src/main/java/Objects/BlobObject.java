package Objects;

public class BlobObject extends MGitObject {
    String format = "blob"; // lopuks akki teeme baitidega


    public BlobObject(String data) {
        super(data);
    }

    @Override
    public String serialize(MiniGitRepository repo) {
        return content;
    }

    @Override
    public void deserialize(String data) {
        this.content = data;
    }
}
