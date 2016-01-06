package com.semantria.test;

import com.semantria.*;
import com.semantria.interfaces.ISerializer;
import com.semantria.mapping.Document;
import com.semantria.mapping.output.DocAnalyticData;
import com.semantria.mapping.output.DocEntity;
import com.semantria.mapping.output.DocTheme;
import com.semantria.serializer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 
 * @author Alex Kidston
 * @author Semantria TextAnalysis class Java version of Python Quick Start
 *         Detailed Analysis app
 * 
 */
public class TextAnalysis {

	/**
	 * Semantria Session
	 */
	private Session session;
	/**
	 * boolean to determine Session use of compression
	 */
	private boolean use_compression = false;
	/**
	 * String array of text blocks to be analysed
	 */
	private String[] initialTexts = {
			"Lisa - there's 2 Skinny cow coupons available $5 skinny cow ice cream coupons on special k boxes and Printable FPC from facebook - a teeny tiny cup of ice cream. I printed off 2 (1 from my account and 1 from dh's). I couldn't find them instore and i'm not going to walmart before the 19th. Oh well sounds like i'm not missing much ...lol",
		    "In Lake Louise - a guided walk for the family with Great Divide Nature Tours rent a canoe on Lake Louise or Moraine Lake  go for a hike to the Lake Agnes Tea House. In between Lake Louise and Banff - visit Marble Canyon or Johnson Canyon or both for family friendly short walks. In Banff  a picnic at Johnson Lake rent a boat at Lake Minnewanka  hike up Tunnel Mountain  walk to the Bow Falls and the Fairmont Banff Springs Hotel  visit the Banff Park Museum. The \"must-do\" in Banff is a visit to the Banff Gondola and some time spent on Banff Avenue - think candy shops and ice cream.",
		    "On this day in 1786 - In New York City  commercial ice cream was manufactured for the first time."
		    };
	/**
	 * Semantria Document
	 */
	private Document doc;
	/**
	 * Java universally unique identifier
	 */
	private UUID uuid;
	/**
	 * Integer httpClient status
	 */
	private Integer status;
	/**
	 * int length of intialTexts
	 */
	private int length;
	/**
	 * Semantria List<DocAnalyticData>
	 */
	private List<DocAnalyticData> results, statusList;
	/**
	 * Semantria DocAnalyticData
	 */
	private DocAnalyticData data;
	/**
	 * JSonSerializer Serializes and deserializes objects into and from the JSON
	 * format
	 */
	private ISerializer serializer = new JsonSerializer();

	public static void main(String[] args) {

		// declare & instantiate class
		TextAnalysis tA = new TextAnalysis();
		// run analysis method
		tA.sessionMethod();

	}

	/**
	 * Java version of Python Quick Start Detailed Analysis application
	 */
	public void sessionMethod() {
		// hold-all arraylist for analysis results from Semantria server
		results = new ArrayList<DocAnalyticData>();
		// initial receipt arraylist for analysis results from Semantria server
		statusList = new ArrayList<DocAnalyticData>();
		// compression set to true
		setUse_compression(true);
		// instantiate Semantria server session
		// String - user key
		// String - user secret
		// JSonSerializer serializer instance
		// compression set to true
		session = Session.createSession("key",
				"secret", serializer,
				isUse_compression());
		// for each loop
		// instantiates Semantria Document object
		// with each string in initialTexts array
		for (String s : initialTexts) {
			// Document constructor (UUID, string)
			doc = new Document(UUID.randomUUID().toString().replace("-", ""), s);
			// httpClient status report from Session
			// method queueDocument(document)
			// returns integer
			status = session.queueDocument(doc);
			// output status to console
			System.out.println("Semantria HTTP status: " + status);
			// if statement checks status code
			// Semantria status codes in API Glossary
			if (status == 202) {
				System.out.println("\"" + doc.getId()
						+ "\" document queued successfully." + "\r\n");
			}
		}
		// set length to String[] length
		length = initialTexts.length;
		// while loop
		// compares returned list.size()
		// to original String[] length
		while (results.size() < length) {
			System.out.println("Retrieving your processed results..." + "\r\n");
			try {
				// allow for communication latency
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// get processed documents
			// receipt list statusList
			statusList = session.getProcessedDocuments();
			// add received list to hold-all list
			results.addAll(statusList);
		}

		// for loop to output results to console
		for (int loop = 0; loop < results.size(); loop++) {
			// define DocAnalyticData object for each result
			data = results.get(loop);
			// print object ID, sentiment score & sentiment polarity
			System.out.println("Document " + data.getId()
					+ " Sentiment score: " + data.getSentimentScore()
					+ " Sentiment polarity: " + data.getSentimentPolarity()
					+ "\r\n");

			// print object themes
			if (data.getThemes() != null) {
				System.out.println("Document themes:" + "\r\n");
				// for each handles attributes of each DocTheme object
				// print theme title, strength score & sentiment polarity
				for (DocTheme theme : data.getThemes()) {
					System.out.println("     " + theme.getTitle()
							+ " (sentiment: " + theme.getStrengthScore()
							+ " Sentiment polarity: "
							+ data.getSentimentPolarity() + ")" + "\r\n");
				}
			}
			// print object entities
			if (data.getEntities() != null) {
				System.out.println("Entities:" + "\r\n");
				// for each handles attributes of each DocEntity object
				// print entity title, type, sentiment score & sentiment
				// polarity
				for (DocEntity entity : data.getEntities()) {
					System.out.println("\t" + entity.getTitle() + " : "
							+ entity.getEntityType() + " (sentiment: "
							+ entity.getSentimentScore()
							+ " Sentiment polarity: "
							+ data.getSentimentPolarity() + ")" + "\r\n");
				}

			}

		}
	}

	/**
	 * getter for boolean use_compression
	 * 
	 * @return boolean
	 */
	public boolean isUse_compression() {
		return use_compression;
	}

	/**
	 * setter for booloean use_compression
	 * 
	 * @param use_compression
	 *            boolean
	 * @return boolean
	 */
	public boolean setUse_compression(boolean use_compression) {
		this.use_compression = use_compression;
		return use_compression;
	}

	/**
	 * getter for DocAnalyticData object
	 * 
	 * @return DocAnalyticData
	 */
	public DocAnalyticData getData() {
		return data;
	}

	/**
	 * setter for DocAnalyticData object
	 * 
	 * @param data
	 *            DocAnalyticData
	 */
	public void setData(DocAnalyticData data) {
		this.data = data;
	}

	/**
	 * getter for UUID
	 * 
	 * @return uuid
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * setter for UUID
	 * 
	 * @param uuid
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}
