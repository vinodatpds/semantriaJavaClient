package com.semantria.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.semantria.src.Session;

public class TextComparison {

	private Session session;
	private boolean use_compression = false;
	private UUID uuid;
	private Integer status;
	private Collection docs;
	private DocAnalyticData data;
	private ISerializer serializer = new JsonSerializer();
	private String collection_id;
	private CollAnalyticData result;

	public static void main(String[] args) {

		TextComparison tC = new TextComparison();
		tC.compareMethod();

	}

	public void compareMethod() {

		session = Session.createSession("key", "secret", serializer,
				isUse_compression());

		collection_id = (UUID.randomUUID().toString());
		docs = new Collection(collection_id);
		docs.setDocuments(fileToArray("src/semantriaAJKTest/source.txt"));
		status = session.queueCollection(docs);

		if ((status != 200) && (status != 202)) {
			System.out.println("Error: ");
			System.out.println(status);
			System.exit(1);
		}

		System.out.println("Collection queued successfully. " + collection_id);

		result = null;
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Retrieving your processed results...");
			result = session.getCollection(collection_id);
			if (result.getStatus().toString() != "QUEUED") {
				break;
			}

		}
		if (result.getStatus().toString() != "PROCESSED") {
			System.out.println("Error: ");
			System.out.println(result.getStatus().toString());
			System.exit(1);
		}

		System.out.println();
		System.out.printf("%s : %s : %s : %s : %s \n", "Facet", "Count",
				"Positive", "Neutral", "Negative");
		for (Facet f : result.getFacets()) {
			System.out.printf("%s : %s : %s : %s : %s \n", f.getLabel(),
					f.getCount(), f.getPositiveCount(), f.getNeutralCount(),
					f.getNegativeCount());
		}

	}

	public static ArrayList<String> fileToArray(String file) {
		File newFile = new File(file);
		String line;
		ArrayList<String> stringList = new ArrayList<String>();
		System.out.println("Reading collection from source.txt file...");
		try {
			FileReader fr = new FileReader(newFile);
			BufferedReader br = new BufferedReader(fr);
			while ((line = br.readLine()) != null) {
				stringList.add(line);
				System.out.println(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringList;
	}

	public boolean isUse_compression() {
		return use_compression;
	}

	public boolean setUse_compression(boolean use_compression) {
		this.use_compression = use_compression;
		return use_compression;
	}

	public DocAnalyticData getData() {
		return data;
	}

	public void setData(DocAnalyticData data) {
		this.data = data;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

}
