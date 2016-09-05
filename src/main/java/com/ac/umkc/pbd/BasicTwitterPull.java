package com.ac.umkc.pbd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The goal of this program is to access a number of tweets by the venerable Donald Trump (@realDonaldTrump),
 * sanitize the message content of special characters, then dump the message content and list of hash tags
 * into two different files.  These files are then meant to be uploaded to some big data system to that the
 * content can be crawled.
 * 
 * Technologies used here are:
 * <ul><li>Twitter REST API to get Tweet data using my personal developer account</li>
 * <li>Signpost for OAuth authentication to Twitter</li>
 * <li>Apache's Commons and HTTPComponents APIs for performing REST calls</li>
 * <li>JSON libraries to parse twitter content</li>
 * <li>Apache Hadoop Core and HDFS libraries for HDFS File writes</li>
 * <li>Maven is used as the build utility</li></ul>
 * 
 * This program expects as a single command-line parameter the folder path into which
 * two files - messageOutput.txt and hashTagOutput.txt - will be generated.  If these
 * files already exist, they will be replaced.
 * 
 * @author AC010168
 *
 */
public class BasicTwitterPull {

  /** OAuth Information for apshaiTerp Developer account on Twitter */
  private static final String ACCESS_TOKEN        = "239401078-eDjOFRN4gICrqcNFyZIojk260ektMEXo3whHBs3v";
  /** OAuth Information for apshaiTerp Developer account on Twitter */
  private static final String ACCESS_TOKEN_SECRET = "3T2JlTzARHylyslH6k4O8G35oRXll0YMXnkSgqqfkjJ9n";
  /** OAuth Information for apshaiTerp Developer account on Twitter */
  private static final String CONSUMER_KEY        = "rzdr518OzoEWmGbD0yie3yDfb";
  /** OAuth Information for apshaiTerp Developer account on Twitter */
  private static final String CONSUMER_SECRET     = "7AFOzobLLXLviMYaMhrXH2oLGlntW0G6tCHJi4LiddWh6AuDBn";
  
  /** Count of how many tweets I want to pull */
  private static final int TWEET_COUNT = 200;
  
