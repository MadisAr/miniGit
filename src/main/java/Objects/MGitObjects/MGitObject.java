package Objects.MGitObjects;

import Objects.MiniGitRepository;

public abstract class MGitObject {
    public MGitObject(byte[] data) { // hetkel argument string, ei pruugi oige olla
        //muudan meetodit oigemaks?
        if (data != null) deserialize(data);
    }

    public abstract String serialize(MiniGitRepository repo);

    public abstract void deserialize(byte[] data);

    public abstract Object getContent();

    public abstract int getSize();

    // tagastab objekti tyybi nt commit v blob
    public abstract String getFormat();
}
