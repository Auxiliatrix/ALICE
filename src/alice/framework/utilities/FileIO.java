package alice.framework.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileIO {
	
	public static void writeToFile(String fileName, String content) {
		File outfile = new File(fileName);
		try ( FileWriter fileWriter = new FileWriter(outfile) ) {
			outfile.getParentFile().mkdirs();
			outfile.createNewFile();
			fileWriter.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String readFromFile(String fileName) {
		File infile = new File(fileName);
		StringBuilder content = new StringBuilder();
		try ( BufferedReader br = new BufferedReader(new FileReader(infile)) ) {
			infile.getParentFile().mkdirs();
			infile.createNewFile();
			
			String line;
			while( (line = br.readLine()) != null ) {
				content.append(line).append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content.toString();
	}
}
