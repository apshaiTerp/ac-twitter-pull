package com.ac.umkc.twitter;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author AC010168
 *
 */
public class TwitterCall {

  /** OAuth Information for apshaiTerp Developer account on Twitter */
  private static final String ACCESS_TOKEN        = "239401078-eDjOFRN4gICrqcNFyZIojk260ektMEXo3whHBs3v";
  /** OAuth Information for apshaiTerp Developer account on Twitter */
  private static final String ACCESS_TOKEN_SECRET = "3T2JlTzARHylyslH6k4O8G35oRXll0YMXnkSgqqfkjJ9n";
  /** OAuth Information for apshaiTerp Developer account on Twitter */
  private static final String CONSUMER_KEY        = "rzdr518OzoEWmGbD0yie3yDfb";
  /** OAuth Information for apshaiTerp Developer account on Twitter */
  private static final String CONSUMER_SECRET     = "7AFOzobLLXLviMYaMhrXH2oLGlntW0G6tCHJi4LiddWh6AuDBn";
  
  /** Count of how many tweets I want to pull */
  private static final int MAX_TWEET_COUNT = 200;
  /** Count of the limit of tweets I can pull */
  private static final int TWEET_LIMIT     = 3200;
  /** The max number of followers that can be pulled in a single request */
  private static final int MAX_FOLLOWER_COUNT = 5000;
  
