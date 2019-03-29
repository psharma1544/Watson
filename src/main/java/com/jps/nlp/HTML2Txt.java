package com.jps.nlp;

import java.util.*;

import com.jaunt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTML2Txt {

    private final Logger logger = LoggerFactory.getLogger(HTML2Txt.class);


    public String dnldText(String url) {
        StringBuilder sb = new StringBuilder();
        List<String> listTags = new ArrayList<>();
        listTags.add("<div>");
        listTags.add("<p>");

        try {
            // TODO: Need to ignore text from tgtIgnoreList
            UserAgent userAgent = new UserAgent();         //create new userAgent (headless browser)
            userAgent.visit(url);

            listTags.forEach(tag -> {
                Elements elements = userAgent.doc.findEvery(tag);
                for (Element element : elements) {
                    // Get the text of the HTML element and clean it before further processing
                    StringBuilder sbTemp = new StringBuilder(cleanInput(element.getTextContent()));
                    if (sbTemp.toString() != "")
                        sb.append(sbTemp).append(" ");
                }
            });
        } catch (ExpirationException e) {         //if an HTTP/connection error occurs, handle JauntException.
            logger.warn("The web scraping Jaunt API has expired: " + e.getMessage());
            e.printStackTrace();
        } catch (JauntException e) {         //if an HTTP/connection error occurs, handle JauntException.
            logger.warn("JauntException: " + e.getMessage());
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * Cleans the input of unnecessary verbiage introduced during HTML parsing
     * @param input
     * @return
     */
    // TODO: There would definitely be better ways to handle this.

    private String cleanInput(String input){
        List<String> tgtIgnoreList = new ArrayList<>();
        tgtIgnoreList.add("&nbsp;");
        tgtIgnoreList.add("&#160;");
        tgtIgnoreList.add("&#8221");
        tgtIgnoreList.add("&#32;");
        tgtIgnoreList.add("&#8217;");
        tgtIgnoreList.add("$&#150;");
        tgtIgnoreList.add("#8226;");
        tgtIgnoreList.add("&#8212;");
        //tgtIgnoreList.add("#8226;");
        //tgtIgnoreList.add("#8226;");
        //tgtIgnoreList.add("#8226;");

        StringBuilder sbTemp = new StringBuilder(input);
        tgtIgnoreList.forEach(entryToPurge -> {
            while (sbTemp.toString().contains(entryToPurge)){
                int offset = sbTemp.indexOf(entryToPurge);
                sbTemp.delete(offset, offset+entryToPurge.length());
            }
        });
        return sbTemp.toString();
    }

}
