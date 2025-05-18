package Objects.MGitObjects;

import Objects.MiniGitRepository;
import UtilityMethods.KvlmParse;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommitObject extends MGitObject {
    protected final Map<String, String> kvlm = new LinkedHashMap<>();
    private Map<String, String> content;
    private final String format = "commit";


    @Override
    public Map<String, String> getContent() {
        return content;
    }

    @Override
    public int getSize() {
        return content.get("message").length();
    }

    @Override
    public String getFormat() {
        return format;
    }

    public CommitObject(byte[] data) {
        super(data);
    }

    // peaks vist tagastama oma contentist tehtud byte[] arrayks
    @Override
    public String serialize(MiniGitRepository repo) {
        return "";
    }

    @Override
    public void deserialize(byte[] data) {
        content = KvlmParse.KvlmParse(data);
    }

    public void set(String key, String value) {
        kvlm.put(key, value);
    }
}


