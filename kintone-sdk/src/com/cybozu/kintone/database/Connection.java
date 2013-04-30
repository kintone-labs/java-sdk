//   Copyright 2013 Cybozu
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.cybozu.kintone.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import sun.misc.BASE64Encoder;

/**
 * kintone data access class 
 *
 */
public class Connection {

	private final String AUTH_HEADER = "X-Cybozu-Authorization";
	private final String JSON_CONTENT = "application/json";
	private final String API_PREFIX = "/k/v1/";
	private final String BOUNDARY = "boundary_aj8gksdnsdfakj342fs3dt3stk8g6j32";
	
	private final String SSL_KEY_STORE = "javax.net.ssl.keyStore";
	private final String SSL_KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword";
	
	private String domain;
	private String auth;
	private Proxy proxy;
	private boolean trustAllHosts;  // for debug
	private boolean useClientCert;
	private HashMap<String, String> headers = new HashMap<String, String>();

	/**
	 * Constructor
	 * @param domain domain name. if your FQDN is "example.cybozu.com", domain name is "example"
	 * @param login login name
	 * @param password password of the login name
	 */
	public Connection(String domain, String login, String password) {
		this.trustAllHosts = false;
		this.useClientCert = false;
		this.domain = domain;
		this.auth = (new BASE64Encoder()).encode(new String(login + ":"
				+ password).getBytes());
	}

	private URL getURL(String api) throws MalformedURLException {
		StringBuilder sb = new StringBuilder();
		sb.append("https://" + this.domain);
		if (this.useClientCert) {
			sb.append(".s");
		}
		sb.append(".cybozu.com" + API_PREFIX);
		if (api != null) {
			sb.append(api);
		}
		
		return new URL(new String(sb));
	}
	/**
	 * Clear the settings of this connection
	 */
	public void close() {
		auth = null;
		proxy = null;
		headers.clear();
		this.trustAllHosts = false;
		this.useClientCert = false;
		Authenticator.setDefault(null);
	}

	/**
	 * Return if this connection trusts all hosts (for debug)
	 * @return true if this connection trusts all hosts
	 */
	public boolean isTrustAllHosts() {
		return trustAllHosts;
	}

	/**
	 * Set to trust all hosts without verifying (for debug)
	 * @param trustAllHosts
	 */
	public void setTrustAllHosts(boolean trustAllHosts) {
		this.trustAllHosts = trustAllHosts;
	}

	/**
	 * Set basic authentication password
	 * @param username
	 * @param password
	 */
	public void setBasicAuth(String username, String password) {
		final String static_username = username;
        final String static_password = password;
        
		Authenticator.setDefault(new Authenticator() {
		    protected PasswordAuthentication getPasswordAuthentication()
		    {
		    	return new PasswordAuthentication(static_username, static_password.toCharArray());
		    }
		});
	}
	/**
	 * Add a user customized header
	 * @param name header name
	 * @param value header value
	 */
	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	/**
	 * Set proxy
	 * @param host proxy host
	 * @param port proxy port
	 */
	public void setProxy(String host, int port) {
		this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
	}

	/**
	 * Set client certificate authentication
	 * @param cert cert file path
	 * @param password cert password
	 */
	public void setClientCert(String cert, String password) {
		System.setProperty(SSL_KEY_STORE, cert);
		System.setProperty(SSL_KEY_STORE_PASSWORD, password);

		this.useClientCert = true;
	}
	
	private void certificate(HttpsURLConnection conn) throws DBException {
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());  
			tmf.init((KeyStore)null);  
			
			KeyStore key_store = KeyStore.getInstance("PKCS12");
			char[] key_pass = System.getProperty(SSL_KEY_STORE_PASSWORD).toCharArray();
			key_store.load(new FileInputStream(System.getProperty(SSL_KEY_STORE)), key_pass);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(key_store, key_pass);
			
			SSLContext sslcontext= SSLContext.getInstance("SSL");
		
			sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
			conn.setSSLSocketFactory(sslcontext.getSocketFactory());
		} catch (KeyManagementException e) {
			throw new DBException("key management exception");
		} catch (KeyStoreException e) {
			throw new DBException("key store exception");
		} catch (CertificateException e) {
			throw new DBException("certificate exception");
		} catch (NoSuchAlgorithmException e) {
			throw new DBException("no such algorighm exception");
		} catch (IOException e) {
			throw new DBException("io exception");
		} catch (UnrecoverableKeyException e)  {
			throw new DBException("unrecoverable key exception");
		}
	}
	
	private static void trustAllHosts(HttpsURLConnection conn) {

		X509TrustManager easyTrustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
			throws java.security.cert.CertificateException {
			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
			throws java.security.cert.CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

		};

		TrustManager[] trustAllCerts = new TrustManager[] {easyTrustManager};

		try {
			SSLContext sc = SSLContext.getInstance("SSL");

			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			conn.setSSLSocketFactory(sc.getSocketFactory());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class VerifyEverythingHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String arg0, SSLSession arg1) {
			return false;
		}
	}

	/**
	 * Request to kintone
	 * @param method GET, POST, PUT or DELETE
	 * @param api api file path and query string
	 * @param body request data body
	 * @return response string
	 * @throws DBException
	 */
	public String request(String method, String api, String body) throws DBException {
		return request(method, api, body, null);
	}
	
	/**
	 * Request to kintone
	 * @param method GET, POST, PUT or DELETE
	 * @param api api file path and query string
	 * @param body request data body
	 * @param outFile download file to the stream
	 * @return response string
	 * @throws DBException
	 */
	public String request(String method, String api, String body, File outFile) throws DBException {
		HttpsURLConnection conn = null;
		String response = null;

		
		URL url;
		try {
			url = this.getURL(api);
		} catch (MalformedURLException e1) {
			throw new DBException("invalid url");
		}
		
		try {
			if (this.proxy == null) {
				conn = (HttpsURLConnection)url.openConnection();
			} else {
				conn = (HttpsURLConnection)url.openConnection(proxy);
			}
			if (this.trustAllHosts) {
				trustAllHosts(conn);
				conn.setHostnameVerifier(new VerifyEverythingHostnameVerifier());
			}

			if (this.useClientCert) {
				certificate(conn);
			}
			
			conn.setRequestProperty(AUTH_HEADER, this.auth);

			conn.setRequestMethod(method);
		} catch (IOException e) {
			throw new DBException("can not open connection");
		}
		boolean post = false;
		if (method.equals("PUT") || method.equals("POST")) {
			post = true;
		}

		if (post) {
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", JSON_CONTENT);
		}
		try {
			conn.connect();
		} catch (IOException e) {
			throw new DBException("cannot connect to host");
		}

		if (post) {
			// send request
			OutputStream os;
			try {
				os = conn.getOutputStream();
			} catch (IOException e) {
				throw new DBException("an error occured while sending data");
			}
			PrintStream ps = new PrintStream(os);
			ps.print(body);
			ps.close();
		}
		
		// receive response
		try {
			int statusCode = conn.getResponseCode();
	        if (statusCode == 404) {
	        	throw new DBNotFoundException("not found");
	        }
	        if (statusCode != 200) {
	        	InputStream err = conn.getErrorStream();
	        	response = streamToString(err);
	        	throw new DBException("http status error(" + statusCode + "): " + response);
	        }
			InputStream is = conn.getInputStream();
			if (outFile != null) {
				OutputStream os = new FileOutputStream(outFile);
				byte[] buffer = new byte[1024];
				int n = 0;
				while (-1 != (n = is.read(buffer))) {
					os.write(buffer, 0, n);
				}
			} else {
				response = streamToString(is);
			}
			is.close();
		} catch (IOException e) {
			throw new DBException("an error occured while receiving data");
		}

		//System.out.println(response);
		return response;

	}
	
	/**
	 * Upload file
	 * @param contentType content type
	 * @param file uploaded file
	 * @return file key of the uploaded file
	 * @throws DBException
	 */
	private String upload(String contentType, File file) throws DBException {
		HttpsURLConnection conn;
		String response = null;
		
		URL url;
		try {
			url = this.getURL("file.json");

			if (this.proxy == null) {
				conn = (HttpsURLConnection)url.openConnection();
			} else {
				conn = (HttpsURLConnection)url.openConnection(proxy);
			}
			if (this.trustAllHosts) {
				trustAllHosts(conn);
				conn.setHostnameVerifier(new VerifyEverythingHostnameVerifier());
			}

			if (this.useClientCert) {
				certificate(conn);
			}
			
			conn.setRequestProperty(AUTH_HEADER, this.auth);

			conn.setRequestMethod("POST");
		} catch (Exception e) {
			throw new DBException("invalid url");
		}
		
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
		
		try {
			conn.connect();
		} catch (IOException e) {
			throw new DBException("cannot connect to host");
		}

		OutputStream os;
		InputStream isFile;
		
		try {
			isFile = new FileInputStream(file.getAbsolutePath());
		} catch (FileNotFoundException e1) {
			throw new DBNotFoundException("cannot open file");
		}
		
		try {
			os = conn.getOutputStream();
			PrintStream ps = new PrintStream(os);
			ps.print("--" + BOUNDARY + "\r\n");
			ps.print("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
			ps.print("Content-Type: " + contentType + "\r\n\r\n");
			byte[] buffer = new byte[1024];
			int n = 0;
			while (-1 != (n = isFile.read(buffer))) {
				os.write(buffer, 0, n);
			}
			ps.print("\r\n--" + BOUNDARY + "--\r\n");
			ps.close();
		} catch (IOException e) {
			throw new DBException("an error occured while sending data");
		}
		
		// receive response
		try {
			int statusCode = conn.getResponseCode();
	        if (statusCode == 404) {
	        	throw new DBNotFoundException("not found");
	        }
	        if (statusCode != 200) {
	        	InputStream err = conn.getErrorStream();
	        	response = streamToString(err);
	        	throw new DBException("http status error(" + statusCode + "): " + response);
	        }
			InputStream is = conn.getInputStream();
			response = streamToString(is);
			is.close();
		} catch (IOException e) {
			throw new DBException("an error occured while receiving data");
		}

		JsonParser parser = new JsonParser();
		String fileKey = null;
		try {
			fileKey = parser.jsonToFileKey(response);
		} catch (IOException e) {
			throw new ParseException("failed to parse json to filekey");
		}
		return fileKey;
	}
	
	private String streamToString(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		char[] b = new char[1024];
		int line;
		while (0 <= (line = reader.read(b))) {
			sb.append(b, 0, line);
		}

		reader.close();
		return new String(sb);
	}
	
	/**
	 * Select records
	 * @param app application id
	 * @param query query string
	 * @param columns column names if needed
	 * @return ResultSet object
	 * @throws DBException
	 */
	public ResultSet select(long app, String query, String[] columns) throws DBException {

		try {
			query = URLEncoder.encode(query, "UTF-8" );
		} catch (UnsupportedEncodingException e) {
		}
		StringBuilder sb = new StringBuilder();
		sb.append("app=");
		sb.append(app);
		sb.append("&query=");
		sb.append(query);
		if (columns != null) {
			int i = 0;
			for (String column: columns) {
				sb.append("&fields[" + i + "]=");
				sb.append(column);
				i++;
			}
		}
		String api = new String(sb);
		String response = request("GET", "records.json?" + api, null);
		JsonParser parser = new JsonParser();
		ResultSet rs = null;;
		try {
			rs = parser.jsonToResultSet(this, response);
		} catch (IOException e) {
			throw new ParseException("failed to parse json to reseltset");
		}

		return rs;
	}

	/**
	 * Insert a new record
	 * @param app application id
	 * @param record Record object to be inserted
	 * @throws DBException
	 */
	public void insert(long app, Record record) throws DBException {
		insert(app, new Record[]{record});
	}

	/**
	 * Insert new records
	 * @param app application id
	 * @param records an array of Record objects to be inserted
	 * @throws DBException
	 */
	public Long[] insert(long app, Record[] records) throws DBException {
		
		JsonParser parser = new JsonParser();
		String json;
		try {
			json = parser.recordsToJsonForInsert(app, records);
		} catch (IOException e) {
			throw new ParseException("failed to encode to json");
		}
		
		String response = request("POST", "records.json", json);
		
		try {
			return parser.jsonToIDs(response);
		} catch (IOException e) {
			throw new ParseException("failed to parse json to id list");
		}
	}

	/**
	 * Update a record
	 * @param app application id
	 * @param id record number of the updated record
	 * @param record updated record object
	 * @throws DBException
	 */
	public void update(long app, long id, Record record) throws DBException {
		update(app, new Long[]{id}, record);
	}

	/**
	 * Update records
	 * @param app application id
	 * @param ids an array of record numbers of the updated records
	 * @param record updated record object
	 * @throws DBException
	 */
	public void update(long app, Long[] ids, Record record) throws DBException {
		JsonParser parser = new JsonParser();
		String json;
		try {
			json = parser.recordsToJsonForUpdate(app, ids, record);
		} catch (IOException e) {
			throw new ParseException("failed to encode to json");
		}
	
		try {
			request("PUT", "records.json", json);
		} catch (DBNotFoundException e) {
			
		}
	}


	/**
	 * Update records
	 * @param app application id
	 * @param query query string to determine the updated records
	 * @param record updated record object
	 * @throws DBException
	 */
	public void update(long app, String query, Record record) throws DBException {
		String[] fields = {};
		ResultSet rs = select(app, query, fields);
		List<Long> ids = new ArrayList<Long>();
		
		if (rs.size() == 0) return;
		
		while(rs.next()) {
			ids.add(rs.getId());
		}
		
		update(app, ids.toArray(new Long[0]), record);
		
	}

	/**
	 * Delete a record
	 * @param app application id
	 * @param id record number to be deleted
	 * @throws DBException
	 */
	public void delete(long app, long id) throws DBException {		
		delete(app, new Long[]{id});
	}

	/**
	 * Delete records
	 * @param app application id
	 * @param ids an array of record numbers to be deleted
	 * @throws DBException
	 */
	public void delete(long app, Long[] ids) throws DBException {
		StringBuilder sb = new StringBuilder();
		sb.append("app=");
		sb.append(app);
		int i = 0;
		for (Long id: ids) {
			sb.append("&ids[" + i + "]=");
			sb.append(id);
			i++;
		}
		String api = new String(sb);
	
		try {
			request("DELETE", "records.json?" + api, null);
		} catch (DBNotFoundException e) {
			
		}
	}

	/**
	 * Delete records
	 * @param app application id
	 * @param query query string to determine the deleted records
	 * @throws DBException
	 */
	public void delete(long app, String query) throws DBException {
		String[] fields = {};
		ResultSet rs = select(app, query, fields);
		List<Long> ids = new ArrayList<Long>();
		
		if (rs.size() == 0) return;
		
		while(rs.next()) {
			ids.add(rs.getId());
		}
		try {
			delete(app, ids.toArray(new Long[0]));
		} catch (DBNotFoundException e) {
			
		}
	}

	/**
	 * Upload file
	 * @param contentType content type
	 * @param file file object to be uploaded
	 * @return file key
	 * @throws DBException
	 */
	public String uploadFile(String contentType, File file) throws DBException {

		return upload(contentType, file);
	}

	/**
	 * Download file
	 * @param fileKey file key
	 * @return file object
	 * @throws IOException
	 * @throws DBException
	 */
	public File downloadFile(String fileKey) throws IOException, DBException {
		File tempFile = File.createTempFile(fileKey, null);
		
		request("GET", "file.json?fileKey=" + fileKey, null, tempFile);
		return tempFile;
	}
}
