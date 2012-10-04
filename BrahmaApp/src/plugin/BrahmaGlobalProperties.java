package plugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/** 
 * This is a singleton class giving access to all global properties for Liza
 * @author kraevam
 *
 */
public enum BrahmaGlobalProperties {

	INSTANCE;
	
	public final static String LOADABLE_APPS_DIR_PROPERTY = "loadableApplicationsDir";
	public final static String EXTENSIONS_DIR_PROPERTY = "extensionsDir";
	
	private final static String DEFAULT_APPS_DIR_VALUE = FileSystems.getDefault().getPath("plugins").toString();
	private final static String DEFAULT_EXTENSIONS_DIR_VALUE = FileSystems.getDefault().getPath("plugins").toString();
	private final static String PROPERTIES_FILE = "config/brahma.properties";
	
	private Map<String, String> properties;
	
	private BrahmaGlobalProperties() {
		properties = new HashMap<String, String>();
		readPropertiesFile();
	}
	
	private void readPropertiesFile() {
		try {
			FileReader fReader = new FileReader(PROPERTIES_FILE);
			BufferedReader reader = new BufferedReader(fReader);
			
			String line = reader.readLine();
			while(line != null) {
				StringTokenizer st = new StringTokenizer(line, "=");
				if (st.countTokens() == 2) {
					// expected pair <key>=<value>
					String key = st.nextToken();
					String value = st.nextToken();
					properties.put(key, value);
				}
				line = reader.readLine();
			}
			
			fReader.close();
			reader.close();

		} catch (IOException e) {
			// problems reading the file, use default values
			properties.put(LOADABLE_APPS_DIR_PROPERTY, DEFAULT_APPS_DIR_VALUE);
			properties.put(EXTENSIONS_DIR_PROPERTY, DEFAULT_EXTENSIONS_DIR_VALUE);
		}
	}
	
	public String getProperty(String key) {
		return properties.get(key);
	}
}
