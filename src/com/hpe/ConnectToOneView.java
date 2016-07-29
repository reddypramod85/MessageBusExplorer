package com.hpe;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.io.*;
import java.math.BigInteger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.hpe.exception.CustomException;
import com.hpe.utility.PropertiesUtility;
import com.rabbitmq.client.AlreadyClosedException;

import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.*;
import java.security.cert.CertificateFactory;
import java.util.Collection;
/*
 * Created by: pramod-reddy.sareddy@hpe.com
 * Class Name: ConnectToOneView
 * Description: With valid credentials of OneView passed by the user, OneView APIs will be invoked to create the sessions, download the certificates, creating the Java Key store and Trust store.
 * Date Created: 15-MAR-2016
 */
public class ConnectToOneView {
	private static ConnectToOneView scmbInstance = null;
	PropertiesUtility propertiesUtility = new PropertiesUtility();
	public ConnectToOneView() {

	}

	public static ConnectToOneView getInstance() {

		if (scmbInstance == null) {
			scmbInstance = new ConnectToOneView();
		}
		return scmbInstance;
	}


	public void getConnOV(String hostName,String userName,String password,HttpServletRequest request) throws KeyManagementException, NoSuchAlgorithmException{
		try{
			HttpsURLConnection connection=null;
			// Create a context that doesn't check certificates.
			SSLContext ssl_ctx = SSLContext.getInstance("TLS");
			TrustManager[ ] trust_mgr = get_trust_mgr();
			ssl_ctx.init(null,                // key manager
					trust_mgr,           // trust manager
					new SecureRandom()); // random number generator
			HttpsURLConnection.setDefaultSSLSocketFactory(ssl_ctx.getSocketFactory());
			connection=callAPI("POST","/rest/login-sessions","",hostName);
			String sessionID= getLoginSession(connection,hostName, userName,password);
			request.getSession().setAttribute("sessionID", sessionID);
			connection=callAPI("GET","/rest/certificates/client/rabbitmq/keypair/default",sessionID,hostName);
			getCertificateKey(connection,sessionID,hostName);
			connection=callAPI("GET","/rest/certificates/ca",sessionID,hostName);
			getCARootCertificate(connection,hostName);
			createKeyStore();
			createTrustStore();
		} catch (CustomException e) {
			request.setAttribute("errormsg", e.getMessage());
		} catch (AlreadyClosedException e) {
			// TODO Auto-generated catch block
			request.setAttribute("errormsg", "Please Login again");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			request.setAttribute("errormsg", "Please Login again");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	private TrustManager[ ] get_trust_mgr() {
		TrustManager[ ] certs = new TrustManager[ ] {
				new X509TrustManager() {
					public X509Certificate[ ] getAcceptedIssuers() { return null; }
					public void checkClientTrusted(X509Certificate[ ] certs, String t) { }
					public void checkServerTrusted(X509Certificate[ ] certs, String t) { }
				}
		};
		return certs;
	}
	public HttpsURLConnection callAPI(String reqMethod, String api, String sessionId, String hostName) throws AlreadyClosedException,SocketException,IOException, ParseException{
		HttpsURLConnection con=null;
		try {
			String formURL = "https://"+hostName+api;
			URL url = new URL(formURL);
			System.out.println("URL"+formURL);
			con = (HttpsURLConnection)url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod(reqMethod);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setAllowUserInteraction(false);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("X-API-Version", "200");

			// Guard against "bad hostname" errors during handshake.
			con.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String host, SSLSession sess) {
					if (host.equals(hostName)) return true;
					else return false;
				}
			});
			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) { 
					return true; }
			};
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			//dump all the content
			if(sessionId!=null && sessionId!="")
				con.setRequestProperty("Auth", sessionId);
			if(api!=null && api!="" && reqMethod!=null && reqMethod.equalsIgnoreCase("PUT") && api.equalsIgnoreCase("/rest/metrics/configuration")){
				String payload="{\"sourceTypeList\":[{\"sourceType\": \"/rest/power-devices\",\"sampleIntervalInSeconds\": \"300\",\"frequencyOfRelayInSeconds\": \"300\"},{\"sourceType\": \"/rest/enclosures\",\"sampleIntervalInSeconds\": \"300\",\"frequencyOfRelayInSeconds\": \"300\"},{\"sourceType\": \"/rest/server-hardware\",\"sampleIntervalInSeconds\": \"300\",\"frequencyOfRelayInSeconds\": \"300\"}]}";
				OutputStream os = con.getOutputStream();
				os.write(payload.getBytes());
				os.flush();
				os.close();
			}
			if(api!=null && api!="" && reqMethod!=null && reqMethod.equalsIgnoreCase("POST") && api.equalsIgnoreCase("/rest/certificates/client/rabbitmq")){
				String payload="{\"type\":\"RabbitMqClientCertV2\",\"commonName\":\"default\"}";
				OutputStream os = con.getOutputStream();
				os.write(payload.getBytes());
				os.flush();
				os.close();
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}

		return con;
	}

	private String getLoginSession(HttpsURLConnection con,String hstName, String usrName, String pswd) throws CustomException{
		String sessionID="";
		if(con!=null){
			try {
				BufferedReader br=null;
				JSONObject json = new JSONObject();
				json.put("userName", usrName);
				json.put("password", pswd);
				String input=json.toString();
				OutputStream os = con.getOutputStream();
				os.write(input.getBytes());
				os.flush();
				System.out.println("****** Print Content of the Loggin Session URL ********");

				br = new BufferedReader(
						new InputStreamReader(con.getInputStream()));
				String output = br.readLine();
				JSONParser jsonParser = new JSONParser();
				if(output!=null){
					JSONObject jsonObject = (JSONObject) jsonParser.parse(output);	
					// get a String sessionID from the JSON object
					sessionID = (String) jsonObject.get("sessionID");
				}
				if(br!=null)
				br.close();
			} catch (ConnectException e) {
				System.out.println("In connecttooneView getLoginSession ConnectException catch block");
				throw new CustomException("Unable to connect to OneView appliance. Please contact your administrator.");
			}
			catch (IOException e) {
				throw new CustomException("Please enter valid credentials");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				throw new CustomException("Unable to parse the JSON response");
			}
		}
		return sessionID;
	}

	private void getCertificateKey(HttpsURLConnection con,String sessionID,String hstName) throws ParseException{
		if(con!=null){
			BufferedReader br=null;
			try {
				String certData="";
				String keyData="";
				System.out.println("****** Print Private key and Public Certificate Content of the URL  ********");
				try{
				br = 
						new BufferedReader(
								new InputStreamReader(con.getInputStream()));
				}catch(IOException e){
					con=callAPI("POST","/rest/certificates/client/rabbitmq",sessionID,hstName);
					br = 
							new BufferedReader(
									new InputStreamReader(con.getInputStream()));
				}
				String output = br.readLine();
				JSONParser jsonParser = new JSONParser();
				if(output!=null){
					JSONObject jsonObject = (JSONObject) jsonParser.parse(output);	
					//System.out.println("++++Cert data++++"+jsonObject.get("base64SSLCertData"));
					certData = (String) jsonObject.get("base64SSLCertData");
					keyData = (String) jsonObject.get("base64SSLKeyData");
					String certFilePath = "";
					String workingDirectory = System.getProperty("user.dir");
					//absoluteFilePath = workingDirectory + System.getProperty("file.separator") + filename;
					certFilePath = workingDirectory + File.separator + propertiesUtility.getProperties("CertName");
					File certFile = new File(certFilePath);
					if(certFile.delete()){
						System.out.println(certFile.getName() + " is deleted!");
					}else{
						System.out.println("Delete operation is failed.");
					}

					if (certFile.createNewFile()) {
						System.out.println("File is created!");
					} else {
						System.out.println("File is already existed!");
					}

					//true = append file

					FileWriter fileWritter = new FileWriter(certFile.getName(),true);
					BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
					bufferWritter.write(certData);
					bufferWritter.close();

					String keyFilePath = "";
					keyFilePath = workingDirectory + File.separator + propertiesUtility.getProperties("KeyfileName");
					File keyFile = new File(keyFilePath);
					if(keyFile.delete()){
						System.out.println(keyFile.getName() + " is deleted!");
					}else{
						System.out.println("Delete operation is failed.");
					}

					if (keyFile.createNewFile()) {
						System.out.println("File is created!");
					} else {
						System.out.println("File is already existed!");
					}

					//true = append file
					FileWriter fileWritterKey = new FileWriter(keyFile.getName(),true);
					BufferedWriter bufferWritterKey = new BufferedWriter(fileWritterKey);
					bufferWritterKey.write(keyData);
					bufferWritterKey.close();
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void getCARootCertificate(HttpsURLConnection con,String hstName) throws ParseException{
		try {
			if(con!=null){
				System.out.println("****** CA Root Certificate Content of the URL  ********");
				BufferedReader br = 
						new BufferedReader(
								new InputStreamReader(con.getInputStream()));
				String output = br.readLine();
				if(output!=null){
					String caCertFilePath = "";
					String caCertData=output.substring(1, output.length()-1);
					caCertData = caCertData.replace("\\n", "\\\n").replace("\\", "");
					String workingDirectory = System.getProperty("user.dir");
					caCertFilePath = workingDirectory + File.separator + propertiesUtility.getProperties("CACerName");

					File certFile = new File(caCertFilePath);
					if(certFile.delete()){
						System.out.println(certFile.getName() + " is deleted!");
					}else{
						System.out.println("Delete operation is failed.");
					}

					if (certFile.createNewFile()) {
						System.out.println("File is created!");
					} else {
						System.out.println("File is already existed!");
					}

					//true = append file
					FileWriter fileWritter = new FileWriter(certFile.getName(),true);
					BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
					bufferWritter.write(caCertData);
					bufferWritter.close();
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static InputStream fullStream ( String fname ) throws IOException {
		FileInputStream fis = new FileInputStream(fname);
		DataInputStream dis = new DataInputStream(fis);
		byte[] bytes = new byte[dis.available()];
		dis.readFully(bytes);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		dis.close();
		return bais;
	}

	private void createKeyStore() throws ParseException, IOException{
		BufferedReader br=null;
		try{
			String keypass = propertiesUtility.getProperties("KeystorePswd");  // this is a new password, you need to come up with to protect your java key store file
			String defaultalias = propertiesUtility.getProperties("Alias");
			String workingDirectory = System.getProperty("user.dir");

			//****************
			String keyStorePath = workingDirectory + File.separator + propertiesUtility.getProperties("KeystoreName");
			String keyFilePath = workingDirectory + File.separator + propertiesUtility.getProperties("KeyfileName");
			String certFilePath = workingDirectory + File.separator + propertiesUtility.getProperties("CertName");
			System.out.println("keyStorePath"+keyStorePath);
			System.out.println("keyFilePath"+keyFilePath);
			System.out.println("certFilePath"+certFilePath);
			PrivateKey key = null;
			FileInputStream fis = null;
			boolean isRSAKey = false;
			File f = new File(keyFilePath);
			fis = new FileInputStream(f);

			br = new BufferedReader(new InputStreamReader(fis));
			StringBuilder builder = new StringBuilder();
			boolean inKey = false;
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (!inKey) {
					if (line.startsWith("-----BEGIN ") && 
							line.endsWith(" PRIVATE KEY-----")) {
						inKey = true;
						isRSAKey = line.contains("RSA");
					}
					continue;
				}
				else {
					if (line.startsWith("-----END ") && 
							line.endsWith(" PRIVATE KEY-----")) {
						inKey = false;
						isRSAKey = line.contains("RSA");
						break;
					}
					builder.append(line);
				}
			}
			KeySpec keySpec = null;
			byte[] encoded = DatatypeConverter.parseBase64Binary(builder.toString());          
			if (isRSAKey)
			{
				keySpec = getRSAKeySpec(encoded);
			}
			else
			{
				keySpec = new PKCS8EncodedKeySpec(encoded);
			}
			KeyFactory kf = KeyFactory.getInstance("RSA");
			key = kf.generatePrivate(keySpec);

			//Creating KEY STORE
			//****************
			KeyStore ks = KeyStore.getInstance("JKS", "SUN");
			ks.load( null , keypass.toCharArray());
			ks.store(new FileOutputStream ( keyStorePath  ),
					keypass.toCharArray());
			ks.load(new FileInputStream ( keyStorePath ),
					keypass.toCharArray());

			// loading CertificateChain
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream certstream = fullStream (certFilePath);

			Collection c = cf.generateCertificates(certstream) ;
			Certificate[] certs = new Certificate[c.toArray().length];
			Certificate cert=null;

			if (c.size() == 1) {
				certstream = fullStream (certFilePath);
				System.out.println("One certificate, no chain.");
				cert = cf.generateCertificate(certstream) ;
				certs[0] = cert;
			} else {
				certs = (Certificate[])c.toArray();
			}

			// storing keystore
			ks.setKeyEntry(defaultalias, key,keypass.toCharArray(),certs );
			System.out.println("Key Store Created");
			System.out.println ("Key and certificate stored.");
			ks.store(new FileOutputStream ( keyStorePath ),
					keypass.toCharArray());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		}finally{
			br.close();
		}

	}
	private void createTrustStore() throws ParseException{
		try{
			String workingDirectory = System.getProperty("user.dir");
			String certFilePath = workingDirectory + File.separator + propertiesUtility.getProperties("CACerName");
			String trustStorePath = workingDirectory + File.separator + propertiesUtility.getProperties("TruststoreName");
			String trustpass =propertiesUtility.getProperties("TruststorePswd");
			// loading CertificateChain
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream certstream = fullStream (certFilePath);

			Collection c = cf.generateCertificates(certstream) ;
			Certificate[] certs = new Certificate[c.toArray().length];
			Certificate cert=null;

			if (c.size() == 1) {
				certstream = fullStream (certFilePath);
				System.out.println("One certificate, no chain.");
				cert = cf.generateCertificate(certstream) ;
				certs[0] = cert;
			} else {
				certs = (Certificate[])c.toArray();
			}

			//Creating TRUST STORE

			KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null);//Make an empty store
			trustStore.setCertificateEntry("CACert", cert);
			//ADD TO THE KEYSTORE AND GIVE IT AN ALIAS NAME
			trustStore.store(new FileOutputStream ( trustStorePath ),trustpass.toCharArray());
			System.out.println("Created Trust Store");
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private RSAPrivateCrtKeySpec getRSAKeySpec(byte[] keyBytes) throws IOException  {
		System.out.println("in getRSAKeySpec ");

		DerParser parser = new DerParser(keyBytes);

		Asn1Object sequence = parser.read();
		if (sequence.getType() != DerParser.SEQUENCE)
			throw new IOException("Invalid DER: not a sequence"); //$NON-NLS-1$

		// Parse inside the sequence
		parser = sequence.getParser();

		parser.read(); // Skip version
		BigInteger modulus = parser.read().getInteger();
		BigInteger publicExp = parser.read().getInteger();
		BigInteger privateExp = parser.read().getInteger();
		BigInteger prime1 = parser.read().getInteger();
		BigInteger prime2 = parser.read().getInteger();
		BigInteger exp1 = parser.read().getInteger();
		BigInteger exp2 = parser.read().getInteger();
		BigInteger crtCoef = parser.read().getInteger();

		RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(
				modulus, publicExp, privateExp, prime1, prime2,
				exp1, exp2, crtCoef);

		return keySpec;
	} 
}
