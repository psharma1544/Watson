package com.jps.search;

import com.jps.util.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.io.FileUtils.getFile;

public class SolrConnectImpl {

    private final Logger logger = (Logger) LoggerFactory.getLogger(SolrConnectImpl.class);
    private final String TGT_DIRECTORY = "C:\\Users\\pshar\\Dropbox\\Programming\\SampleTexts\\NLP_Output";

    /**
     * Sets up and returns Solr client for
     * indexing and other operations later
     * on.
     *
     * @return Solr client
     */
    public HttpSolrClient getSolrClient(String collection) {
        String SOLR_BASE_URL = "http://localhost:8984/solr";
        final String solrUrlMarketsCollection = SOLR_BASE_URL + "/" + collection;
        return new HttpSolrClient.Builder(solrUrlMarketsCollection)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }

    public void closeSolrClient(HttpSolrClient client) throws IOException, SolrServerException {
        client.close();
    }

    /**
     * @throws IOException
     * @throws SolrServerException
     */

    public void cleanAllIndexes(HttpSolrClient client) throws IOException, SolrServerException {
        client.deleteByQuery("*:*");// delete everything!
        client.commit();
    }

    /**
     * @param dirPath
     * @param fields
     * @param client
     * @throws IOException
     * @throws SolrServerException
     */
    public void indexCsvByRow(HttpSolrClient client, String dirPath, String patternTgtFiles, List<String> fields) throws IOException, SolrServerException {

        File dir = new File(dirPath);
        if (dirPath == "")
            dir = new File(TGT_DIRECTORY);

        if (dir.isDirectory()) {
            File[] tgtFiles = dir.listFiles();

            assert tgtFiles != null;
            for (File file : tgtFiles) {
                Pattern p = Pattern.compile(patternTgtFiles, Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(file.getName());
                if (!m.find()) {
                    logger.debug("Skipping file w/ pattern mis-match: " + file.getName());
                    continue;
                }

            /*
                Check if the file is already in our index.
                If so, then don't proceed with indexing.
             */

            // TODO: Re-enable following:
            /*
                2019-03-24: Disabling this check. The returns from this are turning out to be
                positive even if I clear the index and purge the collection completely to the best
                of my knowledge.


                try {
                    String extension = "";
                    boolean fileIndexed = queryCheckFileIndexed(client, file.getName(), extension);
                    if (fileIndexed) {
                        logger.warn("File {} already indexed. Skipping...", file.getName());
                        continue;
                    }
                } catch (IOException | SolrServerException e) {
                    e.printStackTrace();
                }
*/

                logger.info("Indexing file :{}", file.getName());
                List<SolrInputDocument> docs = new ArrayList<>();
                List<String> inputList = modReadFile.readFileIntoListOfStrings(file.getAbsolutePath());
                if ((inputList == null) || (inputList.size() == 0)) {
                    logger.error("File \"" + file + "\" does not have any rows to be inserted into the database.");
                }
                //logger.debug("Indexing file: " + file);
                //logger.debug("The size of the returned list is:" + inputList.size());

                for (String line : inputList) {
                    String[] temp = line.split(",");
                    int[] count = {0};
                    SolrInputDocument doc = new SolrInputDocument();
                    doc.addField("id", UUID.randomUUID().toString());
                    fields.forEach(field -> {
                        try {
                            doc.addField(field, temp[count[0]++]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            logger.error("Exception {} while processing line {} in file {}", e.getMessage(), line, file.getName());
                            e.printStackTrace();
                        }
                    });
                    docs.add(doc);
                }
                client.add(docs);
                client.commit();
            }
        }

    }

    /**
     * @throws IOException
     * @throws SolrServerException
     */

    /*
        2019-02-17: Had added this function at the very start.
        Not in use. The thought was to read the entire CSV
        file as a document rather than line by line as I
        eventually did.
     */
    public void genDocumentIndexing(HttpSolrClient client, String dirPath) throws IOException, SolrServerException {

        File dir = new File(dirPath);


        if (dirPath.equals(""))
            dir = new File(TGT_DIRECTORY);
        if (dir.isDirectory()) {
            File[] tgtFiles = dir.listFiles();
            List<SolrInputDocument> docs = new ArrayList<>();
            assert tgtFiles != null;
            for (File file : tgtFiles) {
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", UUID.randomUUID().toString());
                // TODO: Change the name to be in the form SMBL_QRTR_TYPE_FILENAME
                doc.addField("name", file.getName());
                // TODO: Need to have functions to create and destroy collections
                doc.addField("timestamp_dt", new java.util.Date());
                docs.add(doc);
            }
            client.add(docs);
            client.commit();
        }

        client.close();

    }

    /**
     * @throws IOException
     * @throws SolrServerException
     */

    /*
        Wrote in the initial stages.
        May not be functional.
     */
    public void genStreamIndexing(HttpSolrClient client, String dirPath) throws IOException, SolrServerException {

        File dir = new File(dirPath);
        //final SolrClient client = getSolrClient();


        if (dirPath.toString() == "")
            dir = new File(TGT_DIRECTORY);
        if (dir.isDirectory()) {
            File[] tgtFiles = dir.listFiles();
            for (File file : tgtFiles) {
                logger.info("Indexing file: " + file);
                ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");
                up.addFile(getFile(file.getAbsolutePath()), "application/csv");
                up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
            }
        }
    }

    /**
     * Queries Solr using basic Lucene based search
     *
     * @param client           Solr Client
     * @param maxCount         MAx number of search results to be accounted for
     * @param patternFileNames REGEX for fileName
     * @throws IOException         throws IO Exception
     * @throws SolrServerException throws Solr Exception
     */
    void queryLucene(HttpSolrClient client, long maxCount, String patternFileNames) throws IOException, SolrServerException {
        int batch = 50;
        int offset = 0;

        SolrQuery query = new SolrQuery();
        query.setParam("debug", false);
        query.setParam("defType", "lucene"); // Other choices are dismax, ...

        query.setRows(batch);

        // TODO: These parameters need to be passed ar arguments by caller as a map
        String queryFilter = "+symbol:amzn +pos:NNP +fileName:" + patternFileNames;
        query.setQuery("*:*");
        query.setFields("symbol, filing, lemma, pos, fileName");
        //query.set("fl", "lemma, pos, fileName");

        while (offset < maxCount) {
            query.setStart(offset); // Use combination of start and rows for pagination.
            query.setFilterQueries(queryFilter);
            QueryResponse response = client.query(query);
            SolrDocumentList documents = response.getResults();

            //logger.info("Found " + documents.getNumFound() + " documents");
            //logger.info("Showing " + maxCount + " documents");

            for (SolrDocument document : documents) {
                String lemma = (String) document.getFirstValue("lemma");
                String pos = (String) document.getFirstValue("pos");
                String fileName = (String) document.getFirstValue("fileName");
                logger.info("Lemma: " + lemma + "; pos: " + pos + "; FileName: " + fileName);
            }
            offset += batch;
        }
    }

    /**
     * Queries Solr using Dismax based search
     *
     * @param client           Solr Client
     * @param maxCount         MAx number of search results to be accounted for
     * @param patternFileNames REGEX for fileName
     * @throws IOException         throws IO Exception
     * @throws SolrServerException throws Solr Exception
     */
    void queryDismax(HttpSolrClient client, long maxCount, String patternFileNames) throws IOException, SolrServerException {
        int batch = 50;
        int offset = 0;

        SolrQuery query = new SolrQuery();
        query.setParam("debug", false);
        query.setParam("defType", "dismax"); // Other choices are dismax, ...

        query.setRows(batch);

        query.setParam("q", "*debt*");
        String queryFilter = "+pos:NNP +pos:NNPS +fileName:" + patternFileNames;
        query.setQuery("*debt*");
        //query.setFields("symbol, filing, lemma, pos, fileName");
        query.setFields("*, score");
        //query.set("fl", "lemma, pos, fileName");

        while (offset < maxCount) {
            query.setStart(offset); // Use combination of start and rows for pagination.
            query.setFilterQueries(queryFilter);
            QueryResponse response = client.query(query);
            SolrDocumentList documents = response.getResults();

            //logger.info("Found " + documents.getNumFound() + " documents");
            //logger.info("Showing " + maxCount + " documents");

            for (SolrDocument document : documents) {
                String lemma = (String) document.getFirstValue("lemma");
                String pos = (String) document.getFirstValue("pos");
                String fileName = (String) document.getFirstValue("fileName");
                logger.info("Lemma: " + lemma + "; pos: " + pos + "; FileName: " + fileName);
            }
            offset += batch;
        }
    }

    /**
     * Runs query to check whether given fileName has already been indexed.
     * Put in to avoid repeat indexing of files.
     *
     * @param client    Http Solr Client
     * @param fileName  Explicit file name to be searched in the index
     * @param extension Can be an empty string. No "." required. Used if fileName doesn't have extension in it.
     * @return Query Response for the upstream function to parse and make decisions
     * @throws IOException         IO Exception
     * @throws SolrServerException Solr Exception
     */
    public boolean queryCheckFileIndexed(HttpSolrClient client, String fileName, String extension) throws IOException, SolrServerException {

        SolrQuery query = new SolrQuery();
        query.setParam("debug", false);
        query.setParam("defType", "dismax"); // Other choices are dismax, ...

        if (extension != "") {
            //extension = "txt";
            fileName = fileName.concat(".").concat(extension);
        }
        query.setParam("q", fileName);
        query.setFields("*, score");

        QueryResponse response = client.query(query);
        long resultsFound = response.getResults().getNumFound();
        logger.debug("Found {} indexes for '{}'", resultsFound, fileName);
        return resultsFound > 0;


        //String queryFilter = "+pos:NNP +pos:NNPS +fileName:" + patternFileNames;
        //query.setQuery("*debt*");
        //query.setFields("symbol, filing, lemma, pos, fileName");
        //query.set("fl", "lemma, pos, fileName");

        /*
        SolrDocumentList documents = response.getResults();
        //logger.info("Found " + documents.getNumFound() + " documents");
        //logger.info("Showing " + maxCount + " documents");
        for (SolrDocument document : documents) {
            String lemma = (String) document.getFirstValue("lemma");
            String pos = (String) document.getFirstValue("pos");
            String fileName = (String) document.getFirstValue("fileName");
            logger.info("Lemma: " + lemma + "; pos: " + pos + "; FileName: " + fileName);
        }
        offset += batch;
        */
    }
}



