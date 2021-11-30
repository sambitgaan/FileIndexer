package rb.com.care.purge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public class GetPurgeProperties {
	private String result = "";
	private InputStream inputStream;
	private String indexDirectory;
	private String dataDirectory;
	private Properties prop;
	public String getPropValues() throws IOException {
		try {
			Properties prop = new Properties();
			File configFile = new File("classes\\config.properties"); FileInputStream
			inputStream = new FileInputStream(configFile);
			prop.load(inputStream);
 			Date time = new Date(System.currentTimeMillis());
			dataDirectory = prop.getProperty("FILES_TO_INDEX_DIRECTORY");
			indexDirectory = prop.getProperty("INDEX_DIRECTORY");
 			result = "Directory Paths = " + dataDirectory + ", " + indexDirectory;
			System.out.println("Program Ran on " + time + " on folder = " + result);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return result;
	}
	
	public String getDataDirectory() {
		return prop.getProperty("FILES_TO_INDEX_DIRECTORY");
	}

	public String getIndexDirectory() {
		return prop.getProperty("INDEX_DIRECTORY");
	}

	public Properties getProp() {
		return prop;
	}

	public void setProp() throws IOException {
		prop = new Properties();
		File configFile = new File("classes\\config.properties"); inputStream = new
		FileInputStream(configFile);
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file: config.properties " + "not found in the classpath");
		}
	}

	public String getInputFile() {
		return prop.getProperty("LIST_INPUT_FILE");
	}

	public String getSearchedFile() {
		return prop.getProperty("SEARCHED_FILES");
	}
	
	public String getDeleteLogFile() {
		return prop.getProperty("DELETE_LOG");
	}
}
