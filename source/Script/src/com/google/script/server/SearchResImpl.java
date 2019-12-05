package com.google.script.server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import com.google.script.server.index.MyIndexReader;
import com.google.script.server.index.MyIndexWriter;
import com.google.script.server.index.PreProcessedCorpusReader;
import com.google.script.server.search.ExtractQuery;
import com.google.script.server.search.QueryRetrievalModel;
import com.google.script.client.SearchRes;
import com.google.script.server.helper.Document;
import com.google.script.server.helper.Query;

/**
 * !!! YOU CANNOT CHANGE ANYTHING IN THIS CLASS !!!
 * 
 * Main class for running your HW3.
 * 
 */
@SuppressWarnings("serial")
public class SearchResImpl extends RemoteServiceServlet implements SearchRes{

	public ArrayList<String> getSearchRes(String content) {
		// Initialization.
		MyIndexReader ixreader = null;
		try {
			ixreader = new MyIndexReader("trectext");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		QueryRetrievalModel model = new QueryRetrievalModel(ixreader);
		
		// Extract the queries.
//		ExtractQuery queries = new ExtractQuery("Let's just eat and we'll talk about " + 
//				"it after. I'm starving.");
		ExtractQuery queries = new ExtractQuery(content);
		long startTime = System.currentTimeMillis();
		ArrayList<String> res = new ArrayList<String>();
		while (queries.hasNext()) {
			Query aQuery = queries.next();
			System.out.println(aQuery.GetTopicId() + "\t" + aQuery.GetQueryContent());
			// Retrieve 20 candidate documents on the indexing.
			List<Document> results = null;
			try {
				results = model.retrieveQuery(aQuery, 10);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (results != null) {
				int rank = 1;
				for (Document result : results) {
					res.add(result.docno());
					System.out.println(aQuery.GetTopicId() + " Q0 " + result.docno() + " " + rank + " " + result.score());
					rank++;
				}
			}
		}
		return res;
	}

}