  /**
   * @param args list of command line arguments
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println ("No file path provided as $1 input");
      return;
    }
    
    //create our output files, purging previous ones if possible.
    String parentFolder = args[0];
    File rootFolder  = new File(parentFolder);
    if (!rootFolder.exists()) {
      System.err.println ("The provided folder path does not exist");
      return;
    }
    if (!rootFolder.isDirectory()) {
      System.err.println ("The provided folder path is not a directory");
      return;
    }
    File messageFile = new File(rootFolder, "messageOutput.txt");
    File hashTagFile = new File(rootFolder, "hashTagOutput.txt");
    
    if (messageFile.exists()) messageFile.delete();
    if (hashTagFile.exists()) hashTagFile.delete();
    
    CloseableHttpClient   client   = null;
    CloseableHttpResponse response = null;
    
    PrintWriter messageWriter = null;
    PrintWriter hashTagWriter = null;
    
    try {
      OAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
      consumer.setTokenWithSecret(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
      
      HttpGet request = new HttpGet("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=realDonaldTrump&count=" + TWEET_COUNT);
      consumer.sign(request);
      
      client = HttpClients.createDefault();
      response = client.execute(request);
      
      HttpEntity entity = response.getEntity();
      JSONArray allTweets = new JSONArray(EntityUtils.toString(entity));
      
      messageWriter = new PrintWriter(new BufferedWriter(new FileWriter(messageFile))); 
      hashTagWriter = new PrintWriter(new BufferedWriter(new FileWriter(hashTagFile)));
      
      System.out.println ("Processing " + allTweets.length() + " tweets...");
      
      //Lets start parsing through tweets
      for (int i = 0; i < allTweets.length(); i++) {
        JSONObject tweet = allTweets.getJSONObject(i);
        
        //Get the message text
        String message = tweet.getString("text");
        
        //Get the list of HashTags used in this tweet
        String hashTagsList = "";
        JSONObject entities = tweet.getJSONObject("entities");
        JSONArray hashTags = entities.getJSONArray("hashtags");
        
        if (hashTags.length() == 0) {
          //DEBUG
          //System.out.println ("HashTags: ----");
        } else {
          JSONObject singleHashTag = hashTags.getJSONObject(0);
          //DEBUG
          //System.out.print("HashTags: " + singleHashTag.getString("text"));
          hashTagsList = singleHashTag.getString("text");
          for (int j = 1; j < hashTags.length(); j++) {
            singleHashTag = hashTags.getJSONObject(j);
            //DEBUG
            //System.out.print(", " + singleHashTag.getString("text"));
            hashTagsList += " " + singleHashTag.getString("text");
          }
          //DEBUG
          //System.out.println();
        }
        
        //We need to sanitize the String values, scrub out all non-alphanumeric stuff
        //We strip out URLs first, since the break all our other parsing techniques
        String scrubbedMessage = message;
        String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(scrubbedMessage);
        while (m.find()) {
          scrubbedMessage = scrubbedMessage.replaceAll(m.group()," ").trim();
        }

        //Remove the ampersand symbol
        scrubbedMessage = scrubbedMessage.replaceAll("&amp;", "and");
        //Remove all special characters besides ' @ # (So we retain user and hashtag hits, as well as punctuation)
        scrubbedMessage = scrubbedMessage.replaceAll("[^A-Za-z0-9'@# ]", " ");
        //Since we've been padding with whitespace, cut the spacing down to single space
        scrubbedMessage = scrubbedMessage.trim().replaceAll(" +", " ");
        
        //DEBUG
        //System.out.println ("Message:  " + message);
        //System.out.println ("Scrubbed: " + scrubbedMessage);
        //System.out.println ("HashTags: " + hashTagsList);
        
        messageWriter.println(scrubbedMessage);
        if (hashTagsList.trim().length() > 0)
          hashTagWriter.println(hashTagsList);
        
      }
      
      response.close();
      client.close();
      
      messageWriter.flush();
      hashTagWriter.flush();
      
    } catch (Throwable t) {
      System.out.println ("I had problems: " + t.getMessage());
      t.printStackTrace();
    } finally {
      try { client.close();   } catch (Throwable t) { /** Ignore Errors */ }
      try { response.close(); } catch (Throwable t) { /** Ignore Errors */ }
      try { messageWriter.close(); } catch (Throwable t) { /** Ignore Errors */ }
      try { hashTagWriter.close(); } catch (Throwable t) { /** Ignore Errors */ }
    }
    
    System.out.println ("Program Execution Complete.");
    System.out.println ("The following files were generated: ");
    System.out.println ("  " + messageFile.getAbsolutePath());
    System.out.println ("  " + hashTagFile.getAbsolutePath());
    
    //Now we begin the HDFS process
    try {
      Configuration hdfsConfiguration = new Configuration();
      FileSystem hdfs                 = FileSystem.get(hdfsConfiguration);
      
      Path localFile = new Path(messageFile.getAbsolutePath());
      Path hdfsFile  = new Path("/proj/messageOutput.txt");
      
      //If the HDFS version of the file already exists, purge it first
      if (hdfs.exists(hdfsFile)) {
        hdfs.delete(hdfsFile, false);
      }
      
      FSDataInputStream in   = hdfs.open(localFile);
      FSDataOutputStream out = hdfs.create(hdfsFile, true);

      IOUtils.copyBytes(in, out, hdfsConfiguration);
      
      try { in.close();  } catch (Throwable t) { /** Ignore Errors */ }
      try { out.close(); } catch (Throwable t) { /** Ignore Errors */ }
      
    } catch (Throwable t) {
      System.err.println ("Something bad happened: " + t.getMessage());
      t.printStackTrace();
    }
  }
}
