package UtilityMethods;

public class FindFirstChar {
    public static int findFirstChar(byte[] data, byte x, int start) {
        for (int i = start; i < data.length; i++) {
            if (data[i] == x) {
                return i;
            }
        }
        return -1;
    }

    public static int findFirstChar(String data, String x, int start) {
        int xLen = x.length() - 1;

        for (int i = start; i < data.length() - xLen; i++) {
            if (data.substring(i, i + xLen + 1).equals(x)) {
                return i;
            }
        }
        return -1;
    }
}
