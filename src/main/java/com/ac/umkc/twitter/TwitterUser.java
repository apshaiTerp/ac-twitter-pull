package com.ac.umkc.twitter;

import java.io.Serializable;

/**
 * @author AC010168
 *
 */
public class TwitterUser implements Serializable {

  /** Required ID for serialization */
  private static final long serialVersionUID = 8542981525843929901L;
  
  /** The ID for this twitter user */
  private long twitterID;
  /** The screenName for this twitter user */
  private String userName;
  /** The screenName for this twitter user */
  private String screenName;
  /** The number of followers this user has */
  private int followersCount;
  /** The number of friends (people they follow) this user has */
  private int friendsCount;
  /** The number of statuses (tweets) this user has posted */
  private int statusesCount;
  /** Custom value assigned to track user types */
  private UserType userType;
  /** The location this user is found at, if available */
  private String location;
  
  /**  */
  private String googleLocation;
  /**  */
  private double geoLat;
  /**  */
  private double geoLon;
  
  /**
   * Basic Constructor
   */
  public TwitterUser() {
    twitterID      = -1;
    userName       = null;
    screenName     = null;
    followersCount = -1;
    friendsCount   = -1;
    statusesCount  = -1;
    userType       = null;
    location       = null;
    googleLocation = null;
    geoLat         = 0.0;
    geoLon         = 0.0;
  }
  
  /**
   * Helper method to convert object to json string
   * @return A JSON-formatted string representing this object
   */
  public String jsonify() {
    return "{\"twitterID\":" + twitterID + ",\"userName\":\"" + userName + "\",\"screenName\":\"" + 
        screenName + "\",\"followersCount\":" + followersCount + ",\"friendsCount\":" + friendsCount + 
        ",\"statusesCount\":" + statusesCount + ",\"userType\":\"" + userType + "\",\"location\":\"" + 
        location + "\",\"googleLocation\":\"" + googleLocation + "\",\"geoLat\":" + geoLat + 
        ",\"geoLon\":" + geoLon + "}";
  }
  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return jsonify();
  }

  /**
   * @return the twitterID
   */
  public long getTwitterID() {
    return twitterID;
  }

  /**
   * @param twitterID the twitterID to set
   */
  public void setTwitterID(long twitterID) {
    this.twitterID = twitterID;
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
    if (userName == null)
      this.userName = null;
    else {
      this.userName = userName.trim();
      this.userName = this.userName.replace("\\", "\\\\");
      this.userName = this.userName.replace("\"", "\\\"");
      this.userName = this.userName.replace("/", "\\/");
    }
  }

  /**
   * @return the screenName
   */
  public String getScreenName() {
    return screenName;
  }

  /**
   * @param screenName the screenName to set
   */
  public void setScreenName(String screenName) {
    if (screenName == null)
      this.screenName = null;
    else {
      this.screenName = screenName.trim();
      this.screenName = this.screenName.replace("\\", "\\\\");
      this.screenName = this.screenName.replace("\"", "\\\"");
      this.screenName = this.screenName.replace("/", "\\/");
    }
  }

  /**
   * @return the followersCount
   */
  public int getFollowersCount() {
    return followersCount;
  }

  /**
   * @param followersCount the followersCount to set
   */
  public void setFollowersCount(int followersCount) {
    this.followersCount = followersCount;
  }

  /**
   * @return the friendsCount
   */
  public int getFriendsCount() {
    return friendsCount;
  }

  /**
   * @param friendsCount the friendsCount to set
   */
  public void setFriendsCount(int friendsCount) {
    this.friendsCount = friendsCount;
  }

  /**
   * @return the statusesCount
   */
  public int getStatusesCount() {
    return statusesCount;
  }

  /**
   * @param statusesCount the statusesCount to set
   */
  public void setStatusesCount(int statusesCount) {
    this.statusesCount = statusesCount;
  }

  /**
   * @return the userType
   */
  public UserType getUserType() {
    return userType;
  }

  /**
   * @param userType the userType to set
   */
  public void setUserType(UserType userType) {
    this.userType = userType;
  }

  /**
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(String location) {
    if (location == null)
      this.location = null;
    else {
      this.location = location.trim();
      this.location = this.location.replace("\\", "\\\\");
      this.location = this.location.replace("\"", "\\\"");
      this.location = this.location.replace("/", "\\/");
    }
  }

  /**
   * @return the googleLocation
   */
  public String getGoogleLocation() {
    return googleLocation;
  }

  /**
   * @param googleLocation the googleLocation to set
   */
  public void setGoogleLocation(String googleLocation) {
    this.googleLocation = googleLocation;
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
