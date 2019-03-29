package com.jps.nlp;

import com.jaunt.*;
import com.jaunt.component.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author psharma
 */
class SecEdgarDataImpl implements SecEdgarData {

    private final Logger logger = LoggerFactory.getLogger(SecEdgarDataImpl.class);
    private final String baseEdgarUrl = "https://www.sec.gov/edgar/searchedgar/companysearch.html";
    private final String BASE_SYMBOLS_DIR_PATH = "C:\\Users\\pshar\\Dropbox\\Programming\\SampleTexts\\FilingsBySymbols";

    private final HTML2Txt html2Txt = new HTML2Txt();

    HttpGetExecutor httpGet = new HttpGetExecutor();

    /**
     * Retrieves URL of SEC EDGAR link for a given public company traded in US markets.
     *
     * @param symbol Symbol of a public company listed on NYSE / NASDAQ
     * @return URL where user can search returns of a public company
     */

    @Override
    public String urlBaseEdgar(String symbol) {
        String baseCompanyUrl = "";
        try {
            UserAgent userAgent = new UserAgent();         //create new userAgent (headless browser)
            userAgent.settings.checkSSLCerts = false;

            /*
             * Start with the base page where you can enter either company name or symbol and then
             * locate the text field that asks for symbol name and fill it in with the symbol we are retrieving data for.
             * The URL that we get for that document is the one to be returned.
             */

            userAgent.visit(baseEdgarUrl);
            userAgent.doc.filloutField("Ticker or CIK", symbol);
            String elementQuery = "<form id=\"fast-search\"";
            Form form = userAgent.doc.getForm(elementQuery);
            Document newDoc = form.submit();
            baseCompanyUrl = newDoc.getUrl();
        } catch (ExpirationException e) {         //if an HTTP/connection error occurs, handle JauntException.
            logger.warn("The web scraping Jaunt API has expired: " + e.getMessage());
            e.printStackTrace();
        } catch (JauntException e) {         //if an HTTP/connection error occurs, handle JauntException.
            logger.warn("JauntException: " + e.getMessage());
            e.printStackTrace();
        }
        return baseCompanyUrl;
    }

    /**
     * Retrieves base URL for a particular filing type (10-Q, 10-K) for a given public company.
     * This URL will in turn have several other URLs in the web page each of which will have
     * text for a single document filed with SEC.
     *
     * @param symbol           Symbol of a public company listed on NYSE / NASDAQ
     * @param baseCompanyUrl
     * @param typeFinancialDoc ~
     * @return URL containing a listing of returns of a single type for a single public company
     */

    @Override
    public String urlFilingTypeEdgar(String symbol, String baseCompanyUrl, String typeFinancialDoc) {
        String baseUrlCompanyFilingType = "";

        try {
            UserAgent userAgent = new UserAgent();         //create new userAgent (headless browser)
            userAgent.settings.checkSSLCerts = false;

            // TODO: Handle the case where symbol is incorrect so we don't end up failing later on.
            logger.info("Retrieving base EDGAR web page to retrieve '" + typeFinancialDoc + "' for " + symbol + " from: " + baseCompanyUrl);

            try {
                userAgent.visit(baseCompanyUrl);
                userAgent.doc.filloutField("Filing Type:", typeFinancialDoc);
                Document newDoc = userAgent.doc.submit("Search");
                if (newDoc != null) {
                    baseUrlCompanyFilingType = newDoc.getUrl();
                    logger.info("The " + typeFinancialDoc + " documents for company symbol " + symbol + " are available at: " + baseUrlCompanyFilingType);
                } else {
                    logger.error("Could not retrieve financial documents for symbol \"" + symbol + "\"");
                }
            } catch (NotFound e) {
                logger.error("Could not retrieve financial documents for symbol \"" + symbol + " with URL: \n" + baseCompanyUrl + "\n");
                logger.error("Enter '" + symbol + "' under textbox 'Fast Search'  at URL 'https://www.sec.gov/edgar/searchedgar/companysearch.html' and "
                        + "validate whether SEC provides data for this company.");
            }
        } catch (JauntException e) {         //if an HTTP/connection error occurs, handle JauntException.
            logger.warn("JauntException: " + e.getMessage());
            e.printStackTrace();
        }
        return baseUrlCompanyFilingType;
    }

