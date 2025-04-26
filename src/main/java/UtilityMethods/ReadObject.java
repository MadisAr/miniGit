package UtilityMethods;

import Objects.MGitObjects.BlobObject;
//import Objects.MGitObjects.CommitObject;
import Objects.MGitObjects.MGitObject;
import Objects.MiniGitRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.InflaterInputStream;

import static UtilityMethods.CreateGitSubdirectories.repoFile;
import static UtilityMethods.FindFirstChar.findFirstChar;

public class ReadObject {
    public static MGitObject ReadObject(MiniGitRepository miniGitRepository, String sha) throws IOException {
        // teeme sha alamkaustad
        File repoFile = new File(miniGitRepository.getGitDir());
        File shaFile = repoFile(repoFile.toPath(), "objects", sha.substring(0, 2), sha.substring(2));

        if (!Files.exists(shaFile.toPath())) {
            System.out.println(shaFile.getAbsolutePath());
            return null;
        }

        byte[] decompressedBytes = decompress(Files.readAllBytes(shaFile.toPath()));

        // loe baidireast objekti tyyp/format
        int spaceIndex = findFirstChar(decompressedBytes, (byte) ' ', 0);
        String format = new String(decompressedBytes, 0, spaceIndex, StandardCharsets.UTF_8);

        // loeme baidireast objekti pikkuse ja valideerime selle
        int nullIndex = findFirstChar(decompressedBytes, (byte) 0, spaceIndex + 1);

        int size = Integer.parseInt(new String(decompressedBytes, spaceIndex + 1, nullIndex - spaceIndex - 1, StandardCharsets.UTF_8));

        // valideerime pikkuse, vorreldes sha header vaartust ja tegelikku content valja pikkust
        byte[] content = new byte[decompressedBytes.length - nullIndex - 1];
        System.arraycopy(decompressedBytes, nullIndex + 1, content, 0, content.length);

        if (size != content.length) {
            throw new RuntimeException("Error: sha object " + sha + " header size doesn't match the actual length!");
        }
        System.out.println("format: " + format);
        System.out.println("size: " + size);
        System.out.println("content: " + new String(content, StandardCharsets.US_ASCII));

        String data = size + " " + new String(content, StandardCharsets.US_ASCII);
        switch (format) {
            case "blob":
                return new BlobObject(data);
//            case "commit":
//                return new CommitObject(data);
        }

        return null;
    }

    public static byte[] decompress(byte[] data) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             InflaterInputStream iis = new InflaterInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = iis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }


}
