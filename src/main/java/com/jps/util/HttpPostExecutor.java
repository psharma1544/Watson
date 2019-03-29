package com.jps.finserv.markets.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class HttpPostExecutor {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(HttpPostExecutor.class);

	public ResponseEntity <String> HttpPostExecutor(String url, String authHeader, String session, String jsonPost) {
		ResponseEntity <String> response = null; 
		RestTemplate template = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();

		try{
			headers.add("Authorization", authHeader);

			/* ORIGINAL VALUES
			if (session != ""){
				headers.add("X-Endeavour-Sessionid", session);
				headers.add("Accept", "application/json"); 
				headers.add("Content-Type", "application/json");
			}
			*/

			if (session == ""){
				//headers.add("X-Endeavour-Sessionid", session);
				headers.add("Accept", "application/json"); 
				headers.add("Content-Type", "application/json");
			}
			
			HttpEntity<String> entity = new HttpEntity<String>(jsonPost, headers);
			response = template.exchange(url, HttpMethod.POST, entity, String.class);

			if (response != null){
				logger.info("Response Code: "+response.getStatusCode());
				if (url.contains("ecxngp")){
					logger.info("Received following response for the POST operation: \n\n"+response.getBody().toString());
				}
			}
			//logger.info("Received following response for the POST operation: \n\n"+response.getBody().toString());
			
		}
		catch (HttpClientErrorException errHttpAccess){
			logger.info("HTTP POST Request Failed with exception "+errHttpAccess.getMessage());
			logger.info("The JSON Used for POST Operation is following. Note that this is permitted to be empty for certain POST requests.");
			logger.info(jsonPost);
		}
		catch (HttpServerErrorException errHttpAccess){
			logger.error("HTTP POST Request Failed with exception "+errHttpAccess.getMessage());
			logger.info("The JSON Used for POST Operation is following. Note that this is permitted to be empty for certain POST requests.");
			logger.info(jsonPost);
		}
		catch (ResourceAccessException errResourceAccess){
			logger.error("HTTP POST Request Failed with exception "+errResourceAccess.getMessage());
			//logger.error(errResourceAccess.getCause());
			//System.out.println("3: For HTTP Return Code 400, check if the resource already exists in the appliance.");
		}

		catch (Exception e){
			logger.error("HTTP POST Request Failed.");
			logger.info("The JSON Used for POST Operation is following. Note that this is permitted to be empty for certain POST requests.");
			logger.info(jsonPost);
			e.printStackTrace();
		}
		return response;
	}
}