    /**
     * Generates a listing where each entry contains URL of a specific type of financial document for a given company.
     *
     * @param baseUrlCompanyFilingType URL of the base EDGAR page for a public company's financial documents
     * @param numDocs                  Number of documents (10-k, 10-q) to be retrieved.
     * @param retrievalType            Pattern to be searched for in the page <\br>
     *                                 Must be either "Documents" or "Interactive"
     * @return A list containing individual URLs for financial data for each document.
     */
    @Override
    public List<String> urlsSpecificSingleFilingEdgarPage(String baseUrlCompanyFilingType, int numDocs, String retrievalType) {
        UserAgent userAgent = new UserAgent();         //create new userAgent (headless browser)
        userAgent.settings.checkSSLCerts = false;

        /*
         * We are already on a page (passed in argument) that has HTML links for kind of data (say 10-k, 10-q)  that was requested.
         * We now look for URL links for the end HTML page we are interested in.
         * Number of individual 10-k, 10-Q instances to return data for. 1 instance means data for one quarter
         * or one year will be returned.  These are sorted at SEC site and within this program, so latest is (are) guaranteed. Just
         * preserve the order in a linked list.
         */
        List<String> urlsListingFiliingType = new LinkedList<String>();
        String tag = "<a>";
        String searchPattern = "";

        if (retrievalType.equalsIgnoreCase("Documents"))
            searchPattern = "&nbsp;Documents";
            // We may get "Interactive Data" or just "interactive" in upper / lower case. Need to handle all so somewhat complicated here...
        else if (Pattern.compile(Pattern.quote("Interactive"), Pattern.CASE_INSENSITIVE).matcher(retrievalType).find())
            searchPattern = "&nbsp;Interactive Data";

        int num = 0;
        try {
            userAgent.visit(baseUrlCompanyFilingType);
            Elements urls = userAgent.doc.findEvery(tag);

            //Iterate through all URLs and get value for href attribute in a list.
            for (Element element : urls) {
                if (((element.getTextContent()).contains(searchPattern))) {
                    String temp = element.getAt("href");
                    if (num < numDocs) {
                        ++num;
                        //logger.info("Retrieving "+testFinancialDocumentType.toUpperCase()+ "document(s) for symbol "+symbol+ " with URL:  "+temp);
                        logger.info("All records for this filing are available at: " + temp);
                        urlsListingFiliingType.add(temp);
                    } else
                        break; // Come out as soon as condition is satisfied. No need to loop further through the <a> links.
                }
            }
        } catch (JauntException e) {
            logger.warn("JauntException: " + e.getMessage());
            e.printStackTrace();
        }
        return urlsListingFiliingType;
    }

    /**
     * Generates the URL for a specific return.
     * <p> As an example, this can return the URL of symbol "ABC" for 10-K for year 2017.
     *
     * @param urlFiliingType
     * @param filingType
     * @return
     */
    @Override
    public String urlSingleReturnDetailsEdgar(String urlFiliingType, String filingType) {
        String urlSingleFiling = "";
        String tagTableRecord = "<tr>";

        UserAgent userAgent = new UserAgent();         //create new userAgent (headless browser)
        userAgent.settings.checkSSLCerts = false;
        try {
            userAgent.visit(urlFiliingType);
            // Locate the table that has the data that we need
            Element tblElement = userAgent.doc.findFirst("<table summary=\"Document Format Files\">");
            //logger.info(tblElement.getTextContent());
            // Retrieve all table records for our table
            Elements tableRecords = tblElement.findEvery(tagTableRecord);

            // Now traverse each of these TRs to get to the table data record that we need
            for (Element tr : tableRecords) {
                //logger.debug(tr.getTextContent());
                // The 4th element (start from 0) has information for the type of document like: 10-Q, 10-K. Get that first.
                Element tdFilingType = tr.getElement(3);
                // Does it match what we are looking for?
                String text = tdFilingType.getTextContent().trim();
                // If yes, then get the URL which is in 3rd (start from 0) TD element.
                if (text.equalsIgnoreCase(filingType.trim())) {
                    Element tdLink = tr.getElement(2);
                    Element hrefElmnt = tdLink.findFirst("<a>");
                    urlSingleFiling = hrefElmnt.getAt("href");
                    logger.debug("Text for filing is available at: " + urlSingleFiling);

                }
            }
        } catch (NotFound e) {
            logger.warn("Jaunt NotFound Exception: " + e.getMessage());
        } catch (JauntException e) {
            logger.error("JauntException: " + e.getMessage());
            e.printStackTrace();
        }

        return urlSingleFiling;
    }

