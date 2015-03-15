package ro.pub.acs.traffic.collector;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

import com.mysql.jdbc.Statement;

class ConnectionThread extends Thread {
	
	Socket socket;
	boolean debug;
	DBManager db;
	
	
	public static final String DATE_FORMAT_NOW = "yyyy_MM_dd_HH_mm_ss";
	
	/**
	 * Constructor for the ConnectionThread class.
	 * @param socket the socket connecting to the client
	 * @param debug boolean value for printing debug data
	 */
	public ConnectionThread(Socket socket, boolean debug) {
		this.socket = socket;
		this.debug = debug;
	}

	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			String nextLine;
			
			// read the first line.
			nextLine = in.readLine();
			if (debug)
				System.out.println(nextLine);
			
			// exit if the message format is incorrect.
			if (nextLine.startsWith("#s#"))
				out.println("ACK");
			else
				return;
			
			nextLine = nextLine.replace("#s#", "");
			Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
			String filename = "journey" + URLEncoder.encode(sdf.format(cal.getTime()), "UTF-8") + ".log";
			
			File file = new File("logs", filename);
			
			FileWriter fstream = new FileWriter(file, true);
			BufferedWriter outFile = new BufferedWriter(fstream);
			
			StringTokenizer st = new StringTokenizer(nextLine, "#");
			
			String name = st.nextToken();
			String facebook = st.nextToken();
			String twitter = st.nextToken();
			String id_user = st.nextToken();
			String username = st.nextToken();
			CryptTool ct = new CryptTool();
			String current_token = st.nextToken();
			String password = current_token.equals("0") ? "" : ct.decrypt(current_token);
			current_token = st.nextToken();
			String staticId = current_token.equals("0") ? "" : ct.decrypt(current_token);
			//System.out.println("line1: " + nextLine);
			
			try {
				db = new DBManager();
				Statement statement = (Statement) db.getConn().createStatement();
			
				ResultSet rs = null; 
				rs = statement.executeQuery("SELECT * FROM location WHERE id_user='" + id_user + "'");
				if(!rs.next())
					db.doQuery("INSERT INTO location " +
								"(id_user, name, facebook, twitter, lat, lng, speed, timestamp, stop) " +
								"VALUES " +
								"('" + id_user + "', '" + name + "', '" + facebook + "', '" + twitter + "', '', '', '', '', 0)");
				else { 
					db.doQuery("UPDATE location SET " +
								"name = '" + name + "', " +
								"facebook = '" + facebook + "', " +
								"twitter = '" + twitter + "' " +
							"WHERE id_user='" + id_user + "'");
				}
				rs = statement.executeQuery("SELECT * FROM users WHERE uuid='" + staticId + "'");
				if(!rs.next())
				{
					db.doPreparedQuery("INSERT INTO users " +
								"(username, password, facebook_key, name, facebook, uuid) " +
								"VALUES " +
								"(?, ?, ?, ?, ?, ?)", 
								new String[] {username, password, "0", name, facebook, staticId});
				}
				else
					db.doPreparedQuery("UPDATE users SET " +
							"username = ?, " +
							"password = ?, " +
							"name = ? " +
						"WHERE uuid='" + staticId + "'", 
						new String[] {username, password, name});
				/*System.out.println("username = " + username + "\n" +
						"password = " + password + "\n" +
						"name = " + name);*/
				db.doQuery("INSERT INTO history " +
							"(id_user, file) " +
							"VALUES " +
							"('" + staticId + "', '" + filename + "')");
				db.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			
			outFile.write(nextLine);
			outFile.newLine();
			
			outFile.close();
			fstream.close();
			
			// receive data and write it to file.
			nextLine = in.readLine();
			if(nextLine != null)
			{
				while (!nextLine.equals("#f#")) {
					//System.out.println("line_while: " + nextLine);
					fstream = new FileWriter(file, true);
					outFile = new BufferedWriter(fstream);
					
					StringTokenizer pos = new StringTokenizer(nextLine, " ");
					String speed, timestamp;
					Double lat, lng;

					lat = Double.parseDouble(pos.nextToken());
					lng = Double.parseDouble(pos.nextToken());
					speed = pos.nextToken();
					timestamp = pos.nextToken();
					
					db = new DBManager();
					
					db.doQuery("UPDATE `location` SET " +
										"`lat`='" + lat + "', " +
										"`lng`='" + lng + "', " +
										"`speed`='" + speed + "', " +
										"`timestamp`='" + timestamp + "' " +
									"WHERE id_user='" + id_user + "'");
					
					db.close();
					
					outFile.write(nextLine);
					outFile.newLine();
					nextLine = in.readLine();
					
					outFile.close();
					fstream.close();
					
					if(file.length() == 0 || !Utils.checkFile(filename)) {
						db = new DBManager();
						
						db.doQuery("DELETE FROM `history` WHERE file='" + filename + "'");
						
						db.close();
						
						file.delete();
					}
				}
			}
			// close all open streams.
			
			db.close();
			out.close();
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Server error. Please restart.");
			return;
		}		
	}
}

public class TrafficCollectorServer {

	private static int listeningPort = 8082;
	/**
	 * Main method.
	 * @param args array of command line arguments
	 */
	public static void main(String args[]) {
		ServerSocket serverSocket;
		boolean debug = false;
		
		try {
			// create server socket on the 8082 port.
			serverSocket = new ServerSocket(listeningPort);
			
			// set debug value.
			if (args.length == 1 && args[0].equals("-v")) {
				System.out.println("Debug mode activated");
				debug = true;
		    }

			System.out.println("Listening on port: " + listeningPort);
			
			while (true) {
				System.out.println("Accepted new connection");

				Socket clientSocket = serverSocket.accept();
				Thread connectionThread = new ConnectionThread(clientSocket, debug);
				new Thread(connectionThread).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Server error. Please restart.");
		}
	}
}
