package UtilityMethods;

import Objects.MGitObjects.MGitObject;
import Objects.MiniGitRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterInputStream;

import static UtilityMethods.CreateGitSubdirectories.*;

public class WriteObject {
    /**
     * meetod mGitObjecti krypteeritud sha1'ks muutimiseks
     *
     * @param miniGitRepository minigitObject
     * @param mGitObject mGitObject
     * @return tagastab krypteeritud stringi
     * @throws IOException byteArrayOutputStreami exception
     */
    public static String writeObject(MiniGitRepository miniGitRepository, MGitObject mGitObject) throws IOException, NoSuchAlgorithmException {
        String data = mGitObject.serialize(miniGitRepository);

        // teeme byteArrayOutputStream ja lisame sinna jäjrest byte'e
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            result.write(mGitObject.getFormat().getBytes(StandardCharsets.UTF_8));
            result.write(' ');
            result.write(Integer.toString(data.length()).getBytes(StandardCharsets.UTF_8));
            result.write((byte) 0);
            result.write(data.getBytes(StandardCharsets.UTF_8));


            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hashedBytes = sha1.digest(result.toByteArray());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            String sha = sb.toString();

            //TODO tegelt ei taha kirjutada ikka?
//         teeme antud andmetest uued alamkaustad
            File repoFile = miniGitRepository.getGitDir().toFile();
            File file = createGitDirsAndFile(repoFile,"objects", sha.substring(0, 2), sha.substring(2));

            byte[] resultBytes = compress(result.toByteArray());

//            if (!file.exists()) {
//                try (FileOutputStream fos = new FileOutputStream(file)) {
//                    fos.write(resultBytes);
//                }
//            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(resultBytes);
            }
            return sha;
        }
    }

    public static byte[] compress(byte[] data) throws IOException {
        try (ByteArrayInputStream byteArrayInputStream =  new ByteArrayInputStream(data);
        DeflaterInputStream deflaterInputStream = new DeflaterInputStream(byteArrayInputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int len;

            while ((len = deflaterInputStream.read(buffer)) > 0) {
               byteArrayOutputStream.write(buffer, 0, len);
            }

            return byteArrayOutputStream.toByteArray();
        }
    }
}
