package com.jps.nlp;

import java.io.*;
import java.util.*;

import com.jaunt.*;
import com.jaunt.component.*;

import java.io.IOException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author psharma
 */
public class SecEdgar {
    private final Logger logger = LoggerFactory.getLogger(SecEdgar.class);
    private final boolean DOWNLOADING_ENABLED = Configuration.DOWNLOAD_SEC;
    SecEdgarDataImpl secEdgarData = new SecEdgarDataImpl();

    /**
     * Manages all function calls to retrieve financial returns for a set of public companies
     *
     * @param mapReturns
     */
    public List<String> retrieveLinks(Map<String, Map<String, Integer>> mapReturns) {

        if (!DOWNLOADING_ENABLED) {
            logger.warn("Downloading of new documents disabled in configuration!");
            return null; // Handled downstream but need to find better alternatives
        }

        Set<String> symbols = mapReturns.keySet();
        List<String> listReturnUrls = new LinkedList<>();
        for (String symbol : symbols) {
            String urlBaseCompany = secEdgarData.urlBaseEdgar(symbol);
            //logger.debug("Base EDGAR URL for '"+symbol+"' is: "+urlBaseCompany);
            Map<String, Integer> mapTypesAndCount = mapReturns.get(symbol);
            if ((mapTypesAndCount == null) || (mapTypesAndCount.isEmpty())) {
                logger.error("Empty map received for symbol: " + symbol);
                continue;
            }

            Set<String> setTypesFinancialDocs = mapTypesAndCount.keySet();
            for (String typeFinancialDoc : setTypesFinancialDocs) {
                String urlCompanyFilingType = secEdgarData.urlFilingTypeEdgar(symbol, urlBaseCompany, typeFinancialDoc);
                //logger.debug("Base EDGAR URL for '"+symbol+"'s '"+typeFinancialDoc+ "' documents is: "+baseUrlCompanyFilingType);
                String retrievalType = "Documents";
                int numDocs = mapTypesAndCount.get(typeFinancialDoc);
                if (numDocs > 0) {
                    List<String> listUrls = secEdgarData.urlsSpecificSingleFilingEdgarPage(urlCompanyFilingType, numDocs, retrievalType);
                    for (String url : listUrls) {
                        for (String docType : setTypesFinancialDocs) {
                            String urlLink = secEdgarData.urlSingleReturnDetailsEdgar(url, docType);
                            if ((urlLink != null) && (urlLink != "")) {
                                listReturnUrls.add(urlLink);
                                boolean downloaded = secEdgarData.downloadSecFilings(urlLink, symbol, typeFinancialDoc);
                            }
                        }
                    }
                }
            }
        }
        return listReturnUrls;
    }
}
