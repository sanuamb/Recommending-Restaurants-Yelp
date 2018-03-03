package FeatureProbabilisticRecommendation;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class FileReader 
{	
	public static StringBuilder getFileText(String path) {
		String line_text;
        File file;
        StringBuilder file_text_builder;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
        BufferedReader buffered_reader;
		Charset charset = Charset.forName("US-ASCII");
		
    	line_text = new String();
    	file_text_builder = new StringBuilder();
		
    	file = new File(path);
    	try {
    		fis = new FileInputStream(file);
    		bis = new BufferedInputStream(fis);
        	
    		buffered_reader = new BufferedReader(new InputStreamReader(bis,charset));
    		
			while ((line_text = buffered_reader.readLine()) != null) {
				file_text_builder.append(line_text);
				line_text = "";
    		}	
    	}
    	catch(IOException e) {
    		System.out.println("Cannot read file "+path+" for indexing. Please check.");
    	}
		
		return file_text_builder;
	}
}
