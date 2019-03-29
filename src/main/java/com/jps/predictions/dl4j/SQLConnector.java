package com.jps.predictions.dl4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.jps.util.modOutCSV;
import com.jps.util.Utils;

public class SQLConnector {


    private final Logger logger = (Logger) LoggerFactory.getLogger(SQLConnector.class);

    /**
     * The name of the MySQL account to use (or empty for anonymous)
     */
    private final String userName = "psharma";

    /**
     * The password for the MySQL account (or empty for anonymous)
     */
    private final String password = "$ys8dmin";

    /**
     * The name of the computer running MySQL
     */
    private final String serverName = "localhost";

    /**
     * The port of the MySQL server (default is 3306)
     */
    private final int portNumber = 3306;

    /**
     * The name of the database we are testing with (this default is installed with MySQL)
     */
    private final String dbName = "markets";

    /**
     * Size of recurring window for training. Keeping 5 for weekly precitions
     */

    private final int window = 5;

    /**
     * Size of recurring window for training. Keeping 5 for weekly precitions
     */

    private static final String SQL_DUMP_FOLDER = "C:\\Users\\pshar\\Dropbox\\Programming\\DL4J_Dumps";

    private static File baseDir = new File(SQL_DUMP_FOLDER);
    private static File baseTrainDir = new File(baseDir, "train");
    private static File baseTestDir = new File(baseDir, "test");

    Utils utils = new Utils();

    /**
     * Pushes output of a SQL query to a CSV file
     *
     * @param query           The SQL query to be run
     * @param strTableHeading A string containing all relevant column headers for the CSV with commas separating them
     * @param numTableColumns Number of columns
     */

