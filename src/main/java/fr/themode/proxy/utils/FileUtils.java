package fr.themode.proxy.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {

    public static String readFile(File file) {
        String fileString = null;
        try {
            fileString = Files.readString(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileString;
    }
}
