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
		
		HashMap<String, Object> m = null;
		
		// GET
		String sGetURL = "https://192.168.2.80/query/info";
		m = d.URLFetch(sGetURL, "user", "pw", null, true, "thermostat");
		for (HashMap.Entry<String, Object> entry : m.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}

		// POST
		List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("away", "1"));
		String sPostURL = "https://192.168.2.80/settings?pin=1111";
		m = d.URLFetch(sPostURL, "user", "pw", postParameters, true, "thermostat");
		for (HashMap.Entry<String, Object> entry : m.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}
		
		sPostURL = "https://192.168.2.80/control";		
		postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("mode", "1"));
		postParameters.add(new BasicNameValuePair("heattemp", "60"));
		postParameters.add(new BasicNameValuePair("cooltemp", "65"));
		m = d.URLFetch(sPostURL, "user", "pw", postParameters, true, "thermostat");
		for (HashMap.Entry<String, Object> entry : m.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}
	}
}
