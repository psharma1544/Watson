package com.jps.nlp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static com.jps.nlp.Utils.isTxtFile;

class NLPTargets {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(App.class);
    private List<String> listUrls = new ArrayList<>();
    private List<String> listFiles = new ArrayList<>();
    private List<String> listHtmls = new ArrayList<>();
    private List<String> listPdfs = new ArrayList<>();

    private enum File_Extensions {TXT, HTML, HTM, PDF;}

    List<String> getListUrls() {
        return listUrls;
    }

    List<String> getListFiles() {
        return listFiles;
    }

    List<String> getListHtmls() {
        return listHtmls;
    }


    /**
     * Default constructor to populate values
     */

    NLPTargets() {
        logger.info("Populating lists of URLS, HTML pages, and local directory / text files to be traversed.");
        populateSecEdgarUrls();
        retrieveAndValidateLocalFiles(); // Requires files to be in a single folder
        retrieveAndValidateLocalFilesWithMetadata(); //
        addManualUrls();
        addManualHtmls();
        addManualFiles();
    }

    private void addManualUrls() {
        // Uncomment add() calls and copy in the URL / HTML / Local file (dir) that need to be traversed.
        //listUrls.add("");
        //listUrls.add("https://www.avanan.com/resources/what-is-shadow-it");
    }

    private void addManualFiles() {
        // Uncomment add() calls and copy in the URL / HTML / Local file (dir) that need to be traversed.
        //listFiles.add("");
    }

    private void addManualHtmls() {
        // Uncomment add() calls and copy in the URL / HTML / Local file (dir) that need to be traversed.
        //listHtmls.add("");

    }

    /**
     * Insert input file into appropriate file type queue based on file extension
     *
     * @param file
     */
    private void addFileBasedOnExtension(File file) {
        // Put in absolute path for file
        String fName = file.getAbsolutePath();
        int i = fName.lastIndexOf(".");
        String ext = "";
        if (i > 0) {
            try {
                ext = fName.substring(i + 1);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.getMessage();
            }
            if ((ext.equalsIgnoreCase(File_Extensions.HTM.toString())) || (ext.equalsIgnoreCase(File_Extensions.HTML.toString()))) {
                logger.info("Adding HTML page " + fName + " in the list for annotation checks.");
                listHtmls.add(fName);
            } else if (ext.equalsIgnoreCase(File_Extensions.PDF.toString())) {
                logger.info("Adding PDF file " + fName + " in the list for annotation checks.");
                listPdfs.add(fName);
            } else if (ext.equalsIgnoreCase(File_Extensions.TXT.toString())) {
                logger.info("Adding text file " + fName + " in the list for annotation checks.");
                listFiles.add(fName);
            } else
                logger.warn("Ignoring file / directory " + fName + " from annotation checks.");
        }
    }

    /**
     * Validates input. Checks for whether the input location even exists.
     * Checks for presence of *.txt files as those are the only supported ones for now.
     * Also checks whether a single file has been received instead of a directory.
     * Requires EDGAR data files to be in a single folder with flat file only structure.
     *
     * @return List of files found in given location and with given extension
     */

    private void retrieveAndValidateLocalFiles() {
        // Retrieve the directory path from configuration file

        String dirPath = Configuration.getLocalFiles();

        File dir = new File(dirPath);

        if (!dir.exists()) {
            logger.error("File / Directory not found: " + dirPath);
            logger.error("Please ensure that configuration file has correct directory path for local files.");
            logger.error("If files are on network share, then ensure that this server can access the location.");
            return;
        }
        logger.info("Generating list of local files to be traversed.");

        // Handle scenario where input is a directory
        if (dir.isDirectory()) {
            logger.info("Input directory: " + dirPath);
            File[] dirFilesListing = dir.listFiles();

            if ((dirFilesListing == null) || (dirFilesListing.length == 0)) {
                logger.warn("No files found in input directory: " + dirPath);
                logger.error("Please try again w/ a directory containing text files or a single text file!!");
            } else {
                // Dir has files. Check for presence of *.txt files.

                logger.info("Retrieving list of files.");
                logger.info("Program will ignore any non-txt extension files.");
                for (File file : dirFilesListing) {
                    addFileBasedOnExtension(file);
                }
            }
        }
        // Handle scenario where input is a single file
        else if (dir.isFile()) {
            addFileBasedOnExtension(dir);
        }
    }

