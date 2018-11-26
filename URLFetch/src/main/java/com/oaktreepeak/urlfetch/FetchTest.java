package com.oaktreepeak.urlfetch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

// This is just a simple convenience function that can be changed to perform simple tests
// The example below tested setting and getting settings from a smart Venstar thermostat
public class FetchTest {

	// Main, takes 4 arguments (see println below)
	public static void main(String[] args) {
		HttpFetch d = new HttpFetch(10);

		// Test post URL
		HashMap<String, Object> m = null;
		List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("away", "1"));
		
		// POST
		String sPostURL = "https://192.168.2.80/settings?pin=9999";
		m = d.URLFetch(sPostURL, "user", "pw", postParameters, true);
		for (HashMap.Entry<String, Object> entry : m.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}

		// GET
		String sGetURL = "https://192.168.0.80/query/info";
		m = d.URLFetch(sGetURL, "user", "pw", null, true);
		for (HashMap.Entry<String, Object> entry : m.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}
	}
}