  /** Simple Date Formatter for output on wait times */
  private static final SimpleDateFormat formatter    = new SimpleDateFormat("HH:mm:ss");
  /** Formatter to help convert twitter UTC values to java Dates */
  private static final SimpleDateFormat utcFormatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
  /**
   * Helper method to access Twitter User data by userName
   * @param userName The userName we want to look up
   * @param userType The user Type we want to record this as
   * @param writer The PrintWriter required for the raw response dump
   * 
   * @return a {@link TwitterUser} object with the user info we care about
   */
  public static TwitterUser getTwitterUser(String userName, UserType userType, PrintWriter writer) {
    CloseableHttpClient   client   = null;
    CloseableHttpResponse response = null;
    
    TwitterUser twitterUser        = null;
    String responseString          = null;

    try {
      OAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
      consumer.setTokenWithSecret(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
      
      client = HttpClients.createDefault();

      String twitterURL = "https://api.twitter.com/1.1/users/lookup.json?screen_name=" + userName;
      HttpGet request = new HttpGet(twitterURL);
      consumer.sign(request);
      response = client.execute(request);
      
      HttpEntity entity = response.getEntity();
      responseString    = EntityUtils.toString(entity);
      
      try {
        JSONObject errors = new JSONObject(responseString);
        if (errors.has("errors")) {
          System.out.println (responseString);
          
          JSONObject errorDetails = errors.getJSONArray("errors").getJSONObject(0);
          int errorCode = errorDetails.getInt("code");
          
          if (errorCode == 17) {
            System.out.println ("No user matches for specified terms.");
            return null;
          }
          if (errorCode == 88) {
            System.out.println ("I hit my request limit and need to sleep for about 15 minutes here...");
            System.out.println ("  I will resume by " + formatter.format(new Date(System.currentTimeMillis() + 930000)));
            try { Thread.sleep(910000); } catch (Throwable t) {}
            System.out.println ("Resuming operation");
            return getTwitterUser(userName, userType, writer);
          }
        }
        if (errors.has("error")) {
          System.out.println (responseString);
          System.out.println ("I do not have persmission to get this data...");
          return null;
        }
      } catch (Throwable t) { /** Ignore Me */ }
      
      JSONArray allUsers = new JSONArray(responseString);
      
      for (int i = 0; i < allUsers.length(); i++) {
        JSONObject jsonUser = allUsers.getJSONObject(i);
        twitterUser         = new TwitterUser();
        
        //For the large dump
        writer.println(jsonUser);
        
        twitterUser.setUserType(userType);
        twitterUser.setTwitterID(jsonUser.getLong("id"));
        twitterUser.setUserName(jsonUser.getString("name").trim());
        twitterUser.setScreenName(jsonUser.getString("screen_name").trim());
        twitterUser.setFollowersCount(jsonUser.getInt("followers_count"));
        twitterUser.setFriendsCount(jsonUser.getInt("friends_count"));
        twitterUser.setStatusesCount(jsonUser.getInt("statuses_count"));
        if (jsonUser.has("location"))
          twitterUser.setLocation(jsonUser.getString("location").trim());
        
        //System.out.println (twitterUser.jsonify());
      }
      
      response.close();
      client.close();
      
      return twitterUser;
    } catch (Throwable t) {
      System.err.println("Something bad happened parsing user info for " + userName + "!");
      System.err.println (responseString);
      t.printStackTrace();
      return null;
    }
  }

  /**
   * Helper method to access Twitter User data by userName
   * @param userID The userID we want to look up
   * @param userType The user Type we want to record this as
   * @param writer The PrintWriter required for the raw response dump
   * 
   * @return a {@link TwitterUser} object with the user info we care about
   */
  public static TwitterUser getTwitterUser(long userID, UserType userType, PrintWriter writer) {
    CloseableHttpClient   client   = null;
    CloseableHttpResponse response = null;
    
    TwitterUser twitterUser        = null;
    String responseString          = null;

    try {
      OAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
      consumer.setTokenWithSecret(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
      
      client = HttpClients.createDefault();

      String twitterURL = "https://api.twitter.com/1.1/users/lookup.json?user_id=" + userID;
      HttpGet request = new HttpGet(twitterURL);
      consumer.sign(request);
      response = client.execute(request);
      
      HttpEntity entity = response.getEntity();
      responseString    = EntityUtils.toString(entity);
      
      try {
        JSONObject errors = new JSONObject(responseString);
        if (errors.has("errors")) {
          System.out.println (responseString);
          JSONObject errorDetails = errors.getJSONArray("errors").getJSONObject(0);
          int errorCode = errorDetails.getInt("code");
          
          if (errorCode == 17) {
            System.out.println ("No user matches for specified terms.");
            return null;
          }
          if (errorCode == 88) {
            System.out.println ("I hit my request limit and need to sleep for about 15 minutes here...");
            System.out.println ("  I will resume by " + formatter.format(new Date(System.currentTimeMillis() + 930000)));
            try { Thread.sleep(910000); } catch (Throwable t) {}
            System.out.println ("Resuming operation");
            return getTwitterUser(userID, userType, writer);
          }
        }
        if (errors.has("error")) {
          System.out.println (responseString);
          System.out.println ("I do not have persmission to get this data...");
          return null;
        }
      } catch (Throwable t) { /** Ignore Me */ }
      
      JSONArray allUsers = new JSONArray(responseString);
      
      for (int i = 0; i < allUsers.length(); i++) {
        JSONObject jsonUser = allUsers.getJSONObject(i);
        twitterUser         = new TwitterUser();
        
        //For the large dump
        writer.println(jsonUser);
        
        twitterUser.setUserType(userType);
        twitterUser.setTwitterID(jsonUser.getLong("id"));
        twitterUser.setUserName(jsonUser.getString("name").trim());
        twitterUser.setScreenName(jsonUser.getString("screen_name").trim());
        twitterUser.setFollowersCount(jsonUser.getInt("followers_count"));
        twitterUser.setFriendsCount(jsonUser.getInt("friends_count"));
        twitterUser.setStatusesCount(jsonUser.getInt("statuses_count"));
        if (jsonUser.has("location"))
          twitterUser.setLocation(jsonUser.getString("location").trim());
        
        //System.out.println (twitterUser.jsonify());
      }
      
      response.close();
      client.close();
      
      return twitterUser;
    } catch (Throwable t) {
      System.err.println("Something bad happened parsing user info for " + userID + "!");
      System.err.println(responseString);
      t.printStackTrace();
      return null;
    }
  }

  /**
   * Helper call to get the list of followers of this user.
   * @param userID The userID we want to look up
   * @param followerCount How many followers the user should have
   * 
   * @return A List of Followers, but ID
   */
  public static List<Long> getUserList(long userID, int followerCount) {
    CloseableHttpClient   client   = null;
    CloseableHttpResponse response = null;

    //Cap the loop at running a number of times sufficient to get all followers for this user
    int fLoop = (followerCount / MAX_FOLLOWER_COUNT) + ((followerCount % MAX_FOLLOWER_COUNT) == 0 ? 0 : 1);
    //System.out.println ("fLoop Value = " + fLoop);
    
    List<Long> allUsers   = new LinkedList<Long>();
    String responseString = null;
    
    try {
      OAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
      consumer.setTokenWithSecret(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
      
      client = HttpClients.createDefault();

      long cursor = -1;
      for (int loop = 0; (loop < fLoop) && (cursor != 0); loop++) {
        String twitterURL = "https://api.twitter.com/1.1/followers/ids.json?cursor=" + cursor + "&user_id=" + userID + 
            "&count=" + MAX_FOLLOWER_COUNT + "&stringify_ids=true";
        HttpGet request = new HttpGet(twitterURL);
        consumer.sign(request);
        response = client.execute(request);
        
        HttpEntity entity = response.getEntity();
        responseString    = EntityUtils.toString(entity);
        
        //DEBUG
        //System.out.println (responseString);
        
        JSONObject allFollowers = new JSONObject(responseString);
        
        if (allFollowers.has("errors")) {
          System.out.println (responseString);
          System.out.println ("I hit my follower request limit and need to sleep for about 15 minutes here...");
          loop--;
          System.out.println ("  I will resume by " + formatter.format(new Date(System.currentTimeMillis() + 930000)));
          try { Thread.sleep(910000); } catch (Throwable t) {}
          System.out.println ("Resuming operation");
        } else if (allFollowers.has("error")) {
          System.out.println (responseString);
          System.out.println ("I do not have persmission to get this data...");
          return allUsers;
        } else {
          JSONArray followers = allFollowers.getJSONArray("ids");
          for (int i = 0; i < followers.length(); i++) {
            //System.out.println ("Follower ID: " + followers.getLong(i));
            allUsers.add(followers.getLong(i));
          }
          //System.out.println ("Added " + followers.length() + " followers to list.");
          cursor = allFollowers.getLong("next_cursor");
          
          //System.out.println ("New Cursor:" + cursor);
        }
        
        response.close();
      }
      
      client.close();
      
      //System.out.println ("Total Followers Expected: " + followerCount);
      //System.out.println ("Total Followers Found:    " + allUsers.size());

      return allUsers;
    } catch (Throwable t) {
      System.err.println("Something bad happened parsing follower list for " + userID + "!");
      System.err.println (responseString);
      t.printStackTrace();
      return null;
    }
  }
  
  /**
   * Helper method for getting all tweets for a user.
   * 
   * @param userName The userName we want to get the history for
   * @param tweetCount The number of tweets associated to the user
   * @param writer The PrintWriter required for the raw response dump
   * 
   * @return A List of Twitter Statuses, ideally up to min(tweetCount, 3200)
   */
  public static List<TwitterStatus> getUserTweets(String userName, int tweetCount, PrintWriter writer) {
    utcFormatter.setLenient(true);
    
    CloseableHttpClient   client   = null;
    CloseableHttpResponse response = null;
    
    int loopLimit = Math.min(tweetCount, TWEET_LIMIT);
    int tweets    = 0;
    long cursor   = 0;

    List<TwitterStatus> statuses = new LinkedList<TwitterStatus>();
    String responseString        = null;
    
    try {
      OAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
      consumer.setTokenWithSecret(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
      
      client = HttpClients.createDefault();

      while (tweets < loopLimit) {
        //DEBUG
        //System.out.println ("Tweets: " + tweets);
        
        String twitterURL = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=" + userName + 
            "&count=" + MAX_TWEET_COUNT + "&exclude_replies=true";
        if (tweets > 0) twitterURL += "&max_id=" + cursor;
        
        HttpGet request = new HttpGet(twitterURL);
        consumer.sign(request);
        response = client.execute(request);
        
        HttpEntity entity = response.getEntity();
        responseString = EntityUtils.toString(entity);
        
        //DEBUG
        //System.out.println (responseString);
        
        try {
          JSONObject errors = new JSONObject(responseString);
          if (errors.has("errors")) {
            System.out.println (responseString);
            System.out.println ("I hit my tweet request limit and need to sleep for about 15 minutes here...");
            System.out.println ("  I will resume by " + formatter.format(new Date(System.currentTimeMillis() + 930000)));
            try { Thread.sleep(910000); } catch (Throwable t) {}
            System.out.println ("Resuming operation");
            continue;
          }
          if (errors.has("error")) {
            System.out.println (responseString);
            System.out.println ("I do not have persmission to get this data...");
            return statuses;
          }
        } catch (Throwable t) { /** Ignore Me */ }
        
        JSONArray allTweets = new JSONArray(responseString);
        
        //System.out.println ("Processing " + allTweets.length() + " tweets...");
        
        //DEBUG
        //if (allTweets.length() <= 1)
        //  System.out.println ("Short Tweet: " + allTweets);
        
        //Lets start parsing through tweets
        for (int i = 0; i < allTweets.length(); i++) {
          tweets++;
          JSONObject tweet     = allTweets.getJSONObject(i);
          TwitterStatus status = new TwitterStatus();
          
          //DEBUG
          //System.out.println ("The tweet: " + tweet);
          //System.out.println ("Processing Tweet " + (i + 1));
          
          //For the massive dump
          writer.println (tweet);
          
          status.setStatusID(tweet.getLong("id"));
          status.setRetweetCount(tweet.getInt("retweet_count"));
          status.setFavoriteCount(tweet.getInt("favorite_count"));
          
          status.setCreatedDate(utcFormatter.parse(tweet.getString("created_at")));
          
          //Get the message text
          String message = tweet.getString("text");
          
          //Track the min tweet ID from this batch to be the cap for the next batch
          if (cursor == 0) 
            cursor = tweet.getLong("id");
          else {
            if (cursor > tweet.getLong("id"))
              cursor = tweet.getLong("id");
            else if ((allTweets.length() == 1) && (cursor == tweet.getLong("id"))) {
              //System.out.println ("I should probably bail here, I think I'm stuck...");
              loopLimit = 0;
              break;
            }
          }
          
          //We need to sanitize the String values, scrub out all non-alphanumeric stuff
          //We strip out URLs first, since the break all our other parsing techniques
          String scrubbedMessage = message;
          try {
            String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
            Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(scrubbedMessage);
            while (m.find()) {
              scrubbedMessage = scrubbedMessage.replaceAll(m.group()," ").trim();
            }
          } catch (Throwable t) {
            //We're going to try a more stripped down version that prevents the uncloses paren problem
            try {
              String urlPattern2 = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$~_?\\+-=\\\\\\.&]*)";
              Pattern p2 = Pattern.compile(urlPattern2,Pattern.CASE_INSENSITIVE);
              Matcher m2 = p2.matcher(scrubbedMessage);
              while (m2.find()) {
                scrubbedMessage = scrubbedMessage.replaceAll(m2.group()," ").trim();
              }
            } catch (Throwable t2) {
              System.err.println ("There were problems parsing the regex content.");
              System.err.println ("The offending tweet: " + message);
            }
          }
          
          //Remove the ampersand symbol
          scrubbedMessage = scrubbedMessage.replaceAll("&amp;", "and");
          //Remove all special characters besides ' @ # (So we retain user and hashtag hits, as well as punctuation)
          scrubbedMessage = scrubbedMessage.replaceAll("[^A-Za-z0-9'@# ]", " ");
          //Since we've been padding with whitespace, cut the spacing down to single space
          scrubbedMessage = scrubbedMessage.trim().replaceAll(" +", " ");
          
          status.setFilteredText(scrubbedMessage);
          
          //Get the list of HashTags used in this tweet
          JSONObject entities = tweet.getJSONObject("entities");
          JSONArray hashTags = entities.getJSONArray("hashtags");
        
          if (hashTags.length() > 0) {
            for (int j = 0; j < hashTags.length(); j++)
              status.addHashTag(hashTags.getJSONObject(j).getString("text"));
          }
          
          JSONObject user = tweet.getJSONObject("user");
          status.setUserID(user.getLong("id"));
          status.setUserName(user.getString("screen_name"));
          
          if (!tweet.isNull("coordinates")) {
            JSONArray coordinates = tweet.getJSONObject("coordinates").getJSONArray("coordinates");
            status.setGeoLat(coordinates.getDouble(0));
            status.setGeoLon(coordinates.getDouble(1));
          }
          
          //System.out.println (status.jsonify());
          
          statuses.add(status);
        }//end for all tweets in this batch
        
        response.close();
        
      }//end for all batches
    
      client.close();
      
      //System.out.println ("The total tweets found: " + statuses.size());
      
      return statuses;
    } catch (Throwable t) {
      System.err.println("Something bad happened parsing tweets for " + userName + "!");
      System.err.println(responseString);
      t.printStackTrace();
      return null;
    }
  }
}
