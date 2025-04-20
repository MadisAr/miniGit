package Objects;

public abstract class MGitObject {
    String size;
    String content;
    String format; // kuidagi peab implementima selle sisseandmise ka...


    public MGitObject(String data) { // hetkel argument string, ei pruugi oige olla
        // ajutine info testimiseks
        String[] stuff = data.split(" ", 2);
        this.size = stuff[0];
        this.content = stuff[1];


        if (data == null) {
            deserialize(data);
        } else {
            //initializime kuidagi...
        }
    }

    public abstract String serialize(MiniGitRepository repo);

    public abstract void deserialize(String data);

    public String getSize() {
        return this.size;
    }

    public String getContent() {
        return this.content;
    }

    public String getFormat() {
        return format;
    }
}
