package com.semantria;

import com.semantria.interfaces.ISerializer;
import com.semantria.mapping.Document;
import com.semantria.mapping.output.*;
import com.semantria.serializer.JsonSerializer;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;

public class DetailedModeTestApp
{
    private static final int TIMEOUT_BEFORE_GETTING_RESPONSE = 500; //in millisec

	public static void main(String args[])
	{
		// Use correct Semantria API credentias here
		String key = "f4a9add2-627c-4cd9-bf3d-08dba99cc048";
		String secret = "fb17fecf-6919-4c98-994d-a0cee0730c00";

		if( args != null && args.length == 2 )
		{
			key = args[0];
			secret = args[1];
		}

        Hashtable<String, TaskStatus> docsTracker = new Hashtable<String, TaskStatus>();
		List<String> initialTexts = new ArrayList<String>();
		
		System.out.println("Semantria service demo.");

        //File file = new File( DetailedModeTestApp.class.getProtectionDomain().getCodeSource().getLocation().toString().replace("/target/classes", "/src/main/java/" + DiscoveryModeTestApp.class.getPackage().getName().replace(".","/")).replace("file:/", "")+ "/source.txt");
		File file = new File( System.getProperty("user.dir") + "/source.txt");
		
        if( !file.exists() )
        {
            System.out.println("Source file isn't available.");
            return;
        }

        //Reads dataset from the source file
        try
        {
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            while ((strLine = br.readLine()) != null)
            {
                if (StringUtils.isEmpty(strLine) || strLine.length() < 3)
                {
                    continue;
                }

                initialTexts.add(strLine);
            }
            in.close();
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
		
		// Creates JSON serializer instance
		//ISerializer jsonSerializer = new JsonSerializer();
		// Initializes new session with the serializer object and the keys.
        ISerializer serializer = new JsonSerializer();
		Session session = Session.createSession(key, secret, serializer, true);
		session.setCallbackHandler(new CallbackHandler());

        //Obtaining subscription object to get user limits applicable on server side
        Subscription subscription = session.getSubscription();

        List<Document> outgoingBatch = new ArrayList<Document>(subscription.getBasicSettings().getBatchLimit());

        for(Iterator<String> iterator = initialTexts.iterator(); iterator.hasNext(); ) {
            String uid = UUID.randomUUID().toString();
            Document doc = new Document(uid, iterator.next());
            //System.out.println("doc content = \n\n\n\n\n"+doc.toString()+"\n\n\n\n");
            outgoingBatch.add(doc);
            docsTracker.put(uid, TaskStatus.QUEUED);

            if (outgoingBatch.size() == subscription.getBasicSettings().getBatchLimit()) {
                if(session.queueBatch(outgoingBatch) == 202) {
                    System.out.println("\"" + outgoingBatch.size() + "\" documents queued successfully.");
                    outgoingBatch.clear();
                }
            }
        }

        if (outgoingBatch.size() > 0)
        {
            if( session.queueBatch(outgoingBatch) == 202) {
                System.out.println("\"" + outgoingBatch.size() + "\" documents queued successfully.");
                outgoingBatch.clear();
            }
        }

		//System.out.println();
		try
		{
			List<DocAnalyticData> processed = new ArrayList<DocAnalyticData>();
			
			while(docsTracker.containsValue(TaskStatus.QUEUED))
			{
                // As Semantria isn't real-time solution you need to wait some time before getting of the processed results
                // In real application here can be implemented two separate jobs, one for queuing of source data another one for retrieving
                // Wait half of second while Semantria process queued document
                Thread.currentThread().sleep(TIMEOUT_BEFORE_GETTING_RESPONSE);

				// Requests processed results from Semantria service
				List<DocAnalyticData> temp = session.getProcessedDocuments();
                for(Iterator<DocAnalyticData> i = temp.iterator(); i.hasNext(); ) {
                    DocAnalyticData item = i.next();

                    if (docsTracker.containsKey(item.getId())) {
                        processed.add(item);
                        docsTracker.put(item.getId(), TaskStatus.PROCESSED);
                    }
                }

                System.out.println("Retrieving your processed results...");
			}

			StringBuffer sbr = new StringBuffer();
			sbr.append("\ndoc_id,doc_sentiment_score,doc_sentiment_polarity,cat_topic,cat_strength_score,theme_title,theme_sentiment_score,theme_sentiment_polarity,entity_title,entity_sentiment_score,entity_sentiment_polarity");
			// Output results
			for(DocAnalyticData doc : processed)
			{
				//(!"".isEmpty() && null!="") ? return "" : "";
				
				//String str="";
				//System.out.println((str.isEmpty() && str.length()>0) ? str : "null");
				
				
				sbr.append("\n"+(checkNull(doc.getId())+","
							+checkNull(Float.toString(doc.getSentimentScore()))+","
						    +checkNull(doc.getSentimentPolarity())+","));
				
				System.out.println("Document:\n\tid: " + doc.getId() + "\n\tsentiment score: " + Float.toString(doc.getSentimentScore()) + "\n\tsentiment polarity: " + doc.getSentimentPolarity());
				System.out.println();
				if(doc.getAutoCategories() != null)
				{
					System.out.println("\tdocument categories:");
					for(DocCategory category : doc.getAutoCategories())
					{
						System.out.println("\t\ttopic: " + category.getTitle() + " \n\t\tStrength score: " + Float.toString(category.getStrengthScore()));
						System.out.println();
						sbr.append(checkNull(category.getTitle())+","+checkNull(Float.toString(category.getStrengthScore()))+",");
					}
				}
				else
				{
					sbr.append("null,"+"null,");
				}
				if(doc.getThemes() != null)
				{
					System.out.println("\tdocument themes:");
					for(DocTheme theme : doc.getThemes())
					{
						System.out.println("\t\ttitle: " + theme.getTitle() + " \n\t\tsentiment: " + Float.toString(theme.getSentimentScore()) + "\n\t\tsentiment polarity: " + theme.getSentimentPolarity());
						System.out.println();
						sbr.append(checkNull(theme.getTitle())+","+checkNull(Float.toString(theme.getStrengthScore()))+","+checkNull(theme.getSentimentPolarity())+",");
					}
				}
				else
				{
					sbr.append("null,"+"null,"+"null,");
				}
				if(doc.getEntities() != null)
				{
					System.out.println("\tentities:");
					for(DocEntity entity : doc.getEntities())
					{
						System.out.println("\t\ttitle: " + entity.getTitle() + "\n\t\tsentiment: " + Float.toString(entity.getSentimentScore()) + "\n\t\tsentiment polarity: " + entity.getSentimentPolarity());
						System.out.println();
						sbr.append(checkNull(entity.getTitle())+","+checkNull(Float.toString(entity.getSentimentScore()))+","+checkNull(entity.getSentimentPolarity()));
					}
				}else
				{
					sbr.append("null,"+"null,"+"null");
				}
			}
			System.out.println("processed results : \n"+sbr.toString());
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	private  static String checkNull(String str){
		return (!str.isEmpty() && str.length()>0) ? str : "null";
	}
}
