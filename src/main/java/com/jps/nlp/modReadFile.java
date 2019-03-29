package com.jps.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic functions to read an input file
 * @author psharma
 *
 */
public class modReadFile {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(modReadFile.class);
	//String fileName = null;
	PrintWriter p=null;
	Object[] tempStrArr = null;

	public static String readFile(String fileName){
		//fileName = fileName;
		String inputJSON = "";
		String inputStr;

		try{
			File f = new File(fileName.trim());
			InputStream stream = new FileInputStream(f);

			BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			//BufferedReader bReader = new BufferedReader(new FileReader(fileName));

			while ((inputStr = streamReader.readLine()) != null)
				inputJSON = inputJSON.concat(inputStr.trim());
			streamReader.close();
		}
		catch(Exception e){
			System.out.println("Cannot read from file " + fileName + "\n");
			System.out.println("Exception: " + e.toString());
			//System.exit(1);
		}
		return inputJSON;	
	}

	public static String readPdf(String dirPath, String fileName){
		//fileName = fileName;
		String inputJSON = "";
		String inputStr;

		File dir = new File(dirPath.trim());

		List<String> returnList = new ArrayList<String>();
		PdfReader reader;
		try{
			if (dir.exists()){
				String path = dir.getAbsolutePath().concat(File.pathSeparator).concat(fileName.trim());
				reader = new PdfReader(path);


				// pageNumber = 1
				String textFromPage = PdfTextExtractor.getTextFromPage(reader, 1);
				System.out.println(textFromPage);

				reader.close();			
			}
		}
		catch(Exception e){
			System.out.println("Cannot read from file " + fileName + "\n");
			System.out.println("Exception: " + e.toString());
			//System.exit(1);
		}
		return inputJSON;	
	}


	/**
	 * Reads a file in current directory and returns it in a list of lines. Very useful for CSV parsing.
	 * @param fileName
	 * @return
	 */
	public static List<String> readFileIntoListOfStrings(String fileName){
		//fileName = fileName;
		String inputStr;

		List<String> returnList = new ArrayList<String>();
		try{
			File f = new File(fileName.trim());
			//logger.debug(System.getProperty("user.dir"));
			InputStream stream = new FileInputStream(f);
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			//BufferedReader bReader = new BufferedReader(new FileReader(fileName));

			while ((inputStr = streamReader.readLine()) != null)
				returnList.add(inputStr);
			streamReader.close();
		}
		catch(Exception e){
			logger.error("Cannot read from file " + fileName + "\n");
			logger.error("Exception: " + e.toString());
			e.printStackTrace();
			//System.exit(1);
		}
		return returnList;	
	}

	/**
	 * Reads a file from the given directory path and returns it in a list of lines. Useful for CSV parsing.
	 * @param fileName
	 * @return
	 */

	public static List<String> readFileIntoListOfStringsDirectory(String dirPath, String fileName){
		//fileName = fileName;
		File dir = new File(dirPath.trim());
		String inputStr;

		List<String> returnList = new ArrayList<String>();
		try{
			if (dir.exists()){
				String path = dir.getAbsolutePath().concat(File.pathSeparator).concat(fileName.trim());
				//File f = new File(path);
				File f = new File(dir, fileName.trim());
				InputStream stream = new FileInputStream(f);

				BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
				//BufferedReader bReader = new BufferedReader(new FileReader(fileName));

				while ((inputStr = streamReader.readLine()) != null)
					returnList.add(inputStr);
				streamReader.close();
			}
		}
		catch(Exception e){
			logger.error("Cannot read from file " + fileName + "\n");
			logger.error("Exception: " + e.toString());
			//System.exit(1);
		}
		return returnList;	
	}

}