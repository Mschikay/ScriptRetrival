package com.google.script.client;

import com.google.script.shared.FieldVerifier;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
//import com.google.script.server.SearchResImpl;
//import com.google.script.server.helper.Document;
//import com.google.script.server.helper.Query;
//import com.google.script.server.index.MyIndexReader;
//import com.google.script.server.search.ExtractQuery;
//import com.google.script.server.search.QueryRetrievalModel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Script implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	
	private VerticalPanel mainPanel = new VerticalPanel();
    private FlexTable stocksFlexTable = new FlexTable();
    private HorizontalPanel addPanel = new HorizontalPanel();
    private TextBox newSymbolTextBox = new TextBox();
    private Button addStockButton = new Button("Search");
    private Label lastUpdatedLabel = new Label();
    private TextBox infoBox = new TextBox();

	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network " + "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
	private final SearchResAsync searchRes = GWT.create(SearchRes.class);
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// Assemble Add Stock panel.
		infoBox.setText("none");
	    addPanel.add(newSymbolTextBox);
	    addPanel.add(addStockButton);

	    // Assemble Main panel.
	    mainPanel.add(addPanel);
	    mainPanel.add(infoBox);
	    mainPanel.add(lastUpdatedLabel);

	    // Associate the Main panel with the HTML host page.
	    RootPanel.get("filmList").add(mainPanel);

	    // Move cursor focus to the input box.
	    newSymbolTextBox.setFocus(true);
	    
	    addStockButton.addClickHandler(new ClickHandler() {
	    	public void onClick(ClickEvent event) {
	    		try {
					addFilms();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    });
	    
	    newSymbolTextBox.addKeyDownHandler(new KeyDownHandler() {
	        public void onKeyDown(KeyDownEvent event) {
	          if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
	            try {
					addFilms();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	          }
	        }
	      });
	}
	
	private void addFilms() throws Exception {
		
		final String symbol = newSymbolTextBox.getText().toLowerCase().trim();
	    newSymbolTextBox.setFocus(true);
	    // TODO remove punctuation
	    newSymbolTextBox.setText("");
	    
	    searchRes.getSearchRes(symbol, new AsyncCallback<ArrayList<String>>() {
			public void onFailure(Throwable caught) {
				// Show the RPC error message to the user
//				dialogBox.setText("Remote Procedure Call - Failure");
//				serverResponseLabel.addStyleName("serverResponseLabelError");
//				serverResponseLabel.setHTML(SERVER_ERROR);
//				dialogBox.center();
//				closeButton.setFocus(true);
			}

			public void onSuccess(ArrayList<String> res) {
				for (int i = 0; i < res.size(); i++) {
			    	String str = res.get(i);
			    	stocksFlexTable.setText(i, 0, str);
			    }
				infoBox.setText("suc");
				mainPanel.add(stocksFlexTable);
			}
		});
	}
	
	
		
	    /*
		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				sendButton.setEnabled(true);
				sendButton.setFocus(true);
			}
		});

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			// Fired when the user clicks on the sendButton.
			 
			public void onClick(ClickEvent event) {
				sendNameToServer();
			}

			//Fired when the user types in the nameField.
			 
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendNameToServer();
				}
			}

			//Send the name from the nameField to the server and wait for a response.
			
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel.setText("");
				String textToServer = nameField.getText();
				if (!FieldVerifier.isValidName(textToServer)) {
					errorLabel.setText("Please enter at least four characters");
					return;
				}

				// Then, we send the input to the server.
				sendButton.setEnabled(false);
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");
				greetingService.greetServer(textToServer, new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						// Show the RPC error message to the user
						dialogBox.setText("Remote Procedure Call - Failure");
						serverResponseLabel.addStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML(SERVER_ERROR);
						dialogBox.center();
						closeButton.setFocus(true);
					}

					public void onSuccess(String result) {
						dialogBox.setText("Remote Procedure Call");
						serverResponseLabel.removeStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML(result);
						dialogBox.center();
						closeButton.setFocus(true);
					}
				});
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
		*/
	
}
