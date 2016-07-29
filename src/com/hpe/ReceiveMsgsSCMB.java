package com.hpe;
/*
 * Created by: pramod-reddy.sareddy@hpe.com
 * Class Name: ReceiveMsgsSCMB
 * Description: Connection will be made to the One View applicance with the help of Java Key store and Trust store validation which will contain the certificates.
 * Channel and queue will be created based on the exchange name and binding key to retrieve the messages from SCMB and MSMB(State Change Message Bus, Metrics Streaming Message Bus)
 * Date Created: 12-FEB-2016
 */

import com.hp.ov.sdk.dto.AvailableTargets;
import com.hp.ov.sdk.dto.Target;
import com.hp.ov.sdk.dto.TaskResourceV2;
import com.hp.ov.sdk.dto.generated.Enclosures;
import com.hp.ov.sdk.dto.generated.ServerProfile;
import com.hp.ov.sdk.exceptions.SDKApplianceNotReachableException;
import com.hp.ov.sdk.exceptions.SDKBadRequestException;
import com.hp.ov.sdk.exceptions.SDKInvalidArgumentException;
import com.hp.ov.sdk.exceptions.SDKNoResponseException;
import com.hp.ov.sdk.exceptions.SDKNoSuchUrlException;
import com.hp.ov.sdk.exceptions.SDKResourceNotFoundException;
import com.hp.ov.sdk.exceptions.SDKTasksException;
import com.hp.ov.sdk.rest.client.EnclosureClient;
import com.hp.ov.sdk.rest.client.EnclosureClientImpl;
import com.hp.ov.sdk.rest.client.ServerProfileClient;
import com.hp.ov.sdk.rest.client.ServerProfileClientImpl;
import com.hp.ov.sdk.rest.http.core.client.RestParams;
import com.hp.ov.sdk.util.UrlUtils;
import com.hpe.utility.PropertiesUtility;
import com.hpe.valueobjects.stateVO;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.DefaultSaslConfig;
import com.rabbitmq.client.Envelope;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
//import org.json.JSONObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ReceiveMsgsSCMB {
	private static ReceiveMsgsSCMB scmbInstance = null;
	private List<String> voList;
	//To hold the channel and queue values.
	stateVO sVO;
	String json = "";
	String tag = "";
	//private RestParams params;
	private ServerProfileClient serverProfileClient;
	private EnclosureClient enclosureClient;
	private TaskResourceV2 taskResourceV2;
	 

	protected ReceiveMsgsSCMB() {

	}

	public static ReceiveMsgsSCMB getInstance() {

		if (scmbInstance == null) {
			scmbInstance = new ReceiveMsgsSCMB();
		}
		return scmbInstance;
	}

	public String getMyMethod(String hostName,String exchangeName,String bindingKey, String flag,HttpServletRequest request)throws AlreadyClosedException,SocketException,IOException{
		try{
			final RestParams params = new RestParams();
			System.out.println("In ReceiveMsgsSCMB myMethod hostName :"+hostName+"exchangeName :"+exchangeName+"bindingKey :"+bindingKey+"flag value :"+flag);
			//Retrieving Java key store and Trust store names from property file
			PropertiesUtility propertiesUtility = new PropertiesUtility();
			ConnectToOneView connOV = new ConnectToOneView();
			String workingDirectory = System.getProperty("user.dir");
			String myKeyStore=workingDirectory + File.separator +propertiesUtility.getProperties("KeystoreName");
			String myTrustStore=workingDirectory + File.separator +propertiesUtility.getProperties("TruststoreName");
			DeclareOk queueName =null;
			ConnectionFactory factory=null;
			Connection connection=null;
			Channel channel=null;
			//Retrieving Java key store password and validating  from property file
			final char[] keyPassphrase = propertiesUtility.getProperties("KeystorePswd").toCharArray();
			final KeyStore ks = KeyStore.getInstance("jks");
			ks.load(new FileInputStream(myKeyStore), keyPassphrase);
			final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, keyPassphrase);

			//Retrieving Java Trust store password and validating from property file
			final char[] trustPassphrase = propertiesUtility.getProperties("TruststorePswd").toCharArray();
			final KeyStore tks = KeyStore.getInstance("jks");
			tks.load(new FileInputStream(myTrustStore), trustPassphrase);
			final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(tks);

			//load SSLContext with keystore and truststore.
			final SSLContext c = SSLContext.getInstance("SSL");
			c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
			String sessID = request.getSession().getAttribute("sessionID").toString();
			//flag value will be 1 for the first poll(ajax call from JSP) to create the channel and from second time it will be 2.
			if(flag!=null && Integer.parseInt(flag)<=1){
				if(sVO!=null){
					sVO.getChannel().basicCancel(sVO.getConsumerTag());
				}
				sVO = new stateVO();
				voList = new ArrayList<String>();
				json="";
				factory = new ConnectionFactory();
				factory.setHost(hostName);
				factory.setSaslConfig(DefaultSaslConfig.EXTERNAL);
				factory.setPort(5671);
				factory.useSslProtocol(c);
				sVO.setFactory(factory);
				connection = factory.newConnection();
				sVO.setConnection(connection);
				channel = connection.createChannel();
				sVO.setChannel(channel);
				TrustManagerFactory factory1 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				keyStore.load(this.getClass().getResourceAsStream("/TrustStore"), "password".toCharArray());
				factory1.init(keyStore);
				/*params.setUserName("administrator");
				params.setPassword("password");*/
				params.setApiVersion("200");
				params.setHostname(hostName);
				params.setTrustManager(factory1.getTrustManagers()[0]);
				params.setSessionId(sessID);

				//queueName = channel.queueDeclare("", true, false, true, null);
				queueName = channel.queueDeclare();
				sVO.setQueueName(queueName.getQueue());

				if(bindingKey!=null && bindingKey.equalsIgnoreCase("msmb.#")){
					HttpsURLConnection conn = connOV.callAPI("GET", "/rest/metrics/configuration", sessID, hostName);
					BufferedReader br = 
							new BufferedReader(
									new InputStreamReader(conn.getInputStream()));
					String output = br.readLine();
					JSONParser jsonParser = new JSONParser();
					if(output!=null && output!=""){
						JSONObject jsonObject = (JSONObject) jsonParser.parse(output);	
						// get a String sessionID from the JSON object
						ArrayList list = (ArrayList) jsonObject.get("sourceTypeList");
						if(list!=null && list.size()<=0){
							System.out.println("setting default metrics");
							conn = connOV.callAPI("PUT", "/rest/metrics/configuration", sessID, hostName);
							BufferedReader brd = 
									new BufferedReader(
											new InputStreamReader(conn.getInputStream()));
						}
					}
				}

				channel.queueBind(queueName.getQueue(),exchangeName, bindingKey);
				System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
			}
			//Creating the consumer to consume the messages
			if(sVO!=null){
				Consumer consumer = new DefaultConsumer(sVO.getChannel()) {
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope,
							AMQP.BasicProperties properties, byte[] body) throws IOException {
						String message = new String(body, "UTF-8");
						try {
						if(bindingKey!=null && (bindingKey.equalsIgnoreCase("scmb.server-hardware.#")||bindingKey.equalsIgnoreCase("scmb.#"))){
				            JSONParser jsonParser = new JSONParser();
				            JSONObject jsonObject = (JSONObject) jsonParser.parse(message);
				            // get a String from the JSON object
				            String powerStatus = (String) jsonObject.get("newSubState");
				            System.out.println("++powerStatus++"+powerStatus);
				            jsonObject = (JSONObject) jsonObject.get("resource");
				            //System.out.println("++resource++"+resource);
				            //jsonObject = (JSONObject) jsonParser.parse(resource);
				            String serverProfileUri = (String) jsonObject.get("serverProfileUri");
				            System.out.println("++serverProfileUri++"+serverProfileUri);
				            if(powerStatus!=null && powerStatus.equalsIgnoreCase("Off") && serverProfileUri!=null){
				            	ServerProfile serverProfileDto = null;
				            	serverProfileClient = ServerProfileClientImpl.getClient();
				            	String resourceId = UrlUtils.getResourceIdFromUri(serverProfileUri);
				            	try {
				                    // fetch resource Id using resource name
				                   serverProfileDto = serverProfileClient.getServerProfile(params, resourceId);

				                    serverProfileDto.setServerHardwareUri(null);
				                    serverProfileDto.setEnclosureUri(null);
				                    serverProfileDto.setEnclosureBay(null);
				                    
				                     /* then make sdk service call to get resource aSync parameter
				                     * indicates sync vs async useJsonRequest parameter indicates
				                     * whether json input request present or not*/
				                     
				                    taskResourceV2 = serverProfileClient.updateServerProfile(params, resourceId, serverProfileDto, false, false);

				                    System.out.println("ServerProfileClientTest : updateServerProfile : " + "serverProfile object returned to client : "
				                            + taskResourceV2.toString());
				                    
				                   // https://16.31.86.9/rest/server-profiles/available-targets?profileUri=/rest/server-profiles/c6e549b5-8662-4ee7-9530-5931a0500044
				                   //"serverProfileUri":"/rest/server-profiles/c6e549b5-8662-4ee7-9530-5931a0500044"
				                    AvailableTargets availableTargetsdto = serverProfileClient.getAvailableTargetsForServerProfile(params , null, null, serverProfileUri);
									List targetList = availableTargetsdto.getTargets();
									System.out.println("+++++targetList++++"+targetList);
									if(targetList!=null && targetList.size()>1){
										System.out.println("setting default metrics"+targetList.get(0));
										Target target = (Target) targetList.get(1);
										System.out.println("target"+target);
										if(target!=null){
											System.out.println("serverHardwareUri"+target.getServerHardwareUri());
											System.out.println("enclosureGroupUri"+target.getEnclosureGroupUri());
												serverProfileDto = serverProfileClient.getServerProfile(params, resourceId);
												serverProfileDto.setServerHardwareUri(target.getServerHardwareUri());
							                    serverProfileDto.setEnclosureUri(target.getEnclosureUri());
							                    serverProfileDto.setEnclosureGroupUri(target.getEnclosureGroupUri());
							                    serverProfileDto.setEnclosureBay(target.getEnclosureBay());
							                    taskResourceV2 = serverProfileClient.updateServerProfile(params, resourceId, serverProfileDto, false, false);

							                    System.out.println("ServerProfileClientTest : updateServerProfile : " + "serverProfile object returned to client : "
							                            + taskResourceV2.toString());
											}
										}
				                    
				                } catch (final SDKResourceNotFoundException ex) {
				                    System.out.println("ServerProfileClientTest : updateServerProfile :"
				                            + " resource you are looking is not found for update ");
				                    return;
				                } catch (final SDKBadRequestException ex) {
				                    System.out.println("ServerProfileClientTest : updateServerProfile :" + " bad request, try again : "
				                            + "may be duplicate resource name or invalid inputs. check inputs and try again");
				                    return;
				                } catch (final SDKNoSuchUrlException ex) {
				                    System.out.println("ServerProfileClientTest : updateServerProfile :" + " no such url : " + params.getUrl());
				                    return;
				                } catch (final SDKApplianceNotReachableException e) {
				                    System.out.println("ServerProfileClientTest : updateServerProfile :" + " Applicance Not reachabe at : "
				                            + params.getHostname());
				                    return;
				                } catch (final SDKNoResponseException ex) {
				                    System.out.println("ServerProfileClientTest : updateServerProfile :" + " No response from appliance : "
				                            + params.getHostname());
				                    return;
				                } catch (final SDKInvalidArgumentException ex) {
				                    System.out.println("ServerProfileClientTest : updateServerProfile : " + "arguments are null ");
				                    return;
				                } catch (final SDKTasksException e) {
				                    System.out.println("ServerProfileClientTest : updateServerProfile : " + "errors in task, please check task "
				                            + "resource for more details ");
				                    return;
				                }
				            }
							
						}
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						voList.add(message);
						ObjectMapper mapper = new ObjectMapper();
						json = mapper.writeValueAsString(voList);
						System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
					}
				};
				System.out.println("++++++++++Server Profile Un assign++++++");
				if(flag!=null && Integer.parseInt(flag)<=1){
					tag = sVO.getChannel().basicConsume(sVO.getQueueName(), true, consumer);
					sVO.setConsumerTag(tag);
				}
			}
		}
		catch(Exception e){
			System.out.println("+++++In Catch getMyMethod of ReceiveMsgsSCMB+++++");
			e.printStackTrace();
			throw e;
		}finally{
			return json;
		}
	}

}
