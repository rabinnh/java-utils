/*
Copyright (c) 2018 Richard Bross

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package com.oaktreepeak.urlfetch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

/**
 * A class with the primary method being URLFetch.
 * 
 * Used to fetch URLs from http and https using either Basic or Digest
 * authentication via either GET or POST
 * 
 */
public class HttpFetch {

	// The result will be in 2 keys; Success:boolean, Content:String. Content will
	// be the error if success is false
	private static final boolean DEBUG = false;
	private int iConnectionTimeout = 10;
	
	/**
	 * Instantiate with a connection timeout in seconds
	 * 
	 * @param iConnectionTimeout - connection timeout in seconds
	 */
	public HttpFetch(int iConnectionTimeout) {
		this.iConnectionTimeout = iConnectionTimeout; 
	}

	/**
	 * Fetch a URL Authentication assumes user and password are required. If not use
	 * empty strings, not null Will determine if Basic or Digest authentication is
	 * needed based on response header If "postParams" is not null, will use POST,
	 * otherwise will use GET
	 * 
	 * The Hashmap returned will have 3 keys: "success", "httpcode", and "content"
	 * 
	 * @param sURL         - String representation of the URL
	 * @param sUser        - The user if authentication is required. Should not be
	 *                     null if there will be a challenge.
	 * @param sPW          - The password if authentication is required. Should not
	 *                     be null if there will be a challenge.
	 * @param postParams   - a key/value list of POST arguments. If not needed send
	 *                     an empty list.
	 * @param bIgnoreCerts - if 'true' will ignore invalid SSL certificates (wrong
	 *                     domain, no trust path, etc)
	 */
	@SuppressWarnings("unused")
	public HashMap<String, Object> URLFetch(String sURL, String sUser, String sPW, List<NameValuePair> postParams,
			boolean bIgnoreCerts, String sRealm) {
		
		HashMap<String, Object> mResult = new HashMap<String, Object>();

		try {
			// Create empty objects for POST and GET; we don't know which we'll use below
			HttpPost httppost = null;
			HttpGet httpget = null;

			// Crate a URL object
			URL url = new URL(sURL);

			// Now create the HttpHost object from the URL
			HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());

			// We need an HTTP client either with or without the SSLContext that ignores certs
			CloseableHttpClient httpClient = GetHttpClient(bIgnoreCerts, mResult);
			
			if (httpClient == null)
				return (mResult);

			// The HttpCLientContext
			final HttpClientContext context = HttpClientContext.create();

			// We'll need to allocate the response object below depending on type
			CloseableHttpResponse response = null;
			try {
				if (postParams != null) {
					httppost = new HttpPost(sURL);
					// Get the response
					response = httpClient.execute(targetHost, httppost, context);
				} else {
					httpget = new HttpGet(sURL);
					// Get the response
					response = httpClient.execute(targetHost, httpget, context);
				}
			} catch (SSLHandshakeException e) {
				mResult.put("success", false);
				mResult.put("httpcode", "N/A");
				mResult.put("content", GetStackTraceAsString(e));
				return (mResult);
			}

			// Add credentials for digest header
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED && sRealm != null) {
				// Change to just pass user and password
				Header authHeader = response.getFirstHeader(AUTH.WWW_AUTH);
				HeaderElement[] element = authHeader.getElements();
				if (element.length != 0) {
					AuthCache authCache = new BasicAuthCache();
					CredentialsProvider credsProvider = new BasicCredentialsProvider();
					credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(sUser, sPW));
					if (element[0].getName().startsWith("Basic")) {
						authCache.put(targetHost, new BasicScheme());
					} else if (element[0].getName().startsWith("Digest")) {
						DigestScheme digestScheme = new DigestScheme();
						digestScheme.overrideParamter("realm", sRealm);
						digestScheme.processChallenge(authHeader);
						authCache.put(targetHost, digestScheme);
					}
					context.setCredentialsProvider(credsProvider);
					context.setAuthCache(authCache);
				}
			}

			// This ensures that the resource gets cleaned up
			response = null;
			if (postParams != null) {
				httppost = new HttpPost(sURL);
				if (postParams != null) {
					httppost.setEntity(new UrlEncodedFormEntity(postParams, "UTF-8"));

					// Get the response
					response = httpClient.execute(targetHost, httppost, context);
				}
			} else {
				httpget = new HttpGet(sURL);
				// Get the response
				response = httpClient.execute(targetHost, httpget, context);
			}

			// Get the data
			HttpEntity entity = response.getEntity();
			if (DEBUG && entity != null) {
				System.out.println(response.getStatusLine());
				System.out.println("Response content length: " + entity.getContentLength());
			}

			try {

				try (BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()))) {
					String inputLine;
					String contentBuffer = "";
					while ((inputLine = in.readLine()) != null) {
						contentBuffer += inputLine;
						if (DEBUG) {
							System.out.println(sURL + " : " + inputLine);
						}
					}
					mResult.put("success", true);
					mResult.put("httpcode", response.getStatusLine());
					mResult.put("content", contentBuffer);
					EntityUtils.consume(entity);
				} catch (IOException e) {
					mResult.put("success", false);
					mResult.put("httpcode", response.getStatusLine());
					mResult.put("content", GetStackTraceAsString(e));
				}
			} finally {
				try {
					httpClient.close();

				} catch (IOException e) {
					mResult.put("success", false);
					mResult.put("httpcode", response.getStatusLine());
					mResult.put("content", GetStackTraceAsString(e));
				}
			}
		} catch (MalformedURLException | MalformedChallengeException | UnsupportedEncodingException e) {
			mResult.put("success", false);
			mResult.put("httpcode", "N/A");
			mResult.put("content", GetStackTraceAsString(e));
		} catch (ClientProtocolException e) {
			mResult.put("success", false);
			mResult.put("httpcode", "N/A");
			mResult.put("content", GetStackTraceAsString(e));
		} catch (IOException e) {
			mResult.put("success", false);
			mResult.put("httpcode", "N/A");
			mResult.put("content", GetStackTraceAsString(e));
		}
		return (mResult);
	}

	/**
	 * Retrieve a closable http client instance
	 * 
	 * @param bIgnoreCerts - If true the returned client object will ignore invalid
	 *                     certificates
	 */
	private CloseableHttpClient GetHttpClient(boolean bIgnoreCerts, HashMap<String, Object> mResult) {
		CloseableHttpClient httpClient = null;
		
		// Get ready to set the timeout
		RequestConfig config = RequestConfig.custom()
		  .setConnectTimeout(iConnectionTimeout * 1000)
		  .setConnectionRequestTimeout(iConnectionTimeout * 1000)
		  .setSocketTimeout(iConnectionTimeout * 1000).build();

		if (bIgnoreCerts) {
			// Create an SSL context that accepts certs regardless of name match or trust
			// (for self-signed)
			SSLContext sslContext;
			// Any exceptions here prevent the successful completion of the call, so print
			// the trace
			try {
				sslContext = new SSLContextBuilder().loadTrustMaterial(null, (x509CertChain, authType) -> true).build();
				httpClient = HttpClientBuilder.create().setSSLContext(sslContext)
						.setConnectionManager(new PoolingHttpClientConnectionManager(RegistryBuilder
								.<ConnectionSocketFactory>create()
								.register("http", PlainConnectionSocketFactory.INSTANCE)
								.register("https",
										new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
								.build())).setDefaultRequestConfig(config)
						.build();
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
				mResult.put("success", false);
				mResult.put("httpcode", "N/A");
				mResult.put("content", GetStackTraceAsString(e));
			}
		} else {
			httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		}
		return (httpClient);

	}

	/**
	 * Turn an exception stack trace into a string
	 * 
	 * @param - e is the exception that was caught
	 * 
	 */ 
	private String GetStackTraceAsString(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stackTraceString = sw.toString();
		return (stackTraceString);
	}
}
