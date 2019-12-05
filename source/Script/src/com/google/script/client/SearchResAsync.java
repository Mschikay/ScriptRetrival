package com.google.script.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface SearchResAsync {
	void getSearchRes(String input, AsyncCallback<ArrayList<String>> callback) throws IllegalArgumentException;
}
