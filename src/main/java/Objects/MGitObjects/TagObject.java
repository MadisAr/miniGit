package Objects.MGitObjects;

import Objects.MiniGitRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class TagObject extends CommitObject { // idk kas see on hea mote
    private final String format = "tag";
    private final Map<String, String> kvlm = new LinkedHashMap<>();
    private String message = "";


    public TagObject(byte[] data) {
        super(data);
    }

    public void set(String key, String value) {
        kvlm.put(key, value);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String serialize(MiniGitRepository repo) {
        StringJoiner stringJoiner = new StringJoiner("\n");

        for (Map.Entry<String, String> entry : kvlm.entrySet()) {
            stringJoiner.add(entry.getKey() + " " + entry.getValue());
        }

        stringJoiner.add("");
        stringJoiner.add(message);

        return stringJoiner.toString();
    }

}
