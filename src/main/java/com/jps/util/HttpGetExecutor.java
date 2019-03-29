package com.jps.finserv.markets.util;

import java.net.URI;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class HttpGetExecutor {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(HttpGetExecutor.class);

	public String HttpGetExecutor(String baseURL, String api, String authHeader, String session, String objectID) {
		RestTemplate template = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		String url = baseURL+api+"/"+objectID;
		String retResponse = "";
		try {
			headers.add("Authorization", authHeader);
			headers.add("Accept", "application/json");
			headers.add("Content-Type", "application/json");
			headers.add("X-Endeavour-Sessionid", session);

			HttpEntity<String> entity = new HttpEntity<String>("", headers);

			//ResponseEntity <String> response = template.getForEntity("https://172.20.2.103:8443/api/ibmsvc", String.class);
			//ResponseEntity <String> response = template.getForEntity("https://172.20.2.103:8443/api/ibmsvc", String.class, headers);

			ResponseEntity <String> response = template.exchange(url, HttpMethod.GET, entity, String.class);
			if (response != null){
				retResponse = response.getBody().toString();
				logger.info("Response Code: "+response.getStatusCode());
				//logger.debug(retResponse);
			}
		}
		catch (ResourceAccessException errResourceAccess){
			logger.error(errResourceAccess.getMessage());
		}
		catch (HttpClientErrorException errHttpAccess){
			logger.error(errHttpAccess.getMessage());
			logger.error("HTTP GET Request Failed for URL"+ url);
		}
		catch (HttpServerErrorException errHttpAccess){
			logger.error(errHttpAccess.getMessage());
			logger.error("HTTP GET Request Failed for URL"+ url);
		}
		catch (Exception e){
			logger.error("HTTP Get Request Failed for URL "+url+ " with Exception:"+e.getMessage());
		}

		return retResponse;
	}

	
	public String HttpGetExecutor(String url, String authHeader, String session) {
		RestTemplate template = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		//String url = baseURL+api+"/"+objectID;
		String retResponse = "";
		try {
			headers.add("Authorization", authHeader);
			headers.add("Accept", "application/json");
			headers.add("Content-Type", "application/json");
			headers.add("X-Endeavour-Sessionid", session);

			HttpEntity<String> entity = new HttpEntity<String>("", headers);

			ResponseEntity <String> response = template.exchange(url, HttpMethod.GET, entity, String.class);
			if (response != null){
				retResponse = response.getBody().toString();
				logger.info("Response Code: "+response.getStatusCode());
				//logger.debug(retResponse);
			}
		}
		catch (ResourceAccessException errResourceAccess){
			logger.error(errResourceAccess.getMessage());
		}
		catch (HttpClientErrorException errHttpAccess){
			logger.error(errHttpAccess.getMessage());
			logger.error("HTTP GET Request Failed for URL"+ url);
		}
		catch (HttpServerErrorException errHttpAccess){
			logger.error(errHttpAccess.getMessage());
			logger.error("HTTP GET Request Failed for URL"+ url);
		}
		catch (Exception e){
			logger.error("HTTP Get Request Failed for URL "+url+ " with Exception:"+e.getMessage());
		}

		return retResponse;
	}

	// See the need here: https://stackoverflow.com/questions/21819210/using-resttemplate-in-spring-exception-not-enough-variables-available-to-expan
	public String HttpGetExecutor(String url, String authHeader, String session, String input) {
		RestTemplate template = new RestTemplate();
		//template.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		HttpHeaders headers = new HttpHeaders();
		String retResponse = "";
		try {
			headers.add("Authorization", authHeader);
			headers.add("Accept", "application/json");
			headers.add("Content-Type", "application/json");
			headers.add("X-Endeavour-Sessionid", session);

			HttpEntity<String> entity = new HttpEntity<String>("", headers);

			//ResponseEntity <String> response = template.getForEntity("https://172.20.2.103:8443/api/ibmsvc", String.class);
			//ResponseEntity <String> response = template.getForEntity("https://172.20.2.103:8443/api/ibmsvc", String.class, headers);

			ResponseEntity <String> response = template.exchange(url, HttpMethod.GET, entity, String.class, input);
			if (response != null){
				retResponse = response.getBody().toString();
				logger.info("Response Code: "+response.getStatusCode());
				//logger.debug(retResponse);
			}
		}
		catch (ResourceAccessException errResourceAccess){
			logger.error(errResourceAccess.getMessage());
		}
		catch (HttpClientErrorException errHttpAccess){
			logger.error(errHttpAccess.getMessage());
			logger.error("HTTP GET Request Failed for URL"+ url);
		}
		catch (HttpServerErrorException errHttpAccess){
			logger.error(errHttpAccess.getMessage());
			logger.error("HTTP GET Request Failed for URL"+ url);
		}
		catch (Exception e){
			logger.error("HTTP Get Request Failed for URL "+url+ " with Exception:"+e.getMessage());
		}

		return retResponse;
	}

}