    public void outSqlToCSV(String query, String strTableHeading, int numTableColumns, boolean overwrite) {

        String[] symbols = {"AAPL", "AMZN", "FB", "NFLX", "MSFT", "GOOGL"};
        String tblName = "equities_metadata6";
        String startDate = "2001-01-01";
        String endDate = "2018-06-09";

        if (overwrite) { // select T1.date,
            /**
             * If I understood Javadoc correctly, the output or the Y column(s) must be at the very end of input files.
             * Therefore, T1.close is captured as the last item in the query below.
             */
            query = "select T2.close as Close_" + symbols[1] + ", T3.close as Close_" + symbols[2] + ", T4.close as Close_" + symbols[3] + ", " +
                    "T5.close as Close_" + symbols[4] + ", T6.close as Close_" + symbols[5] + ", T1.open as Open_" + symbols[0] + ", " +
                    "T2.open as Open_" + symbols[1] + ", T3.open as Open_" + symbols[2] + ", T4.open as Open_" + symbols[3] + ", " +
                    "T5.open as Open_" + symbols[4] + ", T6.open as Open_" + symbols[5] + ", T1.high as High_" + symbols[0] + ", " +
                    "T2.high as High_" + symbols[1] + ", T3.high as High_" + symbols[2] + ", T4.high as High_" + symbols[3] + ", " +
                    "T5.high as High_" + symbols[4] + ", T6.high as High_" + symbols[5] + ", T1.low as Low_" + symbols[0] + ", " +
                    "T2.low as Low_" + symbols[1] + ", T3.low as Low_" + symbols[2] + ", T4.low as Low_" + symbols[3] + ", " +
                    "T5.low as Low_" + symbols[4] + ", T6.low as Low_" + symbols[5] + ", T1.moveStochastic as move_" + symbols[0] + ", " +
                    "T2.moveStochastic as move_" + symbols[1] + ", T3.moveStochastic as move_" + symbols[2] + ", T4.moveStochastic as move_" + symbols[3] + ", " +
                    "T5.moveStochastic as move_" + symbols[4] + ", T6.moveStochastic as move_" + symbols[5] + ", T1.avgM as avgM_" + symbols[0] + ", " +
                    "T2.avgM as avgM_" + symbols[1] + ", T3.avgM as avgM_" + symbols[2] + ", T4.avgM as avgM_" + symbols[3] + ", T5.avgM as avgM_" + symbols[4] + ", " +
                    "T6.avgM as avgM_" + symbols[5] + ", T1.emaM as emaM_" + symbols[0] + ", T2.emaM as emaM_" + symbols[1] + ", " +
                    "T3.emaM as emaM_" + symbols[2] + ", T4.emaM as emaM_" + symbols[3] + ", T5.emaM as emaM_" + symbols[4] + ", " +
                    "T6.emaM as emaM_" + symbols[5] + ", T1.emaBW as emaBW_" + symbols[0] + ", T2.emaBW as emaBW_" + symbols[1] + ", " +
                    "T3.emaBW as emaBW_" + symbols[2] + ", T4.emaBW as emaBW_" + symbols[3] + ", T5.emaBW as emaBW_" + symbols[4] + ", " +
                    "T6.emaBW as emaBW_" + symbols[5] + ", T1.oscillatorBW as oscillatorBW_" + symbols[0] + ", T2.oscillatorBW  as oscillatorBW_" + symbols[1] + ", " +
                    "T3.oscillatorBW  as oscillatorBW_" + symbols[2] + ", T4.oscillatorBW  as oscillatorBW_" + symbols[3] + ", T5.oscillatorBW  as oscillatorBW_" + symbols[4] + ", T6.oscillatorBW  as oscillatorBW_" + symbols[5] + ", " +
                    "T1.rsiBW as rsiBW_" + symbols[0] + ", T2.rsiBW as rsiBW_" + symbols[1] + ", T3.rsiBW as rsiBW_" + symbols[2] + ", " +
                    "T4.rsiBW as rsiBW_" + symbols[3] + ", T5.rsiBW as rsiBW_" + symbols[4] + ", T6.rsiBW as rsiBW_" + symbols[5] + ", " +
                    "T1.emaQ as emaQ_" + symbols[0] + ", T2.emaQ as emaQ_" + symbols[1] + ", T3.emaQ as emaQ_" + symbols[2] + ", " +
                    "T4.emaQ as emaQ_" + symbols[3] + ", T5.emaQ as emaQ_" + symbols[4] + ", T6.emaQ as emaQ_" + symbols[5] + ", " +
                    "T1.PcntleStockM as PcntleStockM_" + symbols[0] + ", T2.PcntleStockM as PcntleStockM_" + symbols[1] + ", " +
                    "T3.PcntleStockM as PcntleStockM_" + symbols[2] + ", T4.PcntleStockM as PcntleStockM_" + symbols[3] + ", " +
                    "T5.PcntleStockM as PcntleStockM_" + symbols[4] + ", T6.PcntleStockM as PcntleStockM_" + symbols[5] + ", " +
                    "T1.netChange as netChange_" + symbols[0] + ", T2.netChange as netChange_" + symbols[1] + ", T3.netChange as netChange_" + symbols[2] + ", " +
                    " T4.netChange as netChange_" + symbols[3] + ", T5.netChange as netChange_" + symbols[4] + ", T6.netChange as netChange_" + symbols[5] + ", " +
                    "T1.pcntChange as pcntChange_" + symbols[0] + ", T2.pcntChange as pcntChange_" + symbols[1] + ", T3.pcntChange as pcntChange_" + symbols[2] + ", " +
                    "T4.pcntChange as pcntChange_" + symbols[3] + ", T5.pcntChange as pcntChange_" + symbols[4] + ", T6.pcntChange as pcntChange_" + symbols[5] + ", " +
                    "T1.volume as vol_" + symbols[0] + ", T2.volume as vol_" + symbols[1] + ", T3.volume as vol_" + symbols[2] + ", " +
                    "T4.volume as vol_" + symbols[3] + ", T5.volume as vol_" + symbols[4] + ", T6.volume as vol_" + symbols[5] + ", " +
                    "T1.close as Close_" + symbols[0] + " " +
                    "from " + tblName + " as T1 INNER JOIN  " + tblName + " as T2 on T1.date = T2.date INNER JOIN  " + tblName + " as T3 ON T2.date = T3.date " +
                    "INNER JOIN  " + tblName + " as T4 ON T3.date = T4.date INNER JOIN  " + tblName + " as T5 ON T4.date = T5.date INNER JOIN  " + tblName + " " +
                    "as T6 ON T5.date = T6.date WHERE T1.symbol like '" + symbols[0] + "' and T2.symbol like '" + symbols[1] + "' and T3.symbol like '" + symbols[2] + "' " +
                    "and T4.symbol like '" + symbols[3] + "' and T5.symbol like '" + symbols[4] + "' and T6.symbol like '" + symbols[5] + "' " +
                    "and T1.date > '" + startDate + "' and T1.date < '" + endDate + "'";


            Connection conn = null;
            Properties properties = new Properties();
            properties.put("user", userName);
            properties.put("password", password);
            properties.put("useSSL", "false");
            properties.setProperty("autoReconnect", "true");
            String errorLine = "";

            List<String> cappedReturnedList = new LinkedList<>();
            try {
                conn = DriverManager.getConnection("jdbc:mysql://" + serverName + ":" + portNumber + "/" + dbName, properties);
                Statement st = conn.createStatement();

                List<String> returnList = new LinkedList<String>();
                PreparedStatement pstmtSelectAll = conn.prepareStatement(query);
                ResultSet rs = pstmtSelectAll.executeQuery();
                if (strTableHeading != "")
                    returnList.add(strTableHeading);

                if (rs != null) {
                    while (rs.next()) {
                        String line = "";
                        for (int i = 1; i <= numTableColumns; i++) {
                            //logger.debug(line);
                            line = line.concat((rs.getString(i)).concat(","));
                            errorLine = line;
                        }
                        returnList.add(line);
                    }
                } else
                    logger.error("Empty or Null Result Set received. Validate the input SQL query.");

                // Cap the line count to be a perfect multiple of window size, skipping first few and not last few
                int rsSize = returnList.size();
                int remainder = rsSize % window;
                cappedReturnedList = returnList.stream().skip(remainder).collect(Collectors.toCollection(LinkedList::new));
                pstmtSelectAll.close();

                conn.close();
            } catch (SQLException e) {
                logger.error("SQLException: " + e.getMessage());
                logger.error("Exception occurred at data input:  " + errorLine);
                e.printStackTrace();
            } catch (Exception e) {
                logger.error("Non-SQL Exception: << " + e.getMessage());
                logger.error("Run and validate input SQL query.");
                logger.error("Exception occurred at data input:  " + errorLine);
                e.printStackTrace();
            }
            //String fileSQLOut = "SQLOut".concat("_").concat(utils.getDateAndTime().concat(".csv"));

            genSqlFiles(baseDir, cappedReturnedList);
        }

    }

