package com.google.script.server.search;

import java.io.IOException;
import java.util.*;

import com.google.script.server.helper.Document;
import com.google.script.server.helper.Query;
import com.google.script.server.index.MyIndexReader;

public class QueryRetrievalModel {

	protected MyIndexReader indexReader;

	private HashMap<String, Long> myCollectionFreq = new HashMap<>();
	
	public QueryRetrievalModel(MyIndexReader ixreader) {
		indexReader = ixreader;
	}

	/**
	 * Search for the topic information.
	 * The returned results (retrieved documents) should be ranked by the score (from the most relevant to the least).
	 * TopN specifies the maximum number of results to be returned.
	 *
	 * @param aQuery The query to be searched for.
	 * @param TopN The maximum number of returned document
	 * @return
	 */

	public List<Document> retrieveQuery( Query aQuery, int TopN ) throws IOException {
		// NT: you will find our IndexingLucene.Myindexreader provides method: docLength()
		// implement your retrieval model here, and for each input query, return the topN retrieved documents
		// sort the documents based on their relevance score, from high to low
		int mu = 2000;
//		long totalTermFreq = indexReader.totalTermFreq();
		long totalTermFreq = 0;
		// store DOCID -> <TERM, FREQ>
        // which means for each document, store the frequency of the query term
		HashMap<Integer, HashMap<String, Integer>> mp = new HashMap<>();

		String[] qWords =  aQuery.GetQueryContent().split(" ");
		for (String word: qWords) {
			long collectionFreq = indexReader.GetCollectionFreq(word);

            myCollectionFreq.put(word, collectionFreq);
			if (collectionFreq != 0) {
				int[][] postingList = indexReader.GetPostingList(word);
				for (int[] p: postingList) {
					int docid = p[0];
					int freq = p[1];
					HashMap<String, Integer> termToFreq = mp.getOrDefault(docid, new HashMap<>());
					if (termToFreq.isEmpty()) {
						mp.putIfAbsent(docid, termToFreq);
					}
					termToFreq.put(word, freq);
				}
			} else {
			    System.out.println(word + " does not exist!!!");
            }
		}

		// calculate the score
		ArrayList<Document> results = new ArrayList<>(mp.size());
		totalTermFreq = indexReader.getTotalTermFreq();
		for (Integer docid: mp.keySet()) {
			HashMap<String, Integer> termToFreq = mp.get(docid);

            /**
             * According the formula,
             *      p(w|D) = ( c(w, D) + u * p(w|REF) ) / ( |D| + u )
             *             = |D| / ( |D| + u ) * ( c(w, D) / |D| ) + ( u / ( |D| + u )) * p(w|REF)
			 *
			 *      p(w|REF) = Term's collection frequency / the total length of all the documents
			 *      reference: http://mlwiki.org/index.php/Smoothing_for_Language_Models
			 *
             */


            double score = 1.0;
			long docLen = indexReader.getDocLen(docid);

			double a = 1.0 * docLen / (docLen + mu);
			double b = 1.0 * mu / (docLen + mu);

			for (String token: qWords) {
                long collFreq = myCollectionFreq.get(token);
				if (collFreq != 0) {
					double termFreq = termToFreq.getOrDefault(token, 0);
					score *= (a * termFreq / docLen + b * collFreq / totalTermFreq);
				}
			}
			results.add(new Document(docid.toString(), indexReader.GetDocno(docid), score));
		}

        // sort the results list order by descending
		results.sort((doc1, doc2) -> {
			double doc1Score = doc1.score();
			double doc2Score = doc2.score();
			if (doc1Score > doc2Score) {
				return -1;
			} else if (doc1Score < doc2Score) {
				return 1;
			} else {
				return 0;
			}
		});

		// get topN result, if topN > docRank.size() then just return all documents
		int n = Math.min(TopN, results.size());
		ArrayList<Document> retList = new ArrayList<>(n);
		int cnt = 0;
		for (Document doc: results) {
			retList.add(doc);
			cnt++;
			if (cnt > n - 1)
				break;
		}

		return retList;
	}

}
