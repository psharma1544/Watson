package com.jps.nlp;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Output data to a file in CSV form
 * @author psharma
 */
public class modOutCSV {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(modOutCSV.class);
	String fileName = null;
	PrintWriter p=null;
	Object[] tempStrArr = null;
	/**
	 * Constructor needs the name for the file to create
	 * @param fileName
	 */
	public modOutCSV(String fileName){
		this.fileName = fileName;
		try{
			p = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)),true);
		}catch(Exception e){
			System.out.println("Cannot create the file " + fileName + "\n");
			System.out.println("Exception: " + e.toString());
			System.exit(1);
		}
	}

	/**
	 * Writes a 2 dimensional data store to the file
	 * @param data The 2 dimensional store
	 * @param NUMCOLS The number of columns the data store has
	 */
	//public void write(Object[][] data, int NUMCOLS){
	public void write(Object[][] data, int NUMCOLS){
		for(int i=0; i<data.length; i++){
			//for (Iterator<Object[]> ite = data.iterator(); ite.hasNext(); ){
			tempStrArr = data[i];
			//for(int j=0; j<NUMCOLS; j++){
			for(int j=0; j<tempStrArr.length; j++){

				p.print(tempStrArr[j]);
				p.print(",");

			}
			//the println method will print an OS specific new line
			p.println();
		}
		p.close();
	}

	public void writeList(List<String> listInput){

		try{
		String collect = listInput.stream().collect(Collectors.joining((System.getProperty("line.separator"))));
		//logger.debug(collect);

		p.write(collect);
		p.close();
		}

		catch (Exception e){
			e.printStackTrace();
		}


	}
}
