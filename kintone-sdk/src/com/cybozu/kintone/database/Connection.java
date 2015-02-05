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
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
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
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import sun.misc.BASE64Encoder;

import com.cybozu.kintone.database.exception.DBException;
import com.cybozu.kintone.database.exception.DBNotFoundException;
import com.cybozu.kintone.database.exception.ParseException;

/**
 * kintone data access class.
 * 
 */
public class Connection {
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    
    private final String AUTH_HEADER = "X-Cybozu-Authorization";
    private final String API_TOKEN = "X-Cybozu-API-Token";
    private final String JSON_CONTENT = "application/json";
    private final String API_PREFIX = "/k/v1/";
    private final String GUEST_API_PREFIX = "/k/guest/%d/v1/";
    
    private final String BOUNDARY = "boundary_aj8gksdnsdfakj342fs3dt3stk8g6j32";
    private final String USER_AGENT_KEY = "User-Agent";
    private final String USER_AGENT_VALUE = "kintone-SDK 1.0";
    
    private final String SSL_KEY_STORE = "javax.net.ssl.keyStore";
    private final String SSL_KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword";

    private String domain;
    private String auth;
    private String apiToken;
    private Proxy proxy;
    private String userAgent = USER_AGENT_VALUE;
    private boolean trustAllHosts; // for debug
    private boolean useClientCert;
    private long guestSpaceId = -1;
    private HashMap<String, String> headers = new HashMap<String, String>();

    /**
     * Constructor
     * 
     * @param domain
     *            FQDN.
     *            for example "example1.cybozu.com" or "example2.cybozu.cn" or "example3.kintone.com" .
     * @param login
     *            login name
     * @param password
     *            password of the login name
     */
    public Connection(String domain, String login, String password) {
        this.trustAllHosts = false;
        this.useClientCert = false;
        this.domain = domain;
        this.auth = (new BASE64Encoder()).encode((login + ":" + password)
                .getBytes());
        this.apiToken = null;
    }
    
    /**
     * Constructor
     * 
     * @param domain
     *            FQDN.
     *            for example "example1.cybozu.com" or "example2.cybozu.cn" or "example3.kintone.com" .
     * @param apiToken
     *            api Token
     */
    public Connection(String domain, String apiToken) {
        this.trustAllHosts = false;
        this.useClientCert = false;
        this.domain = domain;
        this.auth = null;
        this.apiToken = apiToken;
    }

    /**
     * Generates the URL of the API.
     * 
     * @param api 
     *            path to api
     * @return
     *            generated url
     * @throws MalformedURLException
     */
    private URL getURL(String api) throws MalformedURLException {
        StringBuilder sb = new StringBuilder();
        sb.append("https://" + this.domain);
        if (!domain.contains(".")) {
            if (this.useClientCert) {
                sb.append(".s");
            }
            sb.append(".cybozu.com");
        }
        if (this.guestSpaceId >= 0) {
        	sb.append(String.format(GUEST_API_PREFIX, this.guestSpaceId));
        } else {
        	sb.append(API_PREFIX);
        }
        if (api != null) {
            sb.append(api);
        }

        return new URL(new String(sb));
    }

    /**
     * Clears the settings of this connection.
     */
    public void close() {
        auth = null;
        proxy = null;
        apiToken = null;
        headers.clear();
        this.trustAllHosts = false;
        this.useClientCert = false;
        Authenticator.setDefault(null);
    }

    /**
     * Returns if this connection trusts all hosts (for debug).
     * 
     * @return true if this connection trusts all hosts
     */
    public boolean isTrustAllHosts() {
        return trustAllHosts;
    }

    /**
     * Sets to trust all hosts without verifying (for debug).
     * 
     * @param trustAllHosts
     */
    public void setTrustAllHosts(boolean trustAllHosts) {
        this.trustAllHosts = trustAllHosts;
    }

