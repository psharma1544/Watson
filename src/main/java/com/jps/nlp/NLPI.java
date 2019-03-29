package com.jps.nlp;
import java.util.List;

/**
 * Base NLP Interface with basic function definitions
 * @author psharma
 */

public interface NLPI {

    /**
     * Analyzes NLP for a target which can be a URL or HTML or a text file.
     * <p>
     *
     * @param docType the type of document among one of "url", "html", "text" for the target
     * document whose text is to be analyzed. The URL, if selected, must be either public or
     * otherwise internally available to the system where this application is run.
     *
     *  @param  path Local path to a Text file or link to URL / HTML page
     * @throws Exception on any failure.
     *
     */
    String processTgt(String tgtType, String symbol, String filingType, String path) throws Exception;

    /**
     * Analyzes NLP for a target which can be a URL or HTML or a text file.
     * <p>
     *
     * @param docType the type of document among one of "url", "html", "text" for the target
     * documents whose text is to be analyzed. Note that all the documents in the listing
     * must be of the same type here. The URL, if selected, must be either public or
     * otherwise internally available to the system where this application is run.
     *
     * @param  listPaths List of local paths or links to URL / HTML page
     * @throws Exception on any failure.
     *
     */
    void processTgtList(String tgtsType, List<String> listPaths) throws Exception;


}
