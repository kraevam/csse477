package server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import protocol.HttpRequest;

/**
 * 
 * @author Marina Kraeva (kraevam@rose-hulman.edu)
 */
public class ServerMonitor {

	private static final int MAX_CONNECTIONS_PER_CLIENT = 1000;
	private static Map<String, Integer> connectionsPerClient = Collections.synchronizedMap(new HashMap<String, Integer>());
	
	/**
	 * 
	 */
	public ServerMonitor() {
		// TODO Auto-generated constructor stub
	}

	public static void addRequest(String client) {
		if(!connectionsPerClient.containsKey(client)) {
			connectionsPerClient.put(client, 1);
		} else {
			int currentConnections = connectionsPerClient.get(client);
			connectionsPerClient.put(client, currentConnections + 1);
		}
	}
	
	public static void removeRequest(String client) {
		int currentClientCount = connectionsPerClient.get(client);
		if (currentClientCount > 0) {
			currentClientCount--;
			connectionsPerClient.put(client, currentClientCount);
		}
	}
}
