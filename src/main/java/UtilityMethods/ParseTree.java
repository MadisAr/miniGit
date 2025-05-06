package UtilityMethods;

import Objects.DTO.TreeDTO;

import java.nio.charset.StandardCharsets;
import java.util.*;

// funktsioonid gitTreeObjekti decodimiseks ja loomiseks
// struktuur on nt byteid: 100644 path 0byte sha-1
// esimeses osas on info failityybi kohta
// siis on tyhik
// info pathi kohta
// null byte
// sha1 encoditud info

public class ParseTree {

    public static List<TreeDTO> parseTree(byte[] treeBytes) {
        // skipib yle esimesed X byte'i kus on kirjas objekti tyyp ja pikkus
        int position = 0;

        List<TreeDTO> returnList = new ArrayList<>();
        TreeDTO treeDTO;

        while ((treeDTO = decodeTreeSHA(treeBytes, position)) != null) {
            returnList.add(treeDTO);
            position += treeDTO.dataLength();
        }
        return returnList;
    }

    public static Comparator<TreeDTO> treeDTOComparator = Comparator.comparing(treeDTO -> {
        if (treeDTO.mode()[0] == (byte) 1 && treeDTO.mode()[1] == (byte) 0) {
            return new String(treeDTO.path());
        } else {
            return new String(treeDTO.path()) + "/";
        }
    });


    /**
     * loeb byte'id ja tagastab nendest saadud TreeDTO
     *
     * @param treeBytes antud byte array mgit Tree objektist
     * @return tagastab TreeDTO, kust saab infot mugavalt valja lugeda
     */
    public static TreeDTO decodeTreeSHA(byte[] treeBytes, int pos) {
        if (pos >= treeBytes.length) return null;

        int spaceInd = FindFirstChar.findFirstChar(treeBytes, (byte) ' ', pos);
        if (spaceInd == -1) return null;

        int nullInd = FindFirstChar.findFirstChar(treeBytes, (byte) 0, spaceInd);
        if (nullInd == -1) return null;

        String x =new String(treeBytes);
        assert (spaceInd - pos <= 7);

        byte[] modeBytes = Arrays.copyOfRange(treeBytes, pos, spaceInd);
        byte[] pathBytes = Arrays.copyOfRange(treeBytes, spaceInd + 1, nullInd);
        byte[] shaBytes = Arrays.copyOfRange(treeBytes, nullInd + 1, nullInd + 21);

        String shaString = HexFormat.of().formatHex(shaBytes);

        return new TreeDTO(modeBytes, pathBytes, shaString);
    }

    /**
     * Votab hetkel sisse numbri Stringi path ja SHA-1 byted ja teeb neist kokku yhe listi
     *
     * @param nr       info failityybi kohta
     * @param path     info pathi kohta
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
