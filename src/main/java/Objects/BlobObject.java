package Objects;

public class BlobObject extends MGitObject{
    String format = "blob"; // lopuks akki teeme baitidega


    public BlobObject(String data) {
        super(data);
    }

    @Override
    public void serialize(MiniGitRepository repo) {

    }

    @Override
    public void deserialize(String data) {
    }
}
