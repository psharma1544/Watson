package com.jps.nlp;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.CategoriesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.MetadataOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.RelationsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SemanticRolesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SentimentOptions;
import com.ibm.watson.developer_cloud.service.exception.BadRequestException;
import com.ibm.watson.developer_cloud.service.exception.UnauthorizedException;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WatsonNLPImpl implements WatsonNLP{
	private final Logger logger = (Logger) LoggerFactory.getLogger(WatsonNLPImpl.class);
	private final String VERSION_NLU = Configuration.WATSON_NLU_VERSION;
	private final String WATSON_KEY = Configuration.WATSON_KEY;
	private final String WATSON_PWD = Configuration.WATSON_PWD;

	/**
	 * Analyzes NLP for a target which can be a URL or HTML or a text file. 
	 * <p>
	 *
	 * @param docType the type of document among one of "url", "html", "text" for the target 
	 * document whose text is to be analyzed. The URL, if selected, must be either public or 
	 * otherwise internally available to the system where this application is run.  
	 *   
	 *  @param  path Local path to a Text file or link to URL / HTML page
	 * @throws Exception on any failure.
	 * 
	 */

	// TODO: Retrieve a MAP of NLP options and enable only those that are set for retrieval ignoring others that are not. 
	public String processTgt(String docType, String symbol, String filingType, String path) {
		NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
				VERSION_NLU,	 WATSON_KEY,	 WATSON_PWD );

		// TODO: Take out hard-coded limits and let configuration control them. 
		EntitiesOptions entitiesOptions = new EntitiesOptions.Builder().emotion(true)
				.sentiment(true)
				.limit(25)
				.build();

		KeywordsOptions keywordsOptions = new KeywordsOptions.Builder()
				.emotion(true)
				.sentiment(true)
				.limit(50)
				.build();

		ConceptsOptions conceptsOptions = new ConceptsOptions.Builder()
				.limit(10)
				.build();

		CategoriesOptions categoriesOptions = new CategoriesOptions();

		MetadataOptions metadata= new MetadataOptions();

		SemanticRolesOptions semanticRoles = new SemanticRolesOptions.Builder()
				.build();

		RelationsOptions relations = new RelationsOptions.Builder()
				.build();



		Features features = new Features.Builder()
				.entities(entitiesOptions)
				.semanticRoles(semanticRoles)
				.keywords(keywordsOptions)
				.categories(categoriesOptions)
				.concepts(conceptsOptions)
				.relations(relations)
				.metadata(metadata)
				.build();

		AnalyzeOptions parameters = null;
		if (docType.equalsIgnoreCase("url"))
			parameters = new AnalyzeOptions.Builder().url(path)
			.features(features)
			.build();
		else if (docType.equalsIgnoreCase("html"))
			parameters = new AnalyzeOptions.Builder().html(path)
			.features(features)
			.build();
		else if (docType.equalsIgnoreCase("text"))
			parameters = new AnalyzeOptions.Builder().text(path)
			.features(features)
			.build();
		else { 
			logger.error("Document type not identified for path:" +path );
			logger.error("Must be one of URL / HTML / Text.");
		}


		AnalysisResults response = null;
		try{
			response = service
					.analyze(parameters)
					.execute();
			System.out.println(response);
			logger.info("Generating text dump from parameters: {} ", parameters.text());
			logger.info("Generating text dump from response: {} ", response.getAnalyzedText());
		}
		catch (UnauthorizedException e){
			logger.error("Unauthorized Access attempt for WatsonNLU: "+e.getMessage());
			logger.error("Please add IBM Watson NLU access credentials in configuration file. ");
			e.printStackTrace();
		}
		catch (BadRequestException e){
			logger.error("Bad Request Exception for WatsonNLU: "+e.getMessage());
			logger.error("Check that target web-page is publicly available.");
			logger.error("If yes, then attempt again.");
			e.printStackTrace();
		}
		// Was getting NPE on response.toString() at times.
		if (response != null)
			return response.toString();
		else
			return "";
	}

	/**
	 * @param docsType
	 * @param  listPathsAndMetaData List of local paths or links to URL / HTML page
	 * @throws Exception
	 */
	public void processTgtList(String docsType, List<String> listPathsAndMetaData) {

		/*
		Had to change this function on 2019-02-13 to index symbol and filing
		 * information into Solr index. It is possible that these changes may
		 * break something else
		 *
			 */
		for (String pathandMetadata : listPathsAndMetaData) {
			String[] data = pathandMetadata.split(",");
			String symbol = data[0];
			String filingType = data[1];
			String absPath = data[2];

			processTgt(docsType, symbol, filingType, absPath);
		}
		return;

	}

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

	public String processTgtDocAgainstTargetEntities(String docType, String path, List<String> targets) {
		NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
				VERSION_NLU,	 "9b967bcc-3074-4ba2-ae02-5e197ae977f2",	"XMElVdlKc4nh");

		SentimentOptions sentiment = new SentimentOptions.Builder()
				.targets(targets)
				.build();

		//CategoriesOptions categoriesOptions = new CategoriesOptions();

		//ConceptsOptions conceptsOptions = new ConceptsOptions.Builder().limit(2).build();

		Features features = new Features.Builder()
				.sentiment(sentiment)
				//.keywords(keywordsOptions)
				//	.categories(categoriesOptions)
				//	.concepts(conceptsOptions)
				.build();

		AnalyzeOptions parameters = null;
		if (docType.equalsIgnoreCase("url"))
			parameters = new AnalyzeOptions.Builder().url(path)
			.features(features)
			.build();
		else if (docType.equalsIgnoreCase("html"))
			parameters = new AnalyzeOptions.Builder().html(path)
			.features(features)
			.build();
		else if (docType.equalsIgnoreCase("text"))
			parameters = new AnalyzeOptions.Builder().text(path)
			.features(features)
			.build();
		else { 
			logger.error("Document type not identified for path:" +path );
			logger.error("Must be one of URL / HTML / Text.");
		}


		AnalysisResults response = null; 
		try{
			response = service
					.analyze(parameters)
					.execute();
			System.out.println(response);
		}
		catch (UnauthorizedException e){
			logger.error(e.getMessage());
			logger.error("Please add IBM Watson NLU access credentials in configuration file. ");
			e.printStackTrace();
		}
		catch (BadRequestException e){
			logger.error("Bad Request Exception for WatsonNLU: "+e.getMessage());
			logger.error("Check that target web-page is publicly available.");
			logger.error("If yes, then attempt again.");
			e.printStackTrace();
		}

		return response.toString();
	}

}