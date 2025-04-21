package UtilityMethods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KvlmParse {
    /**
     * see on meetod millega saab töödelda näiteks commit message'eid
     * commit messageites võib olla ka pgp signatuur aga ma praegu mõtlen, et meil ei ole seda vaja
     * kui tahame saame hiljem lisada
     *
     * @param data etteantud andmed
     * @return tagastab mapi antud datast
     */
    public static Map<String, String> KvlmParse(String data) {
        Map<String, String> dataMap = new HashMap<>();
        String[] dataList = data.split("\n");

        //esimesed neli rida commitis on tree, parent author ja commiter, mis lisame loopiga
        for (int i = 0; i < 4; i++) {
            System.out.println(dataList[i]);
            String[] entry = dataList[i].split(" ", 2);
            dataMap.put(entry[0], entry[1]);
        }

        // ylejaanud peale yhte rida on commit message
        dataMap.put("message", String.join("\n", Arrays.copyOfRange(dataList, 5, dataList.length)));
        return dataMap;
    }
}
