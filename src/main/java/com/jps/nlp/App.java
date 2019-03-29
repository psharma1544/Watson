package com.jps.nlp;

import java.io.*;
import java.util.*;

import com.jps.predictions.dl4j.RNNEquityPricePredictor;
import com.jps.search.DataManagement;
import com.jps.search.SolrConnectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        //RNNEquityPricePredictor rnnEquityPricePredictor = new RNNEquityPricePredictor();
        //rnnEquityPricePredictor.RNNEquityPricePredictor();

        // Load configuration information
        Configuration.readConfig();
        final boolean WATSON_NLP = Configuration.WATSON_NLP;
        final boolean STANFORD_NLP = Configuration.STANFORD_NLP;

        // Now populate various targets that NLP APIs will traverse
        // Constructor takes care of populating list of targets
        NLPTargets targets = new NLPTargets();

        //TODO: Create a MAP here coupling input type and corresponding list.

        List<String> listUrls = targets.getListUrls();
        List<String> listFiles = targets.getListFiles();
        List<String> listHtmls = targets.getListHtmls();

        listFiles.forEach(file -> logger.info(file));


        if ((listUrls == null) || (listUrls.isEmpty())) {
            logger.error("Could not find any web links to traverse for NLP analysis.");
            logger.error("Ensure that input configuration file has right company symbols and filing types entered.");
        }
        else if (WATSON_NLP) {
            WatsonNLPImpl langAnalysis = new WatsonNLPImpl();
            //langAnalysis.processTgtList("url", listUrls);
            langAnalysis.processTgtList("html", listUrls);
        }

        if ((listFiles == null) || (listFiles.isEmpty())) {
            logger.error("Could not find any local files to traverse for NLP analysis.");
            logger.error("Application currently only accepts files in *.txt format.");
            logger.error("Please provide a directory containing text files or a single text file!");
        }

        else if (STANFORD_NLP) {
            StanfordCoreNLPImpl coreNlp = new StanfordCoreNLPImpl();
            //coreNlp.processTgtList("file", listFiles);
            coreNlp.processTgtList("file", listFiles);
        }

        DataManagement dm = new DataManagement();
    }
}


