package Objects;

public record MGitIndexEntry(int cTimeSeconds, int cTimeNanoseconds, int mTimeSeconds, int mTimeNanoseconds,
                             int dev, int ino, int modeType, int modePerms,
                             int uid, int gid, int fileSize,
                             byte[] sha, short flags, String name) {
}
