package com.jps.nlp;

import java.io.File;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages catalog insert operations for annotation metadata
 * @author psharma
 *
 */
public class CatalogNLPData {

	private final Logger logger = (Logger) LoggerFactory.getLogger(CatalogNLPData.class);
	
	/** The name of the MySQL account to use (or empty for anonymous) */
	private final String userName = Configuration.MYSQL_USERNAME;

	/** The password for the MySQL account (or empty for anonymous) */
	private final String password = Configuration.MYSQL_PWD;

	/** The name of the server running MySQL */
	private final String serverName = Configuration.MYSQL_SERVER_NAME;

	/** The port of the MySQL server (default is 3306) */
	private final int portNumber = Configuration.MYSQL_PORT;
	
	/** The name of the database we are testing with (this default is installed with MySQL) */
	private final String dbName = Configuration.MYSQL_DB_NAME;

	/** The number of entries to be inserted */
	private final int ENTRIES = 7;

	// Instantiate utility class
	Utils utils = new Utils();

	/**
	 * Inserts annotation data (lemma, PoS, NER) along with file meta-data (name, dir, #sentence) into SQL table
	 * @param fileName Input file that contains all the delimited information that is to be cataloged 
	 * @param dbName MySQL DB where target table resides
	 * @param tblName MySQL Table to which the data is to be inserted
	 */
	public boolean  insertAnnotations(String fileName, String dbName, String tblName){

		//dbIncomeStmt.prepDataForInsert (fileCompanyFinancialData, dbName, tblName)
		Connection conn = null;
		String lineError = "";
		Properties properties = new Properties();
		int countSkipped = 0;
		int countInserted = 0;
		properties.put("user", userName);
		properties.put("password", password);
		properties.put("useSSL", "false");
		properties.setProperty("autoReconnect", "true");

		List<String> inputList  = modReadFile.readFileIntoListOfStrings(fileName);

		logger.debug("The size of the returned list is: " + inputList .size());
		if ((inputList == null) || (inputList .size() == 0)){
			logger.error("File \""+fileName+"\" does not have any rows to be inserted into the database.");
		}
		// Else file has good data for us to process and insert into DB table. 
		else
			try {
				// TODO: Add exception handling for invalid server / port / db names. 
				// TODO: Handle case where the DB doesn't exist. Create one in that case and then insert.
				
				conn = DriverManager.getConnection("jdbc:mysql://"+ serverName + ":" + portNumber + "/" + dbName, properties);
				String statement = "INSERT INTO "+tblName+" VALUES (?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement pstmt = conn.prepareStatement(statement);
				logger.info("Inserting entries into table '" + tblName+"'...");
				logger.info("Operation may take some time to complete depending on size of input.");
				
				for (String line: inputList){
					lineError = line;
					// Separate input columns based on custom separators
					String[] temp = line.split("\\|\\|");
					if (temp.length == ENTRIES){
						String text = temp[0]; // Raw text
						String lemma = temp[1]; // Base lemma
						String pos = temp[2]; // Part of Speech
						String ner = temp[3]; // Named Entity Recognition
						String file = temp[4]; // File name
						String sNumber = temp[5]; // # Sentence 
						String baseDir = temp[6]; // Directory
						
						// Associate values with position holders for SQL insert
						pstmt.setString(1, text);
						pstmt.setString(2, lemma);
						pstmt.setString(3, pos);
						pstmt.setString(4, ner);
						pstmt.setString(5, file);
						pstmt.setString(6, sNumber);
						pstmt.setString(7, baseDir);
					}
					else{
						logger.error("Parsing input resulted in more data than expected for line: "+lineError);
						// No point inserting this line. Move onto next one and avoid SQL errors. 
						continue; 
					}
					try{
						// Execute and if successful add counters for successful insertions
						pstmt.execute();
						++countInserted;
					}
					catch (SQLIntegrityConstraintViolationException e) { // Don't insert if PK value already exists
						logger.debug("Caught SQLIntegrityConstraintViolationException. Skipping adding redundant entries.");
						++countSkipped;
					}
					//catch (MysqlDataTruncation e) {
					catch (SQLException e) {
						logger.warn(e.getMessage());
						++countSkipped;
					}
				}
				// Close the statement.
				pstmt.close();
				conn.close();
			}
		catch (SQLException e) {
			logger.error("SQLException: << "+e.getMessage()+ " >> occurred for following line item:\""+lineError+"\"");
			logger.error("For debugging check the referred data point in the input file: \""+fileName+"\"");
			e.printStackTrace();}
		catch (Exception e) {
			logger.error("Non-SQL Exception: << "+e.getMessage()+ " >> while traversing following line item:\""+lineError+"\" in the input file: \""+fileName+"\"");
			e.printStackTrace();
		}
		
		logger.info("Total # of records inserted into table '"+tblName+"': "+countInserted);
		logger.info("Total # of records skipped (were already present or caused exception) for insert: "+countSkipped);
	
		if (countInserted > 0){
			return true;
		}
		else 
			return false; 

	}
	
