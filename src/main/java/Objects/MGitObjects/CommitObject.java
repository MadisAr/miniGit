package Objects.MGitObjects;

import Objects.MiniGitRepository;
import UtilityMethods.KvlmParse;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommitObject extends MGitObject {
    protected Map<String, String> kvlm;
    private Map<String, String> content;
    private final String format = "commit";


    @Override
    public Map<String, String> getContent() {
        return kvlm;
    }

    @Override
    public int getSize() {
        return kvlm.get("message").length();
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
        return new String(KvlmParse.KvlmUnParse(kvlm));
    }

    @Override
    public void deserialize(byte[] data) {
        kvlm = KvlmParse.KvlmParse(data);
    }

    public void set(String key, String value) {
        if (kvlm == null) kvlm = new LinkedHashMap<>();
        kvlm.put(key, value);
    }
}


