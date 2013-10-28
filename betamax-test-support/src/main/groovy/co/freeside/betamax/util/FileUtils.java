package co.freeside.betamax.util;

import java.io.*;

public class FileUtils {

    private static final File TMP = new File(System.getProperties().getProperty("java.io.tmpdir"));

    @Deprecated
    public static File newTempDir(String name) {
        File dir = new File(TMP, name);
        dir.mkdirs();
        return dir;
    }

    private FileUtils() {
    }
}