    /**
     * Sets an basic authentication password.
     * 
     * @param username
     * @param password
     */
    public void setBasicAuth(final String username, final String password) {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password
                        .toCharArray());
            }
        });
    }

    
    /**
	 * @return the guestSpaceId
	 */
	public long getGuestSpaceId() {
		return guestSpaceId;
	}

	/**
	 * @param guestSpaceId the guestSpaceId to set
	 */
	public void setGuestSpaceId(long guestSpaceId) {
		this.guestSpaceId = guestSpaceId;
	}

	/**
     * Adds a user customized header.
     * 
     * @param name
     *            header name
     * @param value
     *            header value
     */
    public void addHeader(String name, String value) {
        if (name.equalsIgnoreCase(USER_AGENT_KEY)) {
            userAgent += " " + value;
        } else {
            headers.put(name, value);
        }
    }

    /**
     * Sets the proxy host.
     * 
     * @param host
     *            proxy host
     * @param port
     *            proxy port
     */
    public void setProxy(String host, int port) {
        this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host,
                port));
    }

    /**
     * Sets the client certificate authentication.
     * 
     * @param cert
     *            cert file path
     * @param password
     *            cert password
     */
    public void setClientCert(String cert, String password) {
        System.setProperty(SSL_KEY_STORE, cert);
        System.setProperty(SSL_KEY_STORE_PASSWORD, password);

        this.useClientCert = true;
    }

    /**
     * Binds the client certification to the connection.
     * @param conn
     *            connection object
     * @throws DBException
     */
    private void certificate(HttpsURLConnection conn) throws DBException {
        try {
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);

            KeyStore key_store = KeyStore.getInstance("PKCS12");
            char[] key_pass = System.getProperty(SSL_KEY_STORE_PASSWORD)
                    .toCharArray();
            key_store.load(
                    new FileInputStream(System.getProperty(SSL_KEY_STORE)),
                    key_pass);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(key_store, key_pass);

            SSLContext sslcontext = SSLContext.getInstance("SSL");

            sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
                    new SecureRandom());
            conn.setSSLSocketFactory(sslcontext.getSocketFactory());
        } catch (KeyManagementException e) {
            throw new DBException(e);
        } catch (KeyStoreException e) {
            throw new DBException(e);
        } catch (CertificateException e) {
            throw new DBException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new DBException(e);
        } catch (IOException e) {
            throw new DBException(e);
        } catch (UnrecoverableKeyException e) {
            throw new DBException(e);
        }
    }

    /**
     * Makes the connection trust all hosts.
     * 
     * @param conn
     *            connection object
     */
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

        TrustManager[] trustAllCerts = new TrustManager[] { easyTrustManager };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            conn.setSSLSocketFactory(sc.getSocketFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * HostnameVerifier verify everything.
     *
     */
    private class VerifyEverythingHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String arg0, SSLSession arg1) {
            return false;
        }
    }

    /**
     * Sends a request to kintone.
     * 
     * @param method
     *            GET, POST, PUT or DELETE
     * @param api
     *            api file path and query string
     * @param body
     *            request data body
     * @return response string
     * @throws DBException
     */
    public String request(String method, String api, String body)
            throws DBException {
        return request(method, api, body, null);
    }

    /**
     * Sets user defined HTTP headers.
     * @param conn connection object
     */
    private void setHTTPHeaders(HttpURLConnection conn) {
        if (this.apiToken != null) {
            conn.setRequestProperty(API_TOKEN, this.apiToken);
        } else {
            conn.setRequestProperty(AUTH_HEADER, this.auth);
        }
        conn.setRequestProperty(USER_AGENT_KEY, this.userAgent);
        for (String header : this.headers.keySet()) {
            conn.setRequestProperty(header, this.headers.get(header));
        }
    }

    /**
     * Sends a request to kintone.
     * 
     * @param method
     *            GET, POST, PUT or DELETE
     * @param api
     *            api file path and query string
     * @param body
     *            request data body
     * @param outFile
     *            download file to the stream
     * @return response string
     * @throws DBException
     */
    public String request(String method, String api, String body, File outFile)
            throws DBException {
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
                conn = (HttpsURLConnection) url.openConnection();
            } else {
                conn = (HttpsURLConnection) url.openConnection(proxy);
            }
            if (this.trustAllHosts) {
                trustAllHosts(conn);
                conn.setHostnameVerifier(new VerifyEverythingHostnameVerifier());
            }

            if (this.useClientCert) {
                certificate(conn);
            }

            setHTTPHeaders(conn);

            conn.setRequestMethod(method);
        } catch (IOException e) {
            throw new DBException("can not open connection");
        }
        boolean post = false;
        if (method.equals("PUT") || method.equals("POST") || method.equals("DELETE")) {
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
                throw new DBException("an error occurred while sending data");
            }
            try {
	            OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
	            writer.write(body);
	            writer.close();
            } catch(IOException e) {
            	throw new DBException("socket error");
            }
            
        }

        // receive response
        try {
            checkStatus(conn);
            InputStream is = conn.getInputStream();
            try {
                if (outFile != null) {
                    OutputStream os = new FileOutputStream(outFile);
                    try {
                        byte[] buffer = new byte[8192];
                        int n = 0;
                        while (-1 != (n = is.read(buffer))) {
                            os.write(buffer, 0, n);
                        }
                    } finally {
                        os.close();
                    }
                } else {
                    response = streamToString(is);
                }
            } finally {
                is.close();
            }
        } catch (IOException e) {
            throw new DBException("an error occurred while receiving data");
        }

        // System.out.println(response);
        return response;

    }

    /**
     * Checks the status code of the response.
     * @param conn
     *             a connection object
     */
    private void checkStatus(HttpURLConnection conn) throws IOException, DBException {
        int statusCode = conn.getResponseCode();
        if (statusCode == 404) {
            ErrorResponse response = getErrorResponse(conn);
            if (response == null) {
                throw new DBNotFoundException("not found");
            } else {
                throw new DBNotFoundException(response);
            }
        }
        if (statusCode != 200) {
            ErrorResponse response = getErrorResponse(conn);
            if (response == null) {
                throw new DBException("http status error(" + statusCode + ")");
            } else {
                throw new DBException(response);
            }
        }
    }
    
    /**
     * Creates an error response object.
     * @param conn
     * @return ErrorResponse object. return null if any error occurred
     */
    private ErrorResponse getErrorResponse(HttpURLConnection conn) {
        
        InputStream err = conn.getErrorStream();
        
        String response;
        try {
            response = streamToString(err);
        } catch (IOException e) {
            return null;
        }
        JsonParser parser = new JsonParser();
        return parser.jsonToErrorResponse(response);
    }
    
    /**
     * Uploads a file.
     * 
     * @param contentType
     *            content type
     * @param file
     *            uploaded file
     * @return file key of the uploaded file
     * @throws DBException
     */
    private String upload(File file, String contentType) throws DBException {
        InputStream isFile;

        try {
            isFile = new FileInputStream(file.getAbsolutePath());
        } catch (FileNotFoundException e1) {
            throw new DBNotFoundException("cannot open file");
        }
        
        try {
            return upload(isFile, file.getName(), contentType);
        } finally {
            try {
                isFile.close();
            } catch (IOException e) {
            }
        }
        
    }
    
    /**
     * Uploads a file from input stream.
     * 
     * @param input
     *            the file stream to be uploaded
     * @param fileName
     *            file name
     * @param contentType
     *            content type
     * @return file key of the uploaded file
     * @throws DBException
     */
    private String upload(InputStream input, String fileName, String contentType) throws DBException {
        HttpsURLConnection conn;
        String response = null;

        URL url;
        try {
            url = this.getURL("file.json");

            if (this.proxy == null) {
                conn = (HttpsURLConnection) url.openConnection();
            } else {
                conn = (HttpsURLConnection) url.openConnection(proxy);
            }
            if (this.trustAllHosts) {
                trustAllHosts(conn);
                conn.setHostnameVerifier(new VerifyEverythingHostnameVerifier());
            }

            if (this.useClientCert) {
                certificate(conn);
            }

            setHTTPHeaders(conn);

            conn.setRequestMethod("POST");
        } catch (Exception e) {
            throw new DBException("invalid url");
        }

        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + BOUNDARY);

        try {
            conn.connect();
        } catch (IOException e) {
            throw new DBException("cannot connect to host");
        }

        OutputStream os;

        try {
                os = conn.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
                writer.write("--" + BOUNDARY + "\r\n");
                writer.write("Content-Disposition: form-data; name=\"file\"; filename=\""
                        + fileName + "\"\r\n");
                writer.write("Content-Type: " + contentType + "\r\n\r\n");
                writer.flush();
                byte[] buffer = new byte[8192];
                int n = 0;
                while (-1 != (n = input.read(buffer))) {
                    os.write(buffer, 0, n);
                }
                os.flush();
                writer.write("\r\n--" + BOUNDARY + "--\r\n");
                os.flush();
                writer.close();
        } catch (IOException e) {
            throw new DBException("an error occurred while sending data");
        }
    
        // receive response
        try {
            checkStatus(conn);
            InputStream is = conn.getInputStream();
            try {
                response = streamToString(is);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            throw new DBException("an error occurred while receiving data");
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

    /**
     * An utility method converts a stream object to string.
     * @param is input stream
     * @return string
     * @throws IOException
     */
    private String streamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is,
                "UTF-8"));
        try {
            char[] b = new char[1024];
            int line;
            while (0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
            }
        } finally {
            reader.close();
        }
        return new String(sb);
    }

    /**
     * Selects the records from kintone using a query string.
     * 
     * @param app
     *            application id
     * @param query
     *            query string
     * @return ResultSet object
     * @throws DBException
     */
    public ResultSet select(long app, String query)
            throws DBException {
        return select(app, query, null);
    }

    /**
     * Selects the records from kintone using a query string.
     * 
     * @param app
     *            application id
     * @param query
     *            query string
     * @param columns
     *            column names if needed
     * @return ResultSet object
     * @throws DBException
     */
    public ResultSet select(long app, String query, String[] columns)
            throws DBException {

        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        StringBuilder sb = new StringBuilder();
        sb.append("app=");
        sb.append(app);
        sb.append("&query=");
        sb.append(query);
        if (columns != null) {
            int i = 0;
            for (String column : columns) {
                sb.append("&fields[" + i + "]=");
                try {
                	sb.append(URLEncoder.encode(column, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                }
                i++;
            }
        }
        String api = new String(sb);
        String response = request("GET", "records.json?" + api, null);
        JsonParser parser = new JsonParser();
        ResultSet rs = null;
        
        try {
            rs = parser.jsonToResultSet(this, response);
        } catch (IOException e) {
            throw new ParseException("failed to parse json to resultset");
        }

        return rs;
    }

    /**
     * Inserts a new record.
     * 
     * @param app
     *            application id
     * @param record
     *            The Record object to be inserted
     * @return The id number of inserted record
     * @throws DBException
     */
    public long insert(long app, Record record) throws DBException {
        List<Record> list = new ArrayList<Record>();
        list.add(record);
        List<Long> ids = insert(app, list);
        
        if (ids.size() > 0) {
            return ids.get(0);
        }
        throw new DBException("Failed to insert new record.");
    }

    /**
     * Uploads the file binded with the field and sets the file key.
     * @param field
     * @throws DBException
     */
    public void lazyUpload(Field field) throws DBException {
        if (!field.isLazyUpload()) return;
        
        LazyUploader uploader = field.getLazyUploader();
        String key = uploader.upload(this);
        List<FileDto> list = new ArrayList<FileDto>();
        FileDto file = new FileDto();
        file.setFileKey(key);
        list.add(file);
        field.setValue(list);
    }
    
    /**
     * Inserts new records.
     * 
     * @param app
     *            application id
     * @param records
     *            The array of Record objects to be inserted
     * @return The list of inserted id number
     * @throws DBException
     */
    public List<Long> insert(long app, List<Record> records) throws DBException {

        for (Record record: records) {
            Set<Map.Entry<String,Field>> set = record.getEntrySet();
            for (Map.Entry<String,Field> entry: set) {
                Field field = entry.getValue();
                lazyUpload(field); // force lazy upload
            }
        }
        
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
     * Updates a record(deprecated).
     * 
     * @param app
     *            application id
     * @param id
     *            record number of the updated record
     * @param record
     *            updated record object
     * @throws DBException
     */
    public void update(long app, long id, Record record) throws DBException {
        List<Long> list = new ArrayList<Long>();
        list.add(id);
        update(app, list, record);
    }

    /**
     * Updates a record.
     * 
     * @param app
     *            application id
     * @param record
     *            updated record object
     * @throws DBException
     */
    public void updateByRecord(long app, Record record) throws DBException {
        List<Record> list = new ArrayList<Record>();
        list.add(record);
        updateByRecords(app, list);
    }

    /**
     * Updates records.
     * 
     * @param app
     *            application id
     * @param ids
     *            an array of record numbers of the updated records
     * @param record
     *            updated record object
     * @throws DBException
     */
    public void update(long app, List<Long> ids, Record record)
            throws DBException {
        
        Set<Map.Entry<String,Field>> set = record.getEntrySet();
        for (Map.Entry<String,Field> entry: set) {
            Field field = entry.getValue();
            lazyUpload(field); // force lazy upload
        }
    
        JsonParser parser = new JsonParser();
        String json;
        try {
            json = parser.recordsToJsonForUpdate(app, ids, record);
        } catch (IOException e) {
            throw new ParseException("failed to encode to json");
        }

        request("PUT", "records.json", json);
    }

    /**
     * Updates records.
     * 
     * @param app
     *            application id
     * @param records
     *            an array of the updated record object
     * @throws DBException
     */
    public void updateByRecords(long app, List<Record> records) throws DBException {
        // upload files
        for (Record record: records) {
            Set<Map.Entry<String,Field>> set = record.getEntrySet();
            for (Map.Entry<String,Field> entry: set) {
                Field field = entry.getValue();
                lazyUpload(field); // force lazy upload
            }
        }
    
        JsonParser parser = new JsonParser();
        String json;
        try {
            json = parser.recordsToJsonForUpdate(app, records);
        } catch (IOException e) {
            throw new ParseException("failed to encode to json");
        }

        request("PUT", "records.json", json);
    }
    
    /**
     * Updates records.
     * 
     * @param app
     *            application id
     * @param query
     *            query string to determine the updated records
     * @param record
     *            updated record object
     * @throws DBException
     */
    public void updateByQuery(long app, String query, Record record)
            throws DBException {
        String[] fields = {};
        ResultSet rs = select(app, query, fields);
        List<Long> ids = new ArrayList<Long>();

        if (rs.size() == 0)
            return;

        while (rs.next()) {
            ids.add(rs.getId());
        }

        update(app, ids, record);

    }

    /**
     * Deletes a record.
     * 
     * @param app
     *            application id
     * @param id
     *            record number to be deleted
     * @throws DBException
     */
    public void delete(long app, long id) throws DBException {
        List<Long> list = new ArrayList<Long>();
        list.add(id);
        delete(app, list);
    }

    /**
     * Deletes a record.
     * 
     * @param app
     *            application id
     * @param record
     *            a record object to be deleted
     * @throws DBException
     */
    public void deleteByRecord(long app, Record record) throws DBException {
        List<Record> list = new ArrayList<Record>();
        list.add(record);
        deleteByRecords(app, list);
    }
    
    /**
     * Deletes records.
     * 
     * @param app
     *            application id
     * @param records
     *            a list of the record object to be deleted
     * @throws DBException
     */
    public void deleteByRecords(long app, List<Record> records) throws DBException {
        
        JsonParser parser = new JsonParser();
        String json;
        try {
            json = parser.recordsToJsonForDelete(app, records);
        } catch (IOException e) {
            throw new ParseException("failed to encode to json");
        }
        
        request("DELETE", "records.json", json);
    }

    /**
     * Deletes records.
     * @param app
     *           application id
     * @param ids
     *           a list of record numbers to be deleted
     * @throws DBException
     */
    public void delete(long app, List<Long> ids) throws DBException {
        List<Record> records = new ArrayList<Record>();
        for (Long id : ids) {
            Record record = new Record();
            record.setId(id);
            records.add(record);
        }
        deleteByRecords(app, records);
    }

    /**
     * Deletes records.
     * 
     * @param app
     *            application id
     * @param query
     *            query string to determine the deleted records
     * @throws DBException
     */
    public void deleteByQuery(long app, String query) throws DBException {
        ResultSet rs = select(app, query);
        List<Record> records = new ArrayList<Record>();

        if (rs.size() == 0)
            return;

        while (rs.next()) {
            Record record = new Record();
            record.setId(rs.getId());
            records.add(record);
        }
        deleteByRecords(app, records);
    }

    /**
     * Uploads a file with the content type.
     * 
     * @param file
     *            file object to be uploaded
     * @param contentType
     *            content type
     * @return file key
     * @throws DBException
     */
    public String uploadFile(File file, String contentType) throws DBException {

        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        return upload(file, contentType);
    }

    /**
     * Uploads a file.
     * 
     * @param file
     *            file object to be uploaded
     * @return file key
     * @throws DBException
     */
    public String uploadFile(File file) throws DBException {
        return uploadFile(file, null);
    }

    /**
     * Uploads a file from input stream.
     * 
     * @param contentType
     *            content type
     * @param file
     *            file object to be uploaded
     * @param fileName
     *            upload file name
     * @return file key
     * @throws DBException
     */
    public String uploadFile(String contentType, InputStream file, String fileName) throws DBException {
        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        return upload(file, fileName, contentType);
    }

    /**
     * Downloads a file.
     * 
     * @param fileKey
     *            file key
     * @return file object
     * @throws IOException
     * @throws DBException
     */
    public File downloadFile(String fileKey) throws IOException, DBException {
        File tempFile = File.createTempFile(fileKey, null);

        request("GET", "file.json?fileKey=" + fileKey, null, tempFile);
        return tempFile;
    }
    
    /**
     * Build update.
     * 
     * @param bulk
     *            an instance of bulk request
     * @throws DBException
     */
    public void bulkRequest(BulkRequest bulk) throws DBException {
        
        String json = bulk.getJson();
        
        request("POST", "bulkRequest.json", json);
    }
    
    /**
     * Return the app information object
     * 
     * @param id 
     * 	            app id
     * @return app object
     */
    public AppDto getApp(long id) throws DBException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("id=");
        sb.append(id);
        
        String query = new String(sb);
        String response = request("GET", "app.json?" + query, null);
        JsonParser parser = new JsonParser();
        AppDto app = null;
        
        try {
            app = parser.jsonToApp(response);
        } catch (IOException e) {
            throw new ParseException("failed to parse json to app");
        }

        return app;
    }
    
    /**
     * Search apps with name
     * 
     * @param name
     * @return the list of apps
     */
    public List<AppDto> getApps(String name) throws DBException {
    	return getApps(null, null, name, null, 100, 0);
    }
    
    /**
     * Search apps with id, code or name
     * 
     * @param ids
     * @param codes
     * @param name
     * @param spaceIds
     * @param limit
     * @param offset
     * @return the list of apps
     */
    public List<AppDto> getApps(List<Long> ids, List<String> codes, 
    		String name, List<Long> spaceIds, long limit, long offset) throws DBException {
    	
    	StringBuilder sb = new StringBuilder();
        
        if (ids != null) {
        	for (int i = 0; i < ids.size(); i++) {
        		long id = ids.get(i);
            	if (sb.length() > 0) {
            		sb.append("&");
            	}
            	sb.append("ids[" + i + "]=");
            	sb.append(id);
        	}
        }
        
        if (codes != null) {
	        for (int i = 0; i < codes.size(); i++) {
	        	String code = codes.get(i);
	        	if (sb.length() > 0) {
	        		sb.append("&");
	        	}
	        	try {
	        		sb.append("codes[" + i + "]=");
	        		sb.append(URLEncoder.encode(code, "UTF-8"));
	        	} catch (UnsupportedEncodingException e) {
	        		throw new DBException("Malformed code");
	        	}
	        }
        }
        
        if (name != null) {
        	if (sb.length() > 0) {
        		sb.append("&");
        	}
        	try {
        		sb.append("name=");
        		sb.append(URLEncoder.encode(name, "UTF-8"));
        	} catch (UnsupportedEncodingException e) {
        		throw new DBException("Malformed name");
        	}
        }
        
        if (spaceIds != null) {
	        for (int i = 0; i < spaceIds.size(); i++) {
	        	long id = spaceIds.get(i);
	        	if (sb.length() > 0) {
	        		sb.append("&");
	        	}
	        	sb.append("spaceIds[" + i + "]=");
	        	sb.append(id);
	        }
        }
        
        if (sb.length() > 0) {
    		sb.append("&");
    	}
    	sb.append("limit=");
    	sb.append(limit);
    	sb.append("&offset=");
    	sb.append(offset);
        
        String query = new String(sb);
        String response = request("GET", "apps.json?" + query, null);
        JsonParser parser = new JsonParser();
        List<AppDto> apps = null;
        
        try {
            apps = parser.jsonToApps(response);
        } catch (IOException e) {
            throw new ParseException("failed to parse json to apps");
        }
        
    	return apps;
    }
}
