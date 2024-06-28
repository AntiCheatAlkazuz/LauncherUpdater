package br.alkazuz.launcher.updater.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Checksum {

    public static String calculateMD5(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(is)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