    /**
     * TODO: Implement this function to download the text
     * of a web page for NLP APIs that can not handle web links.
     *
     * @param docType
     * @param path
     * @return
     */
    String dnldUrlSpecificSingleFilingEdgar(String docType, String path) {
        String retString = "";
        return retString;
    }

    /**
     * Download Sec Filings in given location.
     * Puts in files in directory structure {path}\<symbol>\<filingType>\<file.htm>
     */

    boolean downloadSecFilings(String urlLink, String symbol, String typeFinancialDoc) {


        try {
            String name = "";
            String dirSymbolPath = "";
            String dirFilingTypePath = "";
            String parentPath = "";
            boolean dirCreated = false;

            // Make directory. Java checks if dir already exists, don't need a check ourselves.
            dirCreated = new File(BASE_SYMBOLS_DIR_PATH + File.separator + symbol + File.separator + typeFinancialDoc).mkdirs();

            File dirFilingType = new File(BASE_SYMBOLS_DIR_PATH + File.separator + symbol + File.separator + typeFinancialDoc);
            if (dirFilingType.exists()) {
                parentPath = dirFilingType.getAbsolutePath();
                logger.debug("Using folder '{}' to download {} returns for {}.", dirFilingType.getAbsolutePath(), typeFinancialDoc, symbol);
            } else {
                logger.warn("Could not create / find directory to save return types {} for symbol {}: ", typeFinancialDoc, symbol);
                return false;
            }

            // We have the parent path. Now identify a unique name of the file which will be used to download it.
            int i = urlLink.lastIndexOf("/");
            if (i > 0) {
                //String lstButOneIndexValue = urlLink.substring(i);
                String lstIndexValue = urlLink.substring(i + 1);
                //name = lstButOneIndexValue + "_" + lstIndexValue;
                name = lstIndexValue;
                if (Configuration.PURGE_HTM_EXTENSION){
                    logger.debug("Applying regex on file names.");
                    name = name
                            .replaceAll("\\.", "")
                            .replaceAll("-", "");
                            //.replaceAll("htm", "");
                }
            }
            String localFilename = parentPath + File.separator + name;
            Utils.downloadFromUrl(urlLink, localFilename);

            /*
            // Copy the contents to text as well.
            // TODO: Enable an option and copy to text only if that is enabled
            String nameWithoutExt = "";
            String[] arrSplitName = name.split("\\.");
            if (arrSplitName.length > 0)
                nameWithoutExt = arrSplitName[0];

            if (nameWithoutExt != "") {
                nameWithoutExt = nameWithoutExt.concat(".txt");
                String txtFromHtml = html2Txt.dnldText(urlLink);
                String localTxtFilename = parentPath + File.separator + nameWithoutExt;
                PrintWriter writer = new PrintWriter(localTxtFilename, "UTF-8");
                writer.println(txtFromHtml);
                writer.close();
                //System.out.println(txtFromHtml);
            }
            */
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("ArrayIndexOutOfBoundsException while downloading financial returns." + e.getMessage());
        } catch (IOException e) {
            logger.error("IOException while downloading financial returns." + e.getMessage());
        }
        // TODO: Need to have more checks to return true here.
        return true;
    }
}