    /**
     * DL4J has the requirement where the input data for RNN
     * needs to be inserted across several CSV files where
     * each file contains data for a single time-series window.
     * Handle that requirement here.
     * @param tgtDir Directory under which SQL export files are created
     * @param inputList CSV input that is parsed and written into individual files
     */

    private void genSqlFiles(File tgtDir, List<String> inputList) {

        int[] cntFiles = {0};
        List<String> tempList = new LinkedList<>();
        inputList.forEach(row -> {
            tempList.add(row);
            if (tempList.size() == window) {
                String fileSQLOut = "SQLOut".concat("_").concat(String.valueOf(cntFiles[0]++)).concat(".csv");
                fileSQLOut = fileSQLOut.replace(":", "-");

                modOutCSV outCSV = new modOutCSV(tgtDir + File.separator + fileSQLOut);
                outCSV.writeList(tempList);
                File f = new File(fileSQLOut);
                if (f.exists())
                    logger.info("Exported SQL query results to file: " + fileSQLOut);
                tempList.clear();
            }
        });

        /*
        int retCount = 0;
        File dir = new File(SQL_DUMP_FOLDER);
        if (dir.exists() && dir.isDirectory()) {
            retCount = dir.listFiles().length;
        }
        return retCount; // Return the number of files generated for DL4J to help downstream
        */

    }

        /**
         * Creates a map of Primary Key and a list of values for asked columns
         *
         * @param dbName  Name of the DB where execution needs to be made
         * @param tblName Name of the concerned table
         * @param pk      Primary Key
         * @param column  A list<String> of table columns that are to be returned in the map with PK as key
         * @return TODO: Enhance this to account for multiple columns rather than just one
         */
    public Map<String, String> mapPKWithOtherColumns(String dbName, String tblName, String pk, String column) {

        Map<String, String> returnMap = new HashMap<String, String>();
        Connection conn = null;
        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);
        properties.put("useSSL", "false");
        properties.setProperty("autoReconnect", "true");
        //HashSet<String> hSet = new HashSet<String>();

        try {
            conn = DriverManager.getConnection("jdbc:mysql://" + serverName + ":" + portNumber + "/" + dbName, properties);
            Statement st = conn.createStatement();
            String query = "SELECT ?, ? FROM ?";
            //List<String> returnList = new LinkedList<String>();
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setObject(1, pk, JDBCType.VARCHAR);
            pstmt.setObject(2, column, JDBCType.VARCHAR);
            pstmt.setObject(3, tblName, JDBCType.VARCHAR);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                returnMap.put(rs.getString(1), rs.getString(2));
            }
        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Non-SQL Exception: << " + e.getMessage());
            logger.error("Run and validate input SQL query.");
            e.printStackTrace();
        }
        return returnMap;
    }


}

