package com.soapui;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;




// Get Config.Properties values
/**
 * class loads the property files
 * 
 * @author IBM
 * 
 */
public class Config {

	private final static Properties configProp = new Properties();
	

	private Config() {
		// Private constructor to restrict new instances
		
		InputStream in = null;
		try {
		in = new FileInputStream("C:\\TestAutomationTool\\config\\config.properties");		
		configProp.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static class LazyHolder {
		private static final Config INSTANCE = new Config();
	}

	public static Config getInstance() {
		return LazyHolder.INSTANCE;
	}

	public String getProperty(String key) {
		return configProp.getProperty(key);
	}
	
	public  Properties getConfig() {
		return configProp;
	}

}
