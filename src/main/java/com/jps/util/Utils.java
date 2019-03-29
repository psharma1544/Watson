package com.jps.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic utility functions.
 * Contents may differ to keep only functions that are required by this project.
 *
 * @author psharma
 */

public class Utils {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(Utils.class);

    public String getDate() {
        Date date = new Date();
        String strTime = null;

        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            strTime = format.format(date);
            //logger.debug("Current Date Time : " + strTime);
            if (strTime == null) {
                strTime = "-";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strTime;
    }


    public String getTimeOfDay() {
        Date date = new Date();
        String strTime = null;

        try {
            DateFormat format = new SimpleDateFormat("HH:mm:ss");
            strTime = format.format(date);
            //System.out.println("Current Date Time : " + strTime);
            if (strTime == null) {
                strTime = "-";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return strTime;
    }


    public String getDateAndTime() {
        Date date = new Date();
        String strTime = null;

        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd' T 'HH:mm:ss a z");
            strTime = format.format(date);
            //System.out.println("Current Date Time : " + strTime);
            if (strTime == null) {
                strTime = "-";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return strTime;
    }

    /**
     * Simple function to check whether
     * file extension matches w/ what is expected.
     *
     * @param fileName
     * @return
     */

    public static boolean isTxtFile(String fileName, String extPattern) {
        boolean retValue = false;
        String ext = "";
        int i = fileName.lastIndexOf(".");
        if (i > 0) {
            ext = fileName.substring(i + 1);
            if (ext.equalsIgnoreCase(extPattern))
                retValue = true;
        }
        return retValue;
    }

    /**
     * Retrieves file name given absolute or relative path to a file
     *
     * @param path Absolute or relative path that must include file name
     * @return
     */
    public String parseFileNameFromPath(String path) throws ArrayIndexOutOfBoundsException {
        String fileName = "";
        String sep = File.separator;
        String[] upstream = path.split(sep);
        int size = upstream.length;
        if (size == 0) {
            logger.warn("Could not parse path '%s' to identify final file name.", path);
            return "";
        }
        try {
            // Unlikely, but account for scenario where a trailing separator was put in after a file name
            fileName = upstream[size - 1];
            if (fileName == "")
                fileName = upstream[size - 2];
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.warn("Could not parse path '%s' to identify final file name.", path);
            return "";
        }
        return fileName;
    }

    /**
     * Downloads a file from given URL giving it localFileName
     */
    public static void downloadFromUrl(String urlPath, String localFilename) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;


        try {
            URL url = new URL(urlPath);
            URLConnection urlConn = url.openConnection();//connect

            is = urlConn.getInputStream();
            fos = new FileOutputStream(localFilename);

            byte[] buffer = new byte[4096];              //declare 4KB buffer
            int len;

            // Download and write locally, while input stream has data
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }
}




















