package com.jps.finserv.markets.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class HttpDeleteExecutor {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(HttpDeleteExecutor.class);
	
	public boolean HttpDeleteExecutor(String fullURL, String authHeader, String session) throws JsonParseException, JsonMappingException, IOException {
		boolean ResourceDeleted = false;
		RestTemplate template = new RestTemplate();

		logger.info("The resource to be deleted is at URL "+fullURL);

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", authHeader);
			headers.add("Accept", "application/json");
			headers.add("Content-Type", "application/json");
			headers.add("X-Endeavour-Sessionid", session);

			HttpEntity<String> entity = new HttpEntity<String>("", headers);
			ResponseEntity <String> response = template.exchange(fullURL, HttpMethod.DELETE, entity, String.class);

			if (response != null){
				if (response.hasBody()){
					String retResponse = response.getBody().toString();
				}
				logger.info("Response Code: "+response.getStatusCode());
				if (response.getStatusCode().is2xxSuccessful()){
					ResourceDeleted = true;
				}
			}
		}
		catch (ResourceAccessException errResourceAccess){
			logger.info(errResourceAccess.getMessage());
		}
		catch (HttpClientErrorException errHttpAccess){
			logger.info(errHttpAccess.getMessage());
			logger.info("HTTP DELETE Request Failed for URL"+ fullURL);
		}
		catch (HttpServerErrorException errHttpAccess){
			logger.info(errHttpAccess.getMessage());
			logger.info("HTTP DELETE Request Failed for URL"+ fullURL);
		}
		catch (Exception e){
			e.printStackTrace();
		}

		/*
		 * TODO: Check for return codes. If they are expected in (200, 204, 205) then only return success.
		 */
		return ResourceDeleted;
	}

	public boolean HttpDeleteExecutor(String baseURL, String api, String authHeader, String session, String objectID) throws JsonParseException, JsonMappingException, IOException {
		boolean ResourceDeleted = false;
		RestTemplate template = new RestTemplate();
		String url = baseURL+api+"/"+objectID;
		System.out.println("The resource to be deleted is at URL "+url);

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", authHeader);
			headers.add("Accept", "application/json");
			headers.add("Content-Type", "application/json");
			headers.add("X-Endeavour-Sessionid", session);

			HttpEntity<String> entity = new HttpEntity<String>("", headers);
			ResponseEntity <String> response = template.exchange(url, HttpMethod.DELETE, entity, String.class);

			if (response.hasBody()){
				String retResponse = response.getBody().toString();
			}
			logger.info("Response Code: "+response.getStatusCode());
			if (response.getStatusCode().is2xxSuccessful()){
				ResourceDeleted = true;
			}
		}
		catch (ResourceAccessException errResourceAccess){
			logger.info(errResourceAccess.getMessage());
		}
		catch (HttpClientErrorException errHttpAccess){
			logger.info(errHttpAccess.getMessage());
			logger.info("HTTP DELETE Request Failed for URL"+ url);
		}
		catch (HttpServerErrorException errHttpAccess){
			logger.info(errHttpAccess.getMessage());
			logger.info("HTTP DELETE Request Failed for URL"+ url);
		}
		
		catch (Exception e){
			e.printStackTrace();
		}

		/*
		 * TODO: Check for return codes. If they are expected in (200, 204, 205) then only return success.
		 */
		return ResourceDeleted;
	}

}
