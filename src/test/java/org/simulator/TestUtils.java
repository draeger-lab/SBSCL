package org.simulator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang3.ArrayUtils;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Helper functions for tests.
 */
public class TestUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    public static String FBA_RESOURCE_PATH = "/fba";

    /**
     * Returns location of BiGG test model directory from environment variable.
     */
    public static String getBiGGModelPath() {
        Map<String, String> env = System.getenv();
        String key = "BIGG_MODELS";
        String value = null;
        if (env.containsKey(key)) {
            value = env.get(key);
            logger.info(String.format("BiGG models folder found: %s", value));
        }
        else {
            logger.info(String.format("%s environment variable not set.", key));
        }
        return value;
    }


    /**
     * Get an iteratable over the resources in the resourcePath.
     * <p>
     * Resources in the skip set are skipped.
     * If a filter string is given only the resources matching the filter are returned.
     */
    public static Iterable<Object[]> findResources(String resourcePath, String extension, String filter, HashSet<String> skip) {

        File currentDir = new File(System.getProperty("user.dir"));
        // String rootPath = new File(currentDir, resourcePath).getPath();
        String rootPath = currentDir.getAbsolutePath() + "/src/test/resources" + resourcePath;

        System.out.println("curDir:" + currentDir);
        System.out.println("rootPath:" + rootPath);

        // Get SBML files for passed tests
        LinkedList<String> sbmlPaths = TestUtils.findFiles(rootPath, extension, filter, skip);
        Collections.sort(sbmlPaths);

        int N = sbmlPaths.size();
        System.out.println("Number of resources: " + N);
        Object[][] resources = new String[N][1];
        for (int k = 0; k < N; k++) {
            String path = sbmlPaths.get(k);
            // create the resource
            String[] items = path.split("/");
            int mindex = -1;
            for (int i = 0; i < items.length; i++) {
                if (items[i].equals("models")) {
                    mindex = i;
                    break;
                }
            }
            String resource = StringUtils.join(ArrayUtils.subarray(items, mindex, items.length), "/");
            resources[k][0] = "/" + resource;
        }
        return Arrays.asList(resources);
    }


    /**
     * Search recursively for all SBML files in given path.
     * SBML files have to end in ".xml" and pass the filter expression
     * and is not in the skip set.
     */
    public static LinkedList<String> findFiles(String path, String extension, String filter, HashSet<String> skip) {
        LinkedList<String> fileList = new LinkedList<>();

        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return fileList;
        }
        if (skip == null) {
            skip = new HashSet<>();
        }

        for (File f : list) {
            String fpath = f.getAbsolutePath();
            // recursively search directories
            if (f.isDirectory()) {
                fileList.addAll(findFiles(fpath, extension, filter, skip));
            } else {
                String fname = f.getName();
                if (fname.endsWith(extension) && !skip.contains(fname)) {
                    // no filter add
                    if (filter == null) {
                        fileList.add(fpath);
                    } else {
                        // filter matches add
                        Pattern pattern = Pattern.compile(filter);
                        Matcher m = pattern.matcher(fname);
                        if (m.find()) {
                            fileList.add(fpath);
                        }
                    }
                }
            }
        }
        return fileList;
    }

    public static LinkedList<String> findFiles(String path, String extension) {
        return findFiles(path, extension, null, null);
    }
}
