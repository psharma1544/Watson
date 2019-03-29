package com.jps.nlp;

import java.io.*;
import java.util.*;


/**
 * WatsonNLP provides access to IBM Watson based NLP artifacts. 
 * @author psharma 
 */
public interface WatsonNLP extends NLPI{
	

	/**
	 * Analyzes NLP for a target which can a URL or HTML or a text file.
	 * <p>
	 *
	 * @param docType the type of document among one of "url", "html", "text" for the target 
	 * document whose text is to be analyzed. The URL, if selected, must be either public or 
	 * otherwise internally available to the system where this application is run.  
	 *
	 *@param  path Local path to a Text file or link to URL / HTML page
	 *
	 * @param targets a list of different entities which will be searched for and reported on.  
	 * 
	 * @throws Exception on any failure.
	 * 
	 */
	
	String processTgtDocAgainstTargetEntities(String docType, String path, List<String> targets) throws Exception;
	
		
}
