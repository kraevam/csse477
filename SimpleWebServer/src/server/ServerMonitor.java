package server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Marina Kraeva (kraevam@rose-hulman.edu)
 */
public enum ServerMonitor {

	INSTANCE;
	
	public static final int MAX_CONNECTIONS_PER_CLIENT = 150;
	public static final int MAX_DESIRABLE_CONNECTIONS_PER_CLIENT = 100;
	private Map<String, Integer> connectionsPerClient;
	private boolean isDegraded;
	private boolean isFailed;
	private List<String> attackers;
	
	/**
	 * 
	 */
	private ServerMonitor() {
		this.connectionsPerClient = Collections.synchronizedMap(new HashMap<String, Integer>());
		this.attackers = new ArrayList<String>();
		this.isDegraded = false;
		this.isFailed = false;
	}

	public void addRequest(String clientIP) {
		if(!connectionsPerClient.containsKey(clientIP)) {
			connectionsPerClient.put(clientIP, 1);
		} else {
			int currentConnections = connectionsPerClient.get(clientIP);
			connectionsPerClient.put(clientIP, currentConnections + 1);
			if(currentConnections + 1 >= MAX_CONNECTIONS_PER_CLIENT) {
				this.isDegraded = true;
			}
		}
	}
	
	public void removeRequest(String client) {
		if (client == null)
			return;
		int currentClientCount = connectionsPerClient.get(client);
		if (currentClientCount > 0) {
			currentClientCount--;
			if (currentClientCount >= 0) {
				connectionsPerClient.put(client, currentClientCount);
			}
		}
		if (attackers.contains(client) && currentClientCount < MAX_DESIRABLE_CONNECTIONS_PER_CLIENT) {
			attackers.remove(client);
			if (attackers.isEmpty()) {
				this.isDegraded = false;
				this.isFailed = false;
			}
		}
	}
	
	public void notifyDoSAttack(Socket socket) { 
		System.out.print("Time: " + System.currentTimeMillis());
		System.out.println(" DoS Attack detected on socket: " + socket.toString());
		attackers.add(socket.getInetAddress().toString());
	}
	
	public void notifyRecoveredFromDoS () {
		this.isDegraded = false;
	}
	
	public boolean isDegraded() {
		return this.isDegraded;
	}
	
	public boolean isFailed() {
		return this.isFailed;
	}
}
