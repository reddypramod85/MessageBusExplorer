package com.hpe.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


public class PropertiesUtility {
	//private static final Logger LOGGER = Logger.getLogger(PropertiesUtility.class);
	public String getProperties(String Prop){
		String properties = null;
		try{
			Properties props = new Properties();
			//FileInputStream inps = new FileInputStream("/MsgBus.properties");
			props.load(this.getClass().getResourceAsStream("/MsgBus.properties"));
			properties=props.getProperty(Prop);
			this.getClass().getResourceAsStream("/MsgBus.properties").close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return properties;
	}
	public void setProperties(String Prop, String val) {
		try{
			final File propsFile = new File("/MsgBus.properties");
			//final File propsFile = new File("C:/Users/sareddyp/workspace/HPEOVMsgBus/src/MsgBus.properties");
			/*String filePath = new File("C:/Users/sareddyp/workspace/HPEOVMsgBus/src/MsgBus.properties").getAbsolutePath();
			String msgPath = new File("/MsgBus.properties").getAbsolutePath();
			String basePath = new File("").getAbsolutePath();
			System.out.println("filePath"+filePath);
			System.out.println("msgPath"+msgPath);
			System.out.println("propsFile"+propsFile);*/
			Properties props = new Properties();
			props.load(new FileInputStream(propsFile));
			// make changes
			props.setProperty(Prop, val);
			props.save(new FileOutputStream(propsFile), "");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
