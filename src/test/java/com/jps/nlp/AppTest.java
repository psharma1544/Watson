package com.jps.nlp;

import com.jaunt.ExpirationException;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.*;

public class AppTest {
    private final Logger logger = LoggerFactory.getLogger(AppTest.class);
    private final String baseEdgarUrl = "https://www.sec.gov/edgar/searchedgar/companysearch.html";

    @Test(groups = {"infrastructure"})
    public void setup() {
    }

    @DataProvider(name = "db-cred-provider")
    public Object[][] commandListData() {
        String mysqlServerName = "localhost";
        String mysqlPort = "3306";
        String mysqlDbName = "markets";
        String mysqlUserName = "psharma";
        String mysqlUserPwd = "$ys8dmin";

        return new Object[][]
                {
                        {
                                mysqlServerName,
                                mysqlPort,
                                mysqlDbName,
                                mysqlUserName,
                                mysqlUserPwd
                        }
                };
    }

    @Test(dataProvider = "db-cred-provider", groups = {"infrastructure"})
    public void validateSqlConnection(String serverName, String portNumber, String dbName, String userName, String password) throws Exception {
        Connection conn = null;
        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);
        properties.put("useSSL", "false");
        properties.setProperty("autoReconnect", "true");
        final String query = "show tables";
        try {
            conn = DriverManager.getConnection("jdbc:mysql://" + serverName + ":" + portNumber + "/" + dbName, properties);

            List<String> returnList = new LinkedList<String>();
            PreparedStatement pstmtSelectAll = conn.prepareStatement(query);
            ResultSet rs = pstmtSelectAll.executeQuery();

            Assert.assertNotNull(rs);
            Assert.assertEquals(rs.next(), true);

        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Non-SQL Exception: << " + e.getMessage());
            logger.error("Run and validate input SQL query.");
            e.printStackTrace();
        }
    }

    @Test(groups = {"infrastructure"})
    public void testEdgarURLAvailability() {
        try {
            UserAgent userAgent = new UserAgent();         //create new userAgent (headless browser)
            userAgent.settings.checkSSLCerts = false;
            userAgent.visit(baseEdgarUrl);
            Assert.assertNotNull(userAgent.doc.getUrl());
            Assert.assertEquals(userAgent.doc.getUrl(), "https://www.sec.gov/edgar/searchedgar/companysearch.html");
        } catch (ExpirationException e) {         //if an HTTP/connection error occurs, handle JauntException.
            logger.warn("The web scraping Jaunt API has expired: " + e.getMessage());
            e.printStackTrace();
        } catch (JauntException e) {         //if an HTTP/connection error occurs, handle JauntException.
            logger.warn("JauntException: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
