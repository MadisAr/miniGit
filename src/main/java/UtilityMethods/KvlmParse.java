package UtilityMethods;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class KvlmParse {
    /**
     * see on meetod millega saab töödelda näiteks commit message'eid
     * commit messageites võib olla ka pgp signatuur aga ma praegu mõtlen, et meil ei ole seda vaja
     * kui tahame saame hiljem lisada
     *
     * @param rawData etteantud andmed
     * @return tagastab mapi antud datast
     */
    public static Map<String, String> KvlmParse(byte[] rawData) {
        String data = new String(rawData, StandardCharsets.UTF_8);
        data = data.split(" ", 2)[1];
        Map<String, String> dataMap = new LinkedHashMap<>();
        String[] dataList = data.split("\n");

        // NB! esimesed kolm rida commitis on tree, parent ja timestamp (author ja commiter ei ole!, muidu on nende sees timestamp), lisame loopiga
        for (int i = 0; i < 3; i++) {
            String[] entry = dataList[i].split(" ", 2);
            dataMap.put(entry[0], entry[1]);
        }

        // ylejaanud peale yhte rida on commit message
        dataMap.put("message", String.join("\n", Arrays.copyOfRange(dataList, 4, dataList.length)));
        return dataMap;
    }

    public static byte[] KvlmUnParse(Map<String, String> data) {
        StringBuilder dataString = new StringBuilder();

        String[] keys = data.keySet().toArray(new String[0]);
        String[] vals = data.values().toArray(new String[0]);
        for (int i = 0; i < 4; i++) {
            dataString.append(keys[i]).append(" ").append(vals[i]).append("\n");
        }
        dataString.append("\n").append(vals[vals.length - 1]);

        return dataString.toString().getBytes(StandardCharsets.UTF_8);
    }
}
