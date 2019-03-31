package com.jps.search;

import com.jps.nlp.Configuration;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataManagement {

    private final String TGT_DIRECTORY = "C:\\Users\\pshar\\Dropbox\\Programming\\SampleTexts\\NLP_Output";
    private final SolrConnectImpl solr = new SolrConnectImpl();
    private final boolean CLEAR_INDEX = false; // CAUTION: will clean index if true
    private final boolean ENABLE_INDEXING = Configuration.ENABLE_SOLR_INDEXING;
    private final Logger logger = (Logger) LoggerFactory.getLogger(DataManagement .class);

    public DataManagement() {
        try {
            String collection = "markets3";
            HttpSolrClient client = solr.getSolrClient(collection);

            if (CLEAR_INDEX) {
                client.deleteByQuery("*:*");// delete everything!
                client.commit();
            }

            if (ENABLE_INDEXING) {
                indexSentencesData(client);
                indexAnnotationsData(client);
            }
            long maxCount = 200; // Num of search results to be shown
            String patternFileNames = "*.*";
//            solr.queryLucene(client, maxCount, patternFileNames);
//            solr.queryDismax(client, maxCount, patternFileNames);
            solr.closeSolrClient(client);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initiates cataloging / indexing of NLP annotations
     *
     * @throws IOException
     * @throws SolrServerException
     */
    private void indexAnnotationsData(HttpSolrClient client) throws IOException, SolrServerException {
        String patternTgtFiles = "^(Annotations_)[\\w-]*.\\w*";
        List<String> fields = new LinkedList<>();
        fields.add(0, "symbol");
        fields.add(1, "filing");
        fields.add(2, "word");
        fields.add(3, "lemma");
        fields.add(4, "pos");
        fields.add(5, "ner");
        fields.add(6, "fileName");
        fields.add(7, "sNumber");

        solr.indexCsvByRow(client, TGT_DIRECTORY, patternTgtFiles, fields);
    }

    /**
     * Initiates cataloging / indexing of sentences containing NLP corpus
     *
     * @throws IOException
     * @throws SolrServerException
     */
    private void indexSentencesData(HttpSolrClient client) throws IOException, SolrServerException {
        //String patternTgtFiles = "^(Sentences)\\w*.\\w*";
        String patternTgtFiles = "^(Sentences_)[\\w-]*.\\w*";

        List<String> fields = new LinkedList<>();
        fields.add(0, "symbol");
        fields.add(1, "filing");
        fields.add(2, "fileName");
        fields.add(3, "sNumber");
        fields.add(4, "filePath");
        fields.add(5, "sentence");
        fields.add(6, "sentHash");

        solr.indexCsvByRow(client, TGT_DIRECTORY, patternTgtFiles, fields);
    }

    /**
     * Initiates cataloging / indexing of sentences containing NLP corpus
     *
     * @throws IOException
     * @throws SolrServerException
     */
    private void indexCatalogedFiles(HttpSolrClient client) throws IOException, SolrServerException {
        String patternTgtFiles = "TBD";
        List<String> fields = new LinkedList<>();
        fields.add(0, "fileName");

        solr.indexCsvByRow(client, TGT_DIRECTORY, patternTgtFiles, fields);
    }
}
