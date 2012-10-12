package com.gauronit.tagmata.util;

import java.io.File;

public class IOUtil {

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        dir.deleteOnExit();
        return dir.delete();
    }
}
