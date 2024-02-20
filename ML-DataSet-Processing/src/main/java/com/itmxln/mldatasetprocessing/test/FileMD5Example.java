package com.itmxln.mldatasetprocessing.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class FileMD5Example {
    public static String getFileMD5(String filePath) {
        try {
            File file = new File(filePath);
            long totalBytes = file.length();
            long bytesRead = 0;

            InputStream fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] dataBytes = new byte[4096];

            int nread;
            while ((nread = fis.read(dataBytes)) != -1) {
                bytesRead += nread;
                md.update(dataBytes, 0, nread);
                double progressPercentage = (double) bytesRead / totalBytes * 100;
                updateProgress(progressPercentage);
            }
            fis.close();

            byte[] mdbytes = md.digest();

            // Convert the byte to hex format
            StringBuilder sb = new StringBuilder();
            for (byte b : mdbytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void updateProgress(double progressPercentage) {
        final int width = 50; // progress bar width in chars

        System.out.print("\r[");
        int i = 0;
        for (; i <= (int)(progressPercentage * width / 100); i++) {
            System.out.print(".");
        }
        for (; i < width; i++) {
            System.out.print(" ");
        }
        System.out.print("] " + String.format("%.2f", progressPercentage) + "%");
    }


    public static void main(String[] args) {
        String filePath = "D:\\IDM\\Compressed\\Monday-WorkingHours.pcap";
        System.out.println("Calculating MD5 hash of the file: " + filePath);
        String md5Hash = getFileMD5(filePath);
        System.out.println("\nMD5 hash of the file: " + md5Hash);
    }
}
