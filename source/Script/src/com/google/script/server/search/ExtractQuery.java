package com.google.script.server.search;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.google.script.server.helper.Path;
import com.google.script.server.helper.Query;
import com.google.script.server.helper.Stemmer;

public class ExtractQuery {
	private ArrayList<Query> queries = new ArrayList<>();
	private Set<String> stopWords = new HashSet<>();
	private int topicNo = 0;
	private Iterator<Query> it;

	public ExtractQuery(String content) {
		//you should extract the 4 queries from the Path.TopicDir
		//NT: the query content of each topic should be 1) tokenized, 2) to lowercase, 3) remove stop words, 4) stemming
		//NT: you can simply pick up title only for query, or you can also use title + description + narrative for the query content.
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(Path.QueryDir));
//
//			String line;
//			String topicId;
//			ArrayList<String> contents = new ArrayList<>();
//			ArrayList<String> qId = new ArrayList<>();
//			while ((line = br.readLine()) != null) {
//				if (line.contains("<num>")) {
//					topicId = line.substring(6);
//					qId.add(topicId);
//				} else if (line.contains("<title>")) {
//					String curr = line.substring(8);
//					curr = preProcessQueries(curr);
//					contents.add(curr);
//					topicNo++;
//				}
//			}
//			for (int i = 0; i < qId.size(); ++i) {
		Query qu = new Query();
		Query aQuery = new Query();
		aQuery.SetTopicId("910");
		aQuery.SetQueryContent(" Indo-China Got a code tip from the bureau chief this morning The place is about to go up in smoke");
		queries.add(aQuery);
//		qu.SetTopicId("1");
//		qu.SetQueryContent(preProcessQueries(content));
//		queries.add(qu);
//			}
		it = queries.iterator();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}

	private String preProcessQueries(String curr) {
		// tokenize
		curr = tokenize(curr);

		// to lower case
		curr = curr.toLowerCase();

		// remove stop words
		curr = removeStopWords(curr);

		// stemming
		curr = stemmer(curr);

		return curr;

	}

	public boolean hasNext()
	{
		return it.hasNext();
	}

	public Query next()
	{
		if (it.hasNext())
			return it.next();
		else
			return null;
	}

	private String tokenize(String str) {
		String delim_escap = "\n\r";
		String delia_punc = ".,;:?!\"-+*/";
		StringTokenizer stringTokenizer = new StringTokenizer(str, delim_escap + delia_punc);
		StringBuilder res = new StringBuilder();
		while (stringTokenizer.hasMoreTokens()) {
			res.append(stringTokenizer.nextToken().trim()).append(" ");
		}
		return res.toString();
	}

	private String removeStopWords(String str) {
		buildStopWordList();
		StringBuilder res = new StringBuilder();
		String[] contents = str.split(" ");
		for (String s: contents) {
			if (!stopWords.contains(s)) {
				res.append(s).append(" ");
			}
		}
		return res.toString();
	}

	private String stemmer(String str) {
		StringBuilder res = new StringBuilder();
		String[] contents = str.split(" ");
		for (String s: contents) {
			Stemmer stemmer = new Stemmer();
			char[] chars = s.toCharArray();
			stemmer.add(chars, chars.length);
			stemmer.stem();
			res.append(stemmer.toString());
			res.append(" ");
		}
		return res.toString();
	}

	private void buildStopWordList() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(Path.StopwordDir));
			String line;
			while ((line = br.readLine()) != null) {
				stopWords.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


