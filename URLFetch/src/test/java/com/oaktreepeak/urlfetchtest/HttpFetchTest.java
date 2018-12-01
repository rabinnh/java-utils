package com.oaktreepeak.urlfetchtest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import com.oaktreepeak.urlfetch.*;

class HttpFetchTest {
	
	private static int iTimeOut = 10;
	private static String httpNoCredURL;;
	private static String httpsNoCredURL;
	private static String httpsBasicAuthURL;
	private static String httpsDigiestAuthURL;
	private static String testUser;
	private static String testPW;

    // Read properties
	@BeforeAll
    public static void beforeClass() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("HttpFetchTest.properties");
		Properties properties = new Properties();
		try {
			properties.load(input);
			input.close();
			// To store in this object
			HttpFetchTest.httpNoCredURL = properties.getProperty("httpNoCredURL");
			HttpFetchTest.httpsNoCredURL = properties.getProperty("httpsNoCredURL");
			// To pass to Control
			HttpFetchTest.httpsBasicAuthURL = properties.getProperty("httpsBasicAuthURL");
			HttpFetchTest.httpsDigiestAuthURL = properties.getProperty("httpsDigiestAuthURL");
			HttpFetchTest.testUser = properties.getProperty("testUser");
			HttpFetchTest.testPW = properties.getProperty("testPW");
			HttpFetchTest.iTimeOut = Integer.parseInt(properties.getProperty("iTimeOut", "10"));     			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
    }
    
	// Test a simple GET on a non-SSL URL with no other requirements
	@Test
	void fetchHttpURLGetNoCreds() {
		HttpFetch cFetch = new HttpFetch(iTimeOut);
		HashMap<String, Object> mMap = cFetch.URLFetch(httpNoCredURL, null, null, null,false, null);
		if (!(boolean) mMap.get("success")) {
			fail(String.format("Request failed on URL %s with exception %s", httpNoCredURL, (String) mMap.get("content")));
		}
	}
	
	// Test a simple GET on a SSL URL with no other requirements
	@Test
	void fetchHttpsURLGetNoCreds() {
		HttpFetch cFetch = new HttpFetch(iTimeOut);
		HashMap<String, Object> mMap = cFetch.URLFetch(httpsNoCredURL, null, null, null,false, null);
		if (!(boolean) mMap.get("success"))
			fail(String.format("Request failed on URL %s with exception %s", httpNoCredURL, (String) mMap.get("content")));
	}

	// Test a simple GET on a SSL URL with Basic auth
	@Test
	void fetchHttpsURLBasicCreds() {
		HttpFetch cFetch = new HttpFetch(iTimeOut);
		HashMap<String, Object> mMap = cFetch.URLFetch(httpsBasicAuthURL, testUser, testPW, null,false, null);
		if (!(boolean) mMap.get("success"))
			fail(String.format("Request failed on URL %s with exception %s", httpNoCredURL, (String) mMap.get("content")));
	}

	// Test a simple GET on a SSL URL with Digest auth
	@Test
	void fetchHttpsURLGetDigestCreds() {
		HttpFetch cFetch = new HttpFetch(iTimeOut);
		HashMap<String, Object> mMap = cFetch.URLFetch(httpsDigiestAuthURL, testUser, testPW, null,false, null);
		if (!(boolean) mMap.get("success"))
			fail(String.format("Request failed on URL %s with exception %s", httpNoCredURL, (String) mMap.get("content")));
	}

}
