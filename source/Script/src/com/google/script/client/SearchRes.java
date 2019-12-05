package com.google.script.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("search")
public interface SearchRes extends RemoteService{
	ArrayList<String> getSearchRes(String content) throws IllegalArgumentException;
}
