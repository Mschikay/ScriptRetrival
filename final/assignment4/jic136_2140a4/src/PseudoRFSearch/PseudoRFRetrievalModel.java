package PseudoRFSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Classes.Document;
import Classes.Query;
import IndexingLucene.MyIndexReader;
import SearchLucene.MyQueryRetrievalModel;

public class PseudoRFRetrievalModel {

	private MyIndexReader ixreader;
	private MyQueryRetrievalModel qrm;
	private HashMap<String, Long> collectionFrequency;
	private HashMap<Integer, HashMap<String, Integer>> postingLists;
	private long totalTermFreq;

	private final int miu = 2000;
	
	public PseudoRFRetrievalModel(MyIndexReader ixreader) throws IOException {
		this.ixreader = ixreader;
		qrm = new MyQueryRetrievalModel(this.ixreader);
		totalTermFreq = ixreader.getTotalTermFreq();
		postingLists = qrm.getPostingLists();
		collectionFrequency = qrm.getMyCollectionFreq();
	}
	
	/**
	 * Search for the topic with pseudo relevance feedback in 2017 spring assignment 4. 
	 * The returned results (retrieved documents) should be ranked by the score (from the most relevant to the least).
	 * 
	 * @param aQuery The query to be searched for.
	 * @param TopN The maximum number of returned document
	 * @param TopK The count of feedback documents
	 * @param alpha parameter of relevance feedback model
	 * @return TopN most relevant document, in List structure
	 */
	public List<Document> RetrieveQuery( Query aQuery, int TopN, int TopK, double alpha) throws Exception {	
		// this method will return the retrieval result of the given Query, and this result is enhanced with pseudo relevance feedback
		// (1) you should first use the original retrieval model to get TopK documents, which will be regarded as feedback documents
		// (2) implement GetTokenRFScore to get each query token's P(token|feedback model) in feedback documents
		// (3) implement the relevance feedback model for each token: combine the each query token's original retrieval score P(token|document) with its score in feedback documents P(token|feedback model)
		// (4) for each document, use the query likelihood language model to get the whole query's new score, P(Q|document)=P(token_1|document')*P(token_2|document')*...*P(token_n|document')

		String queries = aQuery.GetQueryContent();
		String[] terms = queries.split(" ");

		// beta = 1 - alpha
		double beta = 1.0 - alpha;

		//get P(token|feedback documents)
		HashMap<String, Double> TokenRFScore = GetTokenRFScore(aQuery,TopK);

		/**
		 * P(Q|M’D) = αP(Q|MD) + (1 - α)P(Q|F)
		 */

		List<Document> results = new ArrayList<>();

		/**
		 *	For each document (specified by document id - docid), we calculate each query term's probability
		 *	P(term|D), then combine this score with P(term|feedback documents) by using the formula
		 *	P(term|D') = αP(term|D) + (1 - α)P(term|FD)
		 */
		for (Integer docid: postingLists.keySet()) {
			HashMap<String, Integer> termToFreq = postingLists.get(docid);
			long docLen = ixreader.docLength(docid);

			double a = (double) docLen / (docLen + miu);
			double b = (double) miu / (docLen + miu);

			double score = 1.0;
			for (String term: terms) {
				long currCF = collectionFrequency.get(term);
				if (currCF != 0L) {
					int currTF = termToFreq.getOrDefault(term, 0);

					double x = (double) currTF / docLen;
					double y = (double) currCF / totalTermFreq;
					score *= (alpha * (a * x + b * y) + beta * TokenRFScore.getOrDefault(term, 0.0));
				}
			}
			results.add(new Document(docid.toString(), ixreader.getDocno(docid), score));
		}

		// sort all retrieved documents from most relevant to least, and return TopN
		results.sort((doc1, doc2) -> Double.compare(doc2.score(), doc1.score()));
		
		return results.subList(0, TopN);
	}
	
	public HashMap<String,Double> GetTokenRFScore(Query aQuery,  int TopK) throws Exception
	{
		// for each token in the query, you should calculate token's score in feedback documents: P(token|feedback documents)
		// use Dirichlet smoothing
		// save <token, score> in HashMap TokenRFScore, and return it
		HashMap<String, Double> TokenRFScore = new HashMap<>();
		String conent = aQuery.GetQueryContent();
		String[] terms = conent.split(" ");

		/**
		 * Since we need to treat all of the relevance feedback documents as a big document
		 * So we must compute the document of this big document,
		 * and each term's document frequency.
		 */

		HashMap<String, Long> docTermFreq = new HashMap<>();
		List<Document> topKDocs = qrm.retrieveQuery(aQuery, TopK);
		long totalDocLength = 0;
		for (Document doc: topKDocs) {
			int docid = Integer.parseInt(doc.docid());
			totalDocLength += ixreader.docLength(docid);
			HashMap<String, Integer> posting = postingLists.getOrDefault(docid, new HashMap<>());
			if (posting.isEmpty())
				continue;
			for (String term: posting.keySet()) {
				int tf = posting.getOrDefault(term, 0);
				long preTf = docTermFreq.getOrDefault(term, 0L);
				docTermFreq.put(term, tf + preTf);
			}
		}

		for (String term: terms) {
			double a = (double) totalDocLength / (totalDocLength + miu);
			double b = (double) miu / (totalDocLength + miu);
			long cf = collectionFrequency.getOrDefault(term, 0L);
			double score;
			if (cf != 0L) {
				long tf = docTermFreq.getOrDefault(term, 0L);
				double x = (double) tf / totalDocLength;
				double y = (double) cf / totalTermFreq;
				score = a * x + b * y;
				TokenRFScore.put(term, score);
			}
		}

		return TokenRFScore;
	}
}