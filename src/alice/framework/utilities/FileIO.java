package alice.framework.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileIO {
	
	public static synchronized void writeToFile(String fileName, String content) {
		File outfile = new File(fileName);
		if( outfile.getParentFile() != null ) {
			outfile.getParentFile().mkdirs();
		}
		try {
			outfile.createNewFile();
		} catch (IOException e) {}
		
		try ( FileWriter fileWriter = new FileWriter(outfile) ) {
			fileWriter.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized String readFromFile(String fileName) {
		return readFromFile(fileName, "");
	}
	
	public static synchronized String readFromFile(String fileName, String defaultContent) {
		File infile = new File(fileName);
		if( infile.getParentFile() != null ) {
			infile.getParentFile().mkdirs();
		}
		try {
			infile.createNewFile();
			FileWriter fileWriter = new FileWriter(fileName);
			fileWriter.write(defaultContent);
			fileWriter.close();
		} catch (IOException e) {}
		
		StringBuilder content = new StringBuilder();
		try ( BufferedReader br = new BufferedReader(new FileReader(infile)) ) {
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