    /**
     * <p>
     * Recursively goes through a directory that has SEC EDGAR filings
     * in a particular folder structure. The folder structure must have
     * symbol as the folder name and underneath should have folder by names
     * such as 10-K, 10-Q for individual filings for that symbol.
     * </p>
     *
     * <p>
     * Added to ensure that NoSQL can get stock symbol, filing type, file names
     * etc... so that user can search upon these additional artifacts.
     * </p>
     *
     * @return List of files found in given location in addition to symbol, filing type
     */

    private void retrieveAndValidateLocalFilesWithMetadata() {
        // Retrieve the directory path from configuration file
        String dirPath = Configuration.getLocalFiles();
        File dir = new File(dirPath);

        List<String> symbolsFilesAndMetaData = new ArrayList<>();

        if (!dir.exists()) {
            logger.error("File / Directory not found: " + dirPath);
            logger.error("Please ensure that configuration file has correct directory path for local files.");
            logger.error("If files are on network share, then ensure that this server can access the location.");
            return;
        }
        logger.info("Generating list of local files to be traversed.");

        if (dir.isDirectory()) {
            logger.debug("Input directory: " + dirPath);
            File[] dirFilesListing = dir.listFiles();

            if ((dirFilesListing == null) || (dirFilesListing.length == 0)) {
                logger.warn("No files found in input directory: " + dirPath);
                logger.error("Please try again w/ a directory containing text files or a single text file!!");

            } else {
                for (File dirSymbol : dirFilesListing) {
                    List<String> listTemp = recurseSymbolDirectory(dirSymbol.getAbsolutePath());
                    symbolsFilesAndMetaData.addAll(listTemp);
                }
            }
        }
        listFiles.addAll(symbolsFilesAndMetaData);

    }

    /**
     * @param pathDirSymbol Directory with name of a symbol containing SEC EDGAR filings
     * @return A list where each row has symbol, filing type, fileName
     */


    private List<String> recurseSymbolDirectory(String pathDirSymbol) {
        List<String> listFilesAndMetaData = new ArrayList<>();
        File dirSymbol = new File(pathDirSymbol);


        List<String> filingTypes = new ArrayList<>();
        filingTypes.add("10-K");
        filingTypes.add("10-Q");
        filingTypes.add("424B2");
        filingTypes.add("FWP");
        filingTypes.add("8-K");
        /*
            Following documents are required to be filed by a foreign issuer.
            These companies that are headquartered outside US dont file the traditional
            10-k, 10-s that local companies file.
            See this link for details: https://www.sec.gov/divisions/corpfin/internatl/foreign-private-issuers-overview.shtml
         */
        filingTypes.add("20-F");
        filingTypes.add("6-K");
        filingTypes.add("DFAN14A");

        if (dirSymbol.isDirectory()) {
            File[] dirFilesListing = dirSymbol.listFiles();

            Arrays.stream(dirFilesListing).forEach(dirFiling -> {
                if (filingTypes.contains(dirFiling.getName())) {
                    File[] filings = dirFiling.listFiles();
                    Arrays.stream(filings).forEach(file -> {
                        if (isTxtFile(file.getName(), "txt")) {
                            StringBuilder entry = new StringBuilder();
                            entry.append(dirSymbol.getName()).append(",").append(dirFiling.getName()).append(",").append(file.getAbsolutePath());
                            listFilesAndMetaData.add(entry.toString());
                        }

                    });
                }
            });
        }
        return listFilesAndMetaData;
    }

    /**
     * Populates URL links from SEC EDGAR filings returns.
     */
    private void populateSecEdgarUrls() {
        Map<String, Map<String, Integer>> mapFinReturns = Configuration.mapFinancialReturns;
        SecEdgar secEdgar = new SecEdgar();
        List<String> listTemp = secEdgar.retrieveLinks(mapFinReturns);

        if ((listTemp != null) && !listTemp.isEmpty()) {
            listUrls.addAll(listTemp);
        }
    }
}


