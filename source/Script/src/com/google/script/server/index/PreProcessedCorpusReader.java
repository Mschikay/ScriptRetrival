package com.google.script.server.index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.script.server.helper.Path;

public class PreProcessedCorpusReader {

	private FileReader fr;
	private BufferedReader br;

	public PreProcessedCorpusReader(String type) throws IOException {
		// This constructor opens the pre-processed corpus file, Path.ResultHM1 + type
		// You can use your own version, or download from http://crystal.exp.sis.pitt.edu:8080/iris/resource.jsp
		// Close the file when you do not use it any more
//		fr = new FileReader(Path.RawDataResDir + type);
		fr = new FileReader(Path.RawDataResDir);
		br = new BufferedReader(fr);
	}
	

	public Map<String, String> NextDocument() throws IOException {
		// read a line for docNo, put into the map with <"DOCNO", docNo>
		// read another line for the content , put into the map with <"CONTENT", content>
		Map<String, String> doc = new HashMap<>();
		String line = br.readLine();
		if (line == null) {
		    br.close();
		    fr.close();
		    return null;
        }
        String docNo = line;
		doc.put("DOCNO", docNo.trim());
		String content = "";
		line = br.readLine();
		if (line != null) {
		    content = line;
        }
		doc.put("CONTENT", content.trim());
        return doc;
	}

}