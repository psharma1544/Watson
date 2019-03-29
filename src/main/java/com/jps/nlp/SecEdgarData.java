package com.jps.nlp;

import java.util.*;


/**
 *  
 * @author psharma 
 */
public interface SecEdgarData {
	
	/**
	 * Retrieves URL of SEC EDGAR link for a given public company traded in US markets.
	 * @param symbol Symbol of a public company listed on NYSE / NASDAQ
	 * @return URL where user can search returns of a public company
	 */

	String urlBaseEdgar(String symbol)  throws Exception;

	/**
	 * Retrieves base URL for a particular filing type (10-Q, 10-K) for a given public company.
	 * This URL will in turn have several other URLs in the web page each of which will have 
	 * text for a single document filed with SEC. 
	 * @param symbol Symbol of a public company listed on NYSE / NASDAQ
	 * @param baseCompanyUrl 
	 * @param typeFinancialDoc 
	 * @return URL containing a listing of returns of a single type for a single public company
	 */

	String urlFilingTypeEdgar(String symbol, String baseCompanyUrl, String typeFinancialDoc) throws Exception;
	
	/**
	 * Generates a listing where each entry contains URL of a specific type of financial document for a given company. 
	 * @param baseUrlCompanyFilingType URL of the base EDGAR page for a public company's financial documents
	 * @param numDocs Number of documents (10-k, 10-q) to be retrieved. 
	 * @param searchPattern Pattern to be searched for in the page <\br>
		Must be either "Documents" or "Interactive"
	 * @return A list containing individual URLs for financial data for each document. 
	 */
	
	List<String> urlsSpecificSingleFilingEdgarPage(String baseUrlCompanyFilingType, int numDocs, String retrievalType) throws Exception;
	
	/**
	 * Generates the URL for a specific return. 
	 * <p> As an example, this can return the URL of symbol "ABC" for 10-K for year 2017. 
	 * @param docType
	 * @param path
	 * @return
	 */
	
	String urlSingleReturnDetailsEdgar(String urlFiliingType, String filingType)  throws Exception;
	
}