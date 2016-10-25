package com.ac.umkc.twitter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author AC010168
 * 
 */
public class TwitterStatus {
  
  /** Date formatter for converting Date object to text for json output */
  private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");
  
  /** The unique ID for this tweet */
  private long statusID;
  /** The userID who wrote this tweet */
  private long userID;
  /** The userName who wrote this tweet */
  private String userName;
  /** The number of times this tweet was retweeted */
  private int retweetCount;
  /** The number of times this tweet was liked */
  private int favoriteCount;
  /** The filtered text from this tweet */
  private String filteredText;
  /** The list of HashTags used in this tweet.  May be empty */
  private List<String> hashTags;
  /** The date this tweet was tweeted */
  private Date createdDate;
  /** Geo-coordinates for where this tweet happened */
  private double geoLat;
  /** Geo-coordinates for where this tweet happened */
  private double geoLon;
  
  /**
   * Basic Constructor
   */
  public TwitterStatus() {
    statusID      = -1;
    userID        = -1;
    userName      = null;
    retweetCount  = -1;
    favoriteCount = -1;
    filteredText  = null;
    hashTags      = new ArrayList<String>();
    createdDate   = null;
    geoLat        = 0.0;
    geoLon        = 0.0;
  }

  /**
   * Helper method to add hashTags to the list
   * @param hashTag the hashtag to add
   */
  public void addHashTag(String hashTag) {
    hashTags.add(hashTag);
  }
  
  /**
   * Helper method to convert object to json string
   * @return A JSON-formatted string representing this object
   */
  public String jsonify() {
    String json = "{\"statusID\":" + statusID + ",\"userID\":" + userID + 
        ",\"userName\":\"" + userName + "\",\"retweetCount\":" + retweetCount + 
        ",\"favoriteCount\":" + favoriteCount + ",\"filteredText\":\"" + filteredText + 
        "\",\"geoLat\":" + geoLat + ",\"geoLon\":" + geoLon + ",\"createdDate\":\"" + 
        formatter.format(createdDate) + "\",\"hashTags\":[";
    
    for (int i = 0; i < hashTags.size(); i++) {
      json += "\"" + hashTags.get(i) + "\"";
      if (i < (hashTags.size() - 1))
        json += ",";
    }
    json += "]}";
    
    return json;
  }
  
  /**
   * @return the statusID
   */
  public long getStatusID() {
    return statusID;
  }

  /**
   * @param statusID the statusID to set
   */
  public void setStatusID(long statusID) {
    this.statusID = statusID;
  }

  /**
   * @return the userID
   */
  public long getUserID() {
    return userID;
  }

  /**
   * @param userID the userID to set
   */
  public void setUserID(long userID) {
    this.userID = userID;
  }

  /**
   * @return the userName
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param userName the userName to set
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * @return the retweetCount
   */
  public int getRetweetCount() {
    return retweetCount;
  }

  /**
   * @param retweetCount the retweetCount to set
   */
  public void setRetweetCount(int retweetCount) {
    this.retweetCount = retweetCount;
  }

  /**
   * @return the favoriteCount
   */
  public int getFavoriteCount() {
    return favoriteCount;
  }

  /**
   * @param favoriteCount the favoriteCount to set
   */
  public void setFavoriteCount(int favoriteCount) {
    this.favoriteCount = favoriteCount;
  }

  /**
   * @return the filteredText
   */
  public String getFilteredText() {
    return filteredText;
  }

  /**
   * @param filteredText the filteredText to set
   */
  public void setFilteredText(String filteredText) {
    this.filteredText = filteredText;
  }

  /**
   * @return the hashTags
   */
  public List<String> getHashTags() {
    return hashTags;
  }

  /**
   * @param hashTags the hashTags to set
   */
  public void setHashTags(List<String> hashTags) {
    this.hashTags = hashTags;
  }

  /**
   * @return the createdDate
   */
  public Date getCreatedDate() {
    return createdDate;
  }

  /**
   * @param createdDate the createdDate to set
   */
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  /**
   * @return the geoLat
   */
  public double getGeoLat() {
    return geoLat;
  }

  /**
   * @param geoLat the geoLat to set
   */
  public void setGeoLat(double geoLat) {
    this.geoLat = geoLat;
  }

  /**
   * @return the geoLon
   */
  public double getGeoLon() {
    return geoLon;
  }

  /**
   * @param geoLon the geoLon to set
   */
  public void setGeoLon(double geoLon) {
    this.geoLon = geoLon;
  }
}
