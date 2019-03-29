package com.jps.nlp;

import com.jps.search.SolrConnectImpl;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Annotations generates the annotation metadata for input files in a pipeline.
 * It further calls catalog class to insert annotated metadata.
 *
 * @author psharma
 */
public class StanfordCoreNLPImpl implements StanfordCoreNLPI {

    private static final Logger logger = LoggerFactory.getLogger(StanfordCoreNLPImpl.class);
    private final SolrConnectImpl solr = new SolrConnectImpl();
    // General purpise utility class
    private Utils utils = new Utils();
    // Instantiate catalog class
    private CatalogNLPData catalog = new CatalogNLPData();
    // Retrieve some configuration parameters to control program responses
    private final boolean CATALOGING_ENABLED = Configuration.ENABLE_CATALOGING;
    private final String dbName = Configuration.MYSQL_DB_NAME;
    private final boolean NER = Configuration.NER;
    private final String BASE_NLP_DIR_PATH = "C:\\Users\\pshar\\Dropbox\\Programming\\SampleTexts\\NLP_Output";

    @Override
    public String processTgt(String docType, String symbol, String filingType, String absPath) {
        /*
         * Currently parsing and SQL insert ops are one file at a time in a loop.
         * Otherwise, scaling will become a problem.
         */
        logger.info("Retrieving next location for traversal: " + absPath);
        // TODO: Need "case:" handling here for URL, HTML, local files
        File f = new File(absPath);
        String fileInput = f.getName();
        String contents = modReadFile.readFile(absPath);
        //logger.debug(contents);

        /*
         * Get output of annotate() which returns annotations
         * and sentences in document separately. Get filenames
         * of that returned data and individually call insert() functions
         * for annotations and sentences respectively.
         */

        Map<String, String> inputMap = annotate(contents, symbol, filingType, absPath, fileInput);
        // Proceed with cataloging if enabled
        if (CATALOGING_ENABLED) {
            if (inputMap != null) {
                // Retrieve file name of file that has information on sentences from annotation pipeline
                String fileSentences = inputMap.get("sentences");
                // TODO: Remove the hard-coding of the MySQL table name as "sentences" and "annotations"
                boolean insertedSentences = catalog.insertSentences(fileSentences, dbName, "sentences");
                // Insert annotations only if sentences were successfully inserted.
                if (insertedSentences) {
                    String fileAnnotations = inputMap.get("annotations");
                    catalog.insertAnnotations(fileAnnotations, dbName, "annotations");
                }
            }
        } // if (CATALOGING_ENABLED){
        else {
            logger.warn("MySQL Cataloging is disabled.");
            logger.warn("If required, enable cataloging from \"{pwd}/config.json\" and provide relevant MySQL configuration details.");
        }
        return null; // TODO: Come back and return proper string pattern
        // else {
    } // public void run(String dirPath){


    @Override
    public void processTgtList(String docsType, List<String> listPathsAndMetaData) {

        for (String pathandMetadata : listPathsAndMetaData) {
            String[] data = pathandMetadata.split(",");
            String symbol = data[0];
            String filingType = data[1];
            String absPath = data[2];
            File f = new File(absPath);
            String fileInput = f.getName();

            /*
                Check if the file is already in our index.
                If so, then don't proceed with indexing.
                TODO:: This check needs to be extended for MySQL as well. However, we are not currently cataloging there.
             */
            String collection = "markets";
            boolean fileIndexed = false;
            try {
                HttpSolrClient client = solr.getSolrClient(collection);
                fileIndexed = solr.queryCheckFileIndexed(client, fileInput, "");
                solr.closeSolrClient(client);
            } catch (IOException | SolrServerException e) {
                e.printStackTrace();
            }
            if (!fileIndexed)
                processTgt(docsType, symbol, filingType, absPath);
            else
                logger.warn("File {} already indexed. Skipping...", fileInput);
        }
        return;

    }


