package UtilityMethods;

import Objects.TreeDTO;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

// funktsioonid gitTreeObjekti decodimiseks ja loomiseks
// struktuur on nt byteid: 100644 path 0byte sha-1
// esimeses osas on info failityybi kohta
// siis on tyhik
// info pathi kohta
// null byte
// sha1 encoditud info
public class ParseTree {
    /**
     * loeb byte'id ja tagastab nendest saadud TreeDTO
     *
     * @param treeBytes antud byte array mgit Tree objektist
     * @return tagastab TreeDTO, kust saab infot mugavalt valja lugeda
     */
    public static TreeDTO decodeTreeSHA(byte[] treeBytes) {
        int spaceInd = FindFirstChar.findFirstChar(treeBytes, (byte) ' ', 0);
        int nullInd = FindFirstChar.findFirstChar(treeBytes, (byte) 0, spaceInd);

        if (nullInd == -1 || spaceInd == -1) return null;
        byte[] modeBytes = Arrays.copyOfRange(treeBytes, 0, spaceInd);
        byte[] pathBytes = Arrays.copyOfRange(treeBytes, spaceInd, nullInd);
        byte[] shaBytes = Arrays.copyOfRange(treeBytes, nullInd, treeBytes.length);

        return new TreeDTO(modeBytes, pathBytes, shaBytes);
    }

    /**
     * Votab hetkel sisse numbri Stringi path ja SHA-1 byted ja teeb neist kokku yhe listi
     *
     * @param nr info failityybi kohta
     * @param path info pathi kohta
     * @param shaBytes krypteeritud sha byte'id
     * @return tagastab byte array
     */
    public static byte[] encodeTreeSHA(int nr, String path, byte[] shaBytes) {
        byte[] nrBytes = Integer.toString(nr).getBytes(StandardCharsets.UTF_8);
        byte[] pathBytes = path.getBytes(StandardCharsets.UTF_8);

        byte[] returnable = new byte[nrBytes.length + pathBytes.length + shaBytes.length + 2];

        int pos = 0;
        System.arraycopy(nrBytes, 0, returnable, pos, nrBytes.length);
        pos += nrBytes.length;
        returnable[pos++] = ' ';
        System.arraycopy(pathBytes, 0, returnable, pos, pathBytes.length);
        pos += pathBytes.length;
        returnable[pos++] = 0;
        System.arraycopy(shaBytes, 0, returnable, pos, shaBytes.length);

        return returnable;
    }
}
