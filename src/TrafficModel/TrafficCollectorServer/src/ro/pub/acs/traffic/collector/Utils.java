package ro.pub.acs.traffic.collector;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Utils {
	public static boolean checkFile(String filename) {
		int pos = 0;
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream("logs/" + filename);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = br.readLine();
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				StringTokenizer st= new StringTokenizer(strLine, " ");
				String speed = st.nextToken(); 
				speed = st.nextToken();
				if(!(speed.equals("-2")))
					pos++;
				if(pos == 1) break;
			}
			//Close the input stream
			in.close();
		} catch (Exception e) {
			//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		if(pos == 0)
			return false;
		return true;
	}
}