	/**
	 * Inserts sentences along with their respective metadata  into SQL table
	 * @param fileName Input file that contains all the delimited information that is to be cataloged 
	 * @param dbName MySQL DB where target table resides
	 * @param tblName MySQL Table to which the data is to be inserted
	 */
	public boolean  insertSentences(String inputFile, String dbName, String tblName){
		
		Connection conn = null;
		String lineError = "";
		Properties properties = new Properties();
		int countSkipped = 0;
		int countInserted = 0;
		properties.put("user", userName);
		properties.put("password", password);
		properties.put("useSSL", "false");
		properties.setProperty("autoReconnect", "true");

		List<String> inputList  = modReadFile.readFileIntoListOfStrings(inputFile);

		logger.debug("The size of the returned list is: " + inputList .size());
		if ((inputList == null) || (inputList .size() == 0)){
			logger.error("File \""+inputFile+"\" does not have any rows to be inserted into the database.");
		}
		// Else file has good data for us to process and insert into DB table. 
		else
			try {
				// TODO: Add exception handling for invalid server / port / db names. 
				// TODO: Handle case where the DB doesn't exist. Create one in that case and then insert. 
				conn = DriverManager.getConnection("jdbc:mysql://"+ serverName + ":" + portNumber + "/" + dbName, properties);
				String statement = "INSERT INTO "+tblName+" VALUES (?, ?, ?, ?)";
				PreparedStatement pstmt = conn.prepareStatement(statement);
				logger.info("Inserting entries into table '" + tblName+"'...");
				logger.info("Operation may take some time to complete depending on size of input.");
				
				for (String line: inputList){
					lineError = line;
					String[] temp = line.split("\\|\\|");
					if (temp.length == 4){
						String fileName = temp[0];
						String sNumber = temp[1]; // # of sentence within document
						String parentDir = temp[2]; 
						String text = temp[3]; // Text of sentence
						
						// Associate values with position holders for SQL insert
						pstmt.setString(1, fileName);
						pstmt.setString(2, sNumber);
						pstmt.setString(3, parentDir);
						pstmt.setString(4, text);
					}
					else{
						logger.error("Parsing input resulted in more data than expected for line: "+lineError);
						// No point inserting this line. Move onto next one and avoid SQL errors. 
						continue; 
					}
					try{
						pstmt.execute();
						++countInserted;
					}
					catch (SQLIntegrityConstraintViolationException e) {
						logger.debug("Caught SQLIntegrityConstraintViolationException. Skipping adding redundant entries.");
						++countSkipped;
					}
					//catch (MysqlDataTruncation e) {
					catch (SQLException e) {
						logger.warn(e.getMessage());
						++countSkipped;
					}
				}
				// Close the statement.
				pstmt.close();
				conn.close();
			}
		catch (SQLException e) {
			logger.error("SQLException: << "+e.getMessage()+ " >> occurred for following line item:\""+lineError+"\"");
			logger.error("For debugging check the referred data point in the input file: \""+inputFile+"\"");
			e.printStackTrace();}
		catch (Exception e) {
			logger.error("Non-SQL Exception: << "+e.getMessage()+ " >> while traversing following line item:\""+lineError+"\" in the input file: \""+inputFile+"\"");
			e.printStackTrace();
		}
		
		logger.info("Total # of records inserted into table '"+tblName+"': "+countInserted);
		logger.info("Total # of records skipped (were already present or caused exception) for insert: "+countSkipped);
		
		if (countInserted > 0){
			return true;
		}
		else 
			return false; 
	}

	/**
	 * Pushes output of a SQL query to a CSV file.
	 * Very useful for capturing and sharing output of a SQL query. 
	 * @param query The SQL query to be run 
	 * @param strTableHeading A string containing all relevant column headers for the CSV with commas separating them
	 * @param numTableColumns Number of columns
	 */
	public void outSqlToCSV(String query, String strTableHeading, int numTableColumns){

		Connection conn = null;
		Properties properties = new Properties();
		properties.put("user", userName);
		properties.put("password", password);
		properties.put("useSSL", "false");
		properties.setProperty("autoReconnect", "true");
		String errorLine ="";;

		try{
			conn = DriverManager.getConnection("jdbc:mysql://"+ serverName + ":" + portNumber + "/" + dbName, properties);
			
			List<String> returnList = new LinkedList<String>();
			PreparedStatement pstmtSelectAll = conn.prepareStatement(query);
			ResultSet rs = pstmtSelectAll.executeQuery();
			if (strTableHeading != "")
				returnList.add(strTableHeading);

			if (rs != null){
				while (rs.next()){
					String line = "";
					for (int i = 1; i <=numTableColumns; i++){
						//logger.debug(line);
						line = line.concat((rs.getString(i)).concat("||"));
						errorLine = line;
					}
					returnList.add(line);
				}
			}
			else 
				logger.error("Empty or Null Result Set received. Validate the input SQL query.");
			pstmtSelectAll.close();
			String fileSQLOut = "SQLOut".concat("_").concat(utils.getDateAndTime().concat(".txt"));
			fileSQLOut = fileSQLOut.replace(":", "-");
			modOutCSV outCSV = new modOutCSV(fileSQLOut);
			outCSV.writeList(returnList);
			File f = new File(fileSQLOut);
			if (f.exists())
				logger.info("Exported SQL query results to file: "+fileSQLOut);

			conn.close();
		}
		catch (SQLException e) {
			logger.error("SQLException: "+e.getMessage());
			logger.error("Exception occurred at data input:  "+errorLine);
			e.printStackTrace();}
		catch (Exception e) {
			logger.error("Non-SQL Exception: << "+e.getMessage());
			logger.error("Run and validate input SQL query.");
			logger.error("Exception occurred at data input:  "+errorLine);
			e.printStackTrace();
		}
	}
}