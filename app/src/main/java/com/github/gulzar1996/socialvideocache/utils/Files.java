package com.github.gulzar1996.socialvideocache.utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by gulza on 12/7/2017.
 */

public class Files {

    static void makeDir(File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                throw new IOException("File " + directory + " is not directory!");
            }
        } else {
            boolean isCreated = directory.mkdirs();
            if (!isCreated) {
                throw new IOException(String.format("Directory %s can't be created", directory.getAbsolutePath()));
            }
        }
    }
}