    /**
     * Runs annotations on the input file in a pipeline.
     *
     * @param input         Contents of the file in String form
     * @param dirPath       Directory or File path
     * @param nameInputFile File name
     * @return Map Contains file names for files containing annotation details and individual sentences
     */
    private Map<String, String> annotate(String input, String symbol, String filingType, String dirPath, String nameInputFile) {
        // Map to be eventually returned
        Map<String, String> returnMap = new HashMap<>();
        // Lists containing parsed annotation and sentence w/ metadata
        List<String> annList = new ArrayList<>();
        List<String> sentenceList = new ArrayList<>();

        // Files on system which will keep all this metadata for SQL to later insert() ops.
        String annFileName = "Annotations_".concat(utils.getDateAndTime().concat(".csv"));
        String sentenceFileName = "Sentences_".concat(utils.getDateAndTime().concat(".csv"));
        annFileName = annFileName.replace(":", "-");
        sentenceFileName = sentenceFileName.replace(":", "-");

        String annotators = "";
        // NER compute consumes resources and takes significant time. Enable only if explicitly configured by user.
        if (NER)
            annotators = "tokenize, ssplit, pos, lemma, ner";
        else
            annotators = "tokenize, ssplit, pos, lemma";
        // Now setup a NLP pipeline with given annotators and a set of option value pairs
        // TODO: The sentence delimiter without a succeeding space isn't recognized by CoreNLP currently.
        StanfordCoreNLP pipeline = new StanfordCoreNLP(
                PropertiesUtils.asProperties(
                        "annotators", annotators,
                        "ssplit.boundaryTokenRegex", "\\.|\\. |[!?]+",
                        "maxLength", "100",
                        "tokenize.language", "en"));
        // Create an empty Annotation just with the given text
        Annotation document = new Annotation(input);
        // Run all Annotators on this text
        // logger.info("Running annotations and retrieving annotated datasets.	");
        pipeline.annotate(document);
        // Retrieve list of all sentences for traversal by individual sentences
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        try {
            if (sentences != null) {
                // Counter to mark sentence number
                int count = 0;
                for (CoreMap sentence : sentences) {
                    // Retrieve and print the current sentence first
                    String text = sentence.get(TextAnnotation.class);
                    logger.info("Annotating sentence: '" + text + "'");
                    // Need a label to document sentence number
                    String sCount = "s" + (++count);
                    // Present cleanly in standard out for easier user reads
                    System.out.format("%n|%-64s|%-64s|%-8s|%-8s|%n", "TEXT", "LEMMA", "POS", "NER");
                    System.out.format("%n|%-64s|%-64s|%-8s|%-8s|%n", "----", "-----", "---", "---");
                    // traversing the words in the current sentence
                    // a CoreLabel is a CoreMap with additional token-specific methods
                    for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                        // Retrieve the raw word and it's base linguistic representation
                        String word = token.get(TextAnnotation.class);
                        String lemma = token.get(LemmaAnnotation.class);
                        // Retrieve POS tag of the token
                        String pos = token.get(PartOfSpeechAnnotation.class);
                        // Retrieve NER label of the token; null if not requested
                        String ne = token.get(NamedEntityTagAnnotation.class);
                        System.out.format("|%-64s|%-64s|%-8s|%-8s|%n", word, lemma, pos, ne);
                        // TODO: Changing separator from || to comma to be able to insert values into Solr DB
                        //annList.add(word + "||" + lemma + "||" + pos + "||" + ne + "||" + nameInputFile + "||" + sCount + "||" + dirPath);
                        annList.add(symbol + "," + filingType + "," + word + "," + lemma + "," + pos + "," + ne + "," + nameInputFile + "," + sCount + "," + dirPath);
                    }
                    //sentenceList.add(nameInputFile + "||" + sCount + "||" + dirPath + "||" + text);
                    // For sentences, need to remove commas to avoid challenges with CSV downstream
                    sentenceList.add(symbol + "," + filingType + "," + nameInputFile + "," + sCount + "," + dirPath + "," + text.replaceAll(",", " "));
                    // Parse tree of the current sentence, only captured if "parse" annotation was selected in pipeline
                    Tree tree = sentence.get(TreeAnnotation.class);
                    if (tree != null) {
                        logger.info("Seeting up Constituency Parse tree. ");
                        //options.constituencyTreePrinter.printTree(tree, pw);
                        logger.debug(tree.toString());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception during annotation traversal.");
            e.printStackTrace();
        }
        // Write the annotations to a file for later SQL insert() op.
        modOutCSV annTxt = new modOutCSV(BASE_NLP_DIR_PATH + File.separator + annFileName);
        annTxt.writeList(annList);

        // Push sentences to a file for later SQL insert() op.
        modOutCSV sentenceTxt = new modOutCSV(BASE_NLP_DIR_PATH + File.separator + sentenceFileName);
        sentenceTxt.writeList(sentenceList);

        returnMap.put("annotations", annFileName);
        returnMap.put("sentences", sentenceFileName);

        return returnMap;
    }
}