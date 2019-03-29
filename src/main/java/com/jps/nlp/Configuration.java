package com.jps.nlp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class Configuration {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(Configuration.class);
	// Basic parameters
	static boolean ENABLE_CATALOGING = false; // Whether to attempt cataloging at all
	public static boolean ENABLE_SOLR_INDEXING = false; // Whether to attempt cataloging at all
	static boolean DUMP_SQL = false; 
	static boolean WATSON_NLP = false;
	static boolean STANFORD_NLP = false;
	static boolean DOWNLOAD_SEC = false;
	static boolean PURGE_HTM_EXTENSION = false;

	// All MySQL parameter
	static String MYSQL_SERVER_NAME = ""; 
	static int MYSQL_PORT;
	static String MYSQL_DB_NAME = "";
	static String MYSQL_USERNAME= "";
	static String MYSQL_PWD= "";
	
	// All Watson parameter
	static String WATSON_NLU_VERSION = "";
	static String WATSON_KEY= "";
	static String WATSON_PWD= "";

	// Path where a dir or files are accessible as a local share
	static String PATH_LOCAL_DIR_FILES = "";

	static boolean NER= false; // Named Entity Recognition

	static List<String> symbolsList = new ArrayList<String>();
	static JsonNode configJsonNode = JsonNodeFactory.instance.objectNode();
	static Map<String, Map<String, Integer>> mapFinancialReturns = new HashMap<String, Map<String, Integer>>();
	
	/**
	 * Reads the configuration file for all parameters. 
	 * Everything is static since a single copy is sufficient. 
	 */
	public static void readConfig(){
		
		String workingDir = System.getProperty("user.dir");
		String filePath = workingDir + File.separator + "config.json";
		logger.info("The Configuration File Path is "+filePath);
		
		try {
			// ObjectMapper is for JSON read operations
			ObjectMapper mapper = new ObjectMapper();
			String configJson = modReadFile.readFile(filePath);
			configJsonNode = mapper.readTree(configJson);
			//logger.debug(configJson);

			DUMP_SQL = configJsonNode.get("dumpSQL").asBoolean();
			WATSON_NLP = configJsonNode.get("WatsonNLP").asBoolean();
			STANFORD_NLP = configJsonNode.get("StanfordNLP").asBoolean();
			NER = configJsonNode.get("NER").asBoolean();
			DOWNLOAD_SEC = configJsonNode.get("DownloadSecReturns").asBoolean();
			PURGE_HTM_EXTENSION = configJsonNode.get("purgeHtmExtension").asBoolean();

			ENABLE_SOLR_INDEXING = configJsonNode.get("enableSolrIndexing").asBoolean();



			/* All Cataloging related options */
			ENABLE_CATALOGING = configJsonNode.get("enableCataloging").asBoolean();
			MYSQL_SERVER_NAME = configJsonNode.get("mysqlServerName").asText();
			MYSQL_PORT = configJsonNode.get("mysqlPort").asInt();
			MYSQL_DB_NAME = configJsonNode.get("mysqlDbName").asText();
			MYSQL_USERNAME = configJsonNode.get("mysqlUserName").asText();
			MYSQL_PWD = configJsonNode.get("mysqlUserPwd").asText();

			/* All Watson metadata options */
			WATSON_NLU_VERSION = configJsonNode.get("WatsonNLUVersion").asText();
			WATSON_KEY = configJsonNode.get("WatsonKey").asText();
			WATSON_PWD = configJsonNode.get("WatsonPwd").asText();

			PATH_LOCAL_DIR_FILES = configJsonNode.get("pathLocalDir").asText();

			
			/*
			 * The config JSON or upstream function will set up a Map where symbol 
			 * will be key and value will be another map. The value map will have return 
			 * type (10-K, 10-Q etc.) as key and count for each as the value. Here, we 
			 * retrieve all that data.  
			 */

			JsonNode returnsJsonList = configJsonNode.get("returns");
			if (returnsJsonList != null){
				Map<String, Integer> filingTypeAndCount = new HashMap<String, Integer>();
				for (JsonNode returnJson : returnsJsonList) {
					String symbol = returnJson .get("symbol").asText();
					JsonNode filingsJsonList = returnJson.get("filings");
					for (JsonNode filingJson : filingsJsonList) {
						String type = filingJson .get("type").asText();
						int count = filingJson .get("count").asInt(); 
						if ( (type != null) && (count > 0)) 
							filingTypeAndCount.put(type, count);
					}
					if ((filingTypeAndCount != null) && (!filingTypeAndCount.isEmpty())){
						Map<String, Integer> tempMap = new HashMap<String, Integer>();
						tempMap.putAll(filingTypeAndCount);
						mapFinancialReturns.put(symbol, tempMap);
						filingTypeAndCount.clear();
					}
				}
			}
		}
		catch (JsonParseException e) {
			e.printStackTrace();
			logger.error("JsonParseException while reading the base Configuration JSON file: "+e.getMessage());
			logger.error("Validate configuration JSON file for errors.");
		}

		catch (Exception e) {
			logger.error("Exception while reading the base Configuration JSON file: "+e.getMessage());
			e.printStackTrace();
			logger.warn("Ensure that \"config.json\" file is present in the current directory.");
			logger.warn("Program will attempt annotation but no cataloging will be performed.");
		}
	}

	public Map<String, Map<String, Integer>> getReturnsMap(){
		return mapFinancialReturns;
	}
	
	public static String getMySQLServerName(){
		return MYSQL_SERVER_NAME;
	}

	public static int getgetMySQLPort(){
		return MYSQL_PORT;
	}

	public static String getMySQLDBName(){
		return MYSQL_DB_NAME;
	}

	public static String getMySQLUserName(){
		return MYSQL_USERNAME;
	}

	public static String getMySQLUserPwd(){
		return MYSQL_PWD;
	}

	public static String getLocalFiles(){
		return PATH_LOCAL_DIR_FILES ;
	}

	public static boolean getNER(){
		return NER;
	}

	public static boolean getDownloadSec(){
		return DOWNLOAD_SEC;
	}

	public static boolean getSqlDumpStatus(){
		return DUMP_SQL;
	}

	public static boolean getStanfordNLP(){
		return STANFORD_NLP;
	}

	public static boolean getWatsonNLP(){
		return WATSON_NLP;
	}

	public static boolean getCatalogingState(){
		return ENABLE_CATALOGING;
	}
}