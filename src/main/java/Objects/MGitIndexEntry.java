package Objects;

public class MGitIndexEntry {
    int cTimeSeconds;
    int cTimeNanoseconds;
    int mTimeSeconds;
    int mTimeNanoseconds;
    int dev;
    int ino;
    int modeType;
    int modePerms;
    int uid;
    int gid;
    int fileSize;
    byte[] sha;
    short flags;
    String name;

    public MGitIndexEntry(int cTimeSeconds, int cTimeNanoseconds, int mTimeSeconds, int mTimeNanoseconds,
                          int dev, int ino, int modeType, int modePerms,
                          int uid, int gid, int fileSize,
                          byte[] sha, short flags, String name) {
        this.cTimeSeconds = cTimeSeconds;
        this.cTimeNanoseconds = cTimeNanoseconds;
        this.mTimeSeconds = mTimeSeconds;
        this.mTimeNanoseconds = mTimeNanoseconds;
        this.dev = dev;
        this.ino = ino;
        this.modeType = modeType;
        this.modePerms = modePerms;
        this.uid = uid;
        this.gid = gid;
        this.fileSize = fileSize;
        this.sha = sha;
        this.flags = flags;
        this.name = name;
    }
}
