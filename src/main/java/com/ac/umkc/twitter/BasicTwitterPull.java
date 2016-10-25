package com.ac.umkc.twitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

  /** Storage point for file path */
  private String inputFolder;
  /** Storage point for file path */
  private String outputFolder;
  
  /** Simple Date Formatter for output on wait times */
  private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

  /**
   * Basic Constructor.
   * @param inputPath The file path provided at the command prompt.
   * @param outputPath The file path provided at the command prompt.
   */
  public BasicTwitterPull(String inputPath, String outputPath) {
    inputFolder  = inputPath;
    outputFolder = outputPath;
  }
  
  /**
   * @param args list of command line arguments
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println ("You did not specify the input and output folder paths");
      return;
    }
    
    //create our output files, purging previous ones if possible.
    BasicTwitterPull pull = new BasicTwitterPull(args[0], args[1]);
    pull.execute();
  }
    
  public void execute() {   
    File rootInputFolder  = new File(inputFolder);
    if (!rootInputFolder.exists()) {
      System.err.println ("The provided input folder path does not exist");
      return;
    }
    if (!rootInputFolder.isDirectory()) {
      System.err.println ("The provided input folder path is not a directory");
      return;
    }
    File designersFile  = new File(rootInputFolder, "designers.txt");
    File publishersFile = new File(rootInputFolder, "publishers.txt");
    File reviewersFile  = new File(rootInputFolder, "reviewers.txt");
    File keyEventsFile  = new File(rootInputFolder, "keyevents.txt");
    File usersFile      = new File(rootInputFolder, "followersJson.txt");
    
    File rootOutputFolder  = new File(outputFolder);
    if (!rootOutputFolder.exists()) {
      System.err.println ("The provided output folder path does not exist");
      return;
    }
    if (!rootOutputFolder.isDirectory()) {
      System.err.println ("The provided output folder path is not a directory");
      return;
    }
    File userFile        = new File(rootOutputFolder, "userOutput.txt");
    File userRawFile     = new File(rootOutputFolder, "userRawOutput.txt");
    File statusesFile    = new File(rootOutputFolder, "statusesOutput.txt");
    File statusesRawFile = new File(rootOutputFolder, "statusesRawOutput.txt");

    if (userFile.exists())        userFile.delete();
    if (userRawFile.exists())     userRawFile.delete();
    if (statusesFile.exists())    statusesFile.delete();
    if (statusesRawFile.exists()) statusesRawFile.delete();

    List<String> designers              = new LinkedList<String>();
    List<String> publishers             = new LinkedList<String>();
    List<String> reviewers              = new LinkedList<String>();
    List<String> keyEvents              = new LinkedList<String>();
    Map<Integer, List<Long>> inputUsers = new HashMap<Integer, List<Long>>(); 

    //Get the users we need to upload from the input files
    try {
      BufferedReader designersReader  = new BufferedReader(new FileReader(designersFile));
      BufferedReader publishersReader = new BufferedReader(new FileReader(publishersFile));
      BufferedReader reviewersReader  = new BufferedReader(new FileReader(reviewersFile));
      BufferedReader keyEventsReader  = new BufferedReader(new FileReader(keyEventsFile));
      BufferedReader usersReader      = new BufferedReader(new FileReader(usersFile));
      
      String line = "";
      
      //Read the designers
      line = designersReader.readLine();
      while (line != null) {
        designers.add(line);
        line = designersReader.readLine();
      }
      designersReader.close();
      
      //Read the publishers
      line = publishersReader.readLine();
      while (line != null) {
        publishers.add(line);
        line = publishersReader.readLine();
      }
      publishersReader.close();
      
      //Read the reviewers
      line = reviewersReader.readLine();
      while (line != null) {
        reviewers.add(line);
        line = reviewersReader.readLine();
      }
      reviewersReader.close();
      
      //Read the keyEvents
      line = keyEventsReader.readLine();
      while (line != null) {
        keyEvents.add(line);
        line = keyEventsReader.readLine();
      }
      keyEventsReader.close();
      
      //Read the users
      int userCount = 0;
      line = usersReader.readLine();
      while (line != null) {
        JSONObject jsonLine = new JSONObject(line);
        int value = jsonLine.getInt("followCount");

        JSONArray userArray = jsonLine.getJSONArray("users");
        List<Long> tempUsers = new ArrayList<Long>(userArray.length());
        userCount += userArray.length();
        
        for (int i = 0; i < userArray.length(); i++)
          tempUsers.add(userArray.getLong(i));
        
        inputUsers.put(value, tempUsers);
        
        line = usersReader.readLine();
      }
      usersReader.close();

      System.out.println ("Number of designers to find:     " + designers.size());
      System.out.println ("Number of publishers to find:    " + publishers.size());
      System.out.println ("Number of reviewers to find:     " + reviewers.size());
      System.out.println ("Number of keyEvents to find:     " + keyEvents.size());
      System.out.println ("Number of non-key users to find: " + userCount);
    } catch (Throwable t) {
      System.out.println ("There were problems reading our users lists");
      t.printStackTrace();
      return;
    }
    
    PrintWriter userWriter        = null;
    PrintWriter userRawWriter     = null;
    PrintWriter statusesWriter    = null;
    PrintWriter statusesRawWriter = null;
    
    int totalTweets = 0;
    
    //Map<Long, Integer> followerMap = new HashMap<Long, Integer>();
    //List<Long> knownUserIDs        = new LinkedList<Long>();
    
    try {
      userWriter        = new PrintWriter(new BufferedWriter(new FileWriter(userFile))); 
      userRawWriter     = new PrintWriter(new BufferedWriter(new FileWriter(userRawFile))); 
      statusesWriter    = new PrintWriter(new BufferedWriter(new FileWriter(statusesFile)));
      statusesRawWriter = new PrintWriter(new BufferedWriter(new FileWriter(statusesRawFile)));
      
      //Process the list of designers
      for (String designer : designers) {
        System.out.println ("[" + formatter.format(new Date()) + "] About to process data for Designer " + designer);
        
        TwitterUser user             = TwitterCall.getTwitterUser(designer, UserType.DESIGNER, userRawWriter);
        List<TwitterStatus> statuses = TwitterCall.getUserTweets(user.getScreenName(), user.getStatusesCount(), statusesRawWriter);
        //List<Long> followers         = TwitterCall.getUserList(user.getTwitterID(), user.getFollowersCount());
        
        //knownUserIDs.add(user.getTwitterID());
        totalTweets += statuses.size();
        System.out.println ("Total Tweet Count: " + statuses.size() + "  (" + totalTweets + ")");
        
        userWriter.println (user.jsonify());
        for (TwitterStatus status : statuses)
          statusesWriter.println (status.jsonify());
        
        /**********************************************
        for (Long follower : followers) {
          followersWriter.println (follower);
          if (followerMap.containsKey(follower)) {
            Integer keyValue = followerMap.get(follower);
            keyValue++;
            followerMap.put(follower, keyValue);
          } else {
            followerMap.put(follower, new Integer(1));
          }
        }
        /**********************************************/
        
        userWriter.flush();
        userRawWriter.flush();
        statusesWriter.flush();
        statusesRawWriter.flush();
      }
      
      //Process the list of publishers
      for (String publisher : publishers) {
        System.out.println ("[" + formatter.format(new Date()) + "] About to process data for Publisher " + publisher);
        
        TwitterUser user             = TwitterCall.getTwitterUser(publisher, UserType.PUBLISHER, userRawWriter);
        List<TwitterStatus> statuses = TwitterCall.getUserTweets(user.getScreenName(), user.getStatusesCount(), statusesRawWriter);
        //List<Long> followers         = TwitterCall.getUserList(user.getTwitterID(), user.getFollowersCount());
        
        //knownUserIDs.add(user.getTwitterID());
        totalTweets += statuses.size();
        System.out.println ("Total Tweet Count: " + statuses.size() + "  (" + totalTweets + ")");
        
        userWriter.println (user.jsonify());
        for (TwitterStatus status : statuses)
          statusesWriter.println (status.jsonify());
        
        /**********************************************
        for (Long follower : followers) {
          followersWriter.println (follower);
          if (followerMap.containsKey(follower)) {
            Integer keyValue = followerMap.get(follower);
            keyValue++;
            followerMap.put(follower, keyValue);
          } else {
            followerMap.put(follower, new Integer(1));
          }
        }
        /**********************************************/
        
        userWriter.flush();
        userRawWriter.flush();
        statusesWriter.flush();
        statusesRawWriter.flush();
      }
      
      //Process the list of reviewers
      for (String reviewer : reviewers) {
        System.out.println ("[" + formatter.format(new Date()) + "] About to process data for Reviewer " + reviewer);
        
        TwitterUser user             = TwitterCall.getTwitterUser(reviewer, UserType.REVIEWER, userRawWriter);
        List<TwitterStatus> statuses = TwitterCall.getUserTweets(user.getScreenName(), user.getStatusesCount(), statusesRawWriter);
        //List<Long> followers         = TwitterCall.getUserList(user.getTwitterID(), user.getFollowersCount());
        
        //knownUserIDs.add(user.getTwitterID());
        totalTweets += statuses.size();
        System.out.println ("Total Tweet Count: " + statuses.size() + "  (" + totalTweets + ")");
        
        userWriter.println (user.jsonify());
        for (TwitterStatus status : statuses)
          statusesWriter.println (status.jsonify());
        
        /**********************************************
        for (Long follower : followers) {
          followersWriter.println (follower);
          if (followerMap.containsKey(follower)) {
            Integer keyValue = followerMap.get(follower);
            keyValue++;
            followerMap.put(follower, keyValue);
          } else {
            followerMap.put(follower, new Integer(1));
          }
        }
        /**********************************************/
        
        userWriter.flush();
        userRawWriter.flush();
        statusesWriter.flush();
        statusesRawWriter.flush();
      }
      
      //Process the list of keyEvents
      for (String keyEvent : keyEvents) {
        System.out.println ("[" + formatter.format(new Date()) + "] About to process data for Key Event " + keyEvent);
        
        TwitterUser user             = TwitterCall.getTwitterUser(keyEvent, UserType.CONVENTION, userRawWriter);
        List<TwitterStatus> statuses = TwitterCall.getUserTweets(user.getScreenName(), user.getStatusesCount(), statusesRawWriter);
        //List<Long> followers         = TwitterCall.getUserList(user.getTwitterID(), user.getFollowersCount());
        
        //knownUserIDs.add(user.getTwitterID());
        totalTweets += statuses.size();
        System.out.println ("Total Tweet Count: " + statuses.size() + "  (" + totalTweets + ")");
        
        userWriter.println (user.jsonify());
        for (TwitterStatus status : statuses)
          statusesWriter.println (status.jsonify());
        
        /**********************************************
        for (Long follower : followers) {
          followersWriter.println (follower);
          if (followerMap.containsKey(follower)) {
            Integer keyValue = followerMap.get(follower);
            keyValue++;
            followerMap.put(follower, keyValue);
          } else {
            followerMap.put(follower, new Integer(1));
          }
        }
        /**********************************************/
        
        userWriter.flush();
        userRawWriter.flush();
        statusesWriter.flush();
        statusesRawWriter.flush();
      }
      
      //Now we need to re-order the list of users, making sure to not include users
      //we have already added to one of our other groups
      /**********************************************
      System.out.println ("[" + formatter.format(new Date()) + "] Sorting followers list.");
      Map<Integer, List<Long>> revampUserList = new HashMap<Integer, List<Long>>();
      for (Long curUserID : followerMap.keySet()) {
        if (!knownUserIDs.contains(curUserID)) {
          Integer value = followerMap.get(curUserID);
          if (revampUserList.containsKey(value)) {
            List<Long> tempFollowers = revampUserList.get(value);
            tempFollowers.add(curUserID);
            Collections.sort(tempFollowers);
            revampUserList.put(value, tempFollowers);
          } else {
            List<Long> tempFollowers = new LinkedList<Long>();
            tempFollowers.add(curUserID);
            revampUserList.put(value, tempFollowers);
          }
        }
      }
      
      List<Integer> sortValues = new ArrayList<Integer>(revampUserList.size());
      sortValues.addAll(revampUserList.keySet());
      Collections.sort(sortValues);
      
      //We're looking to write out some data on this for me
      for (Integer value : sortValues) {
        List<Long> userIDs = revampUserList.get(value);
        String outputLine = "{\"followCount\":" + value + ",\"users\":[";
        for (int loop = 0; loop < userIDs.size(); loop++) {
          outputLine += userIDs.get(loop);
          if (loop < (userIDs.size() -1 ))
            outputLine += ",";
        }
        outputLine += "]}";
        
        followersWriter.println (outputLine);
      }
      followersWriter.flush();
      followersWriter.close();
      /**********************************************/
      
      List<Integer> sortValues = new ArrayList<Integer>(inputUsers.size());
      sortValues.addAll(inputUsers.keySet());
      Collections.sort(sortValues);
      
      //now we want to record all users, and process tweets
      for (Integer value : sortValues) {
        System.out.println ("[" + formatter.format(new Date()) + "] Processing users who follow " + value + " other key users.");
        if (value < 10) {
          System.out.println ("We are ignoring users who follow less than 10 key entities");
          continue;
        }
        
        List<Long> userIDs = inputUsers.get(value);
        System.out.println ("  There are " + userIDs.size() + " users in this category...");
        
        for (Long curID : userIDs) {
          TwitterUser user = TwitterCall.getTwitterUser(curID, UserType.COMMUNITY, userRawWriter);
          userWriter.println (user.jsonify());
          
          if (value >= 20) {
            List<TwitterStatus> statuses = TwitterCall.getUserTweets(user.getScreenName(), user.getStatusesCount(), statusesRawWriter);
            totalTweets += statuses.size();
            System.out.println ("Total Tweet Count: " + statuses.size() + "  (" + totalTweets + ")");

            for (TwitterStatus status : statuses)
              statusesWriter.println (status.jsonify());
          }
        }
        
        userWriter.flush();
        userRawWriter.flush();
        statusesWriter.flush();
        statusesRawWriter.flush();
      }
      
      userWriter.close();
      userRawWriter.close();
      statusesWriter.close();
      statusesRawWriter.close();
      
    } catch (Throwable t) {
      System.out.println ("I had problems: " + t.getMessage());
      t.printStackTrace();
    }
    
    System.out.println ("Program Execution Complete.");
    System.out.println ("There were " + totalTweets + " tweets recorded.");
    System.out.println ("The following files were generated: ");
    System.out.println ("  " + userFile.getAbsolutePath());
    System.out.println ("  " + userRawFile.getAbsolutePath());
    System.out.println ("  " + statusesFile.getAbsolutePath());
    System.out.println ("  " + statusesRawFile.getAbsolutePath());
  }
}
