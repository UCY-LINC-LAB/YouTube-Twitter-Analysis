package com.zgeorg03.database;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by zgeorg03 on 2/25/17.
 */
public interface DBServicesI {


    /**
     * This function will give the number of videos that are in monitored state and not finished
     * @return
     */
    int getTotalMonitoredVideosAndNotFinished();

    /**
     * Set video as finished
     * @return
     */
    boolean setVideoAsFinished(String video_id);

    /**
     *
     * This function will give the number of videos that  have been finished
     * @return
     */
    int getTotalFinishedVideos();

    /**
     *
     * Get a list of videos that need a dynamic update.
     * The duration is the period that has to pass in order to set a video for dynamic update
     * @return List of id's
     */
    List<String> getVideosThatNeedDynamicUpdate(long duration, TimeUnit unit);


    /**
     *
     * Get a list of videos that have comments to be collected, not over maxComments
     *
     *
     * @param maxComments
     * @return List of id's
     */
    Map<String,Integer> getVideosThatNeedComments(int maxComments);

    /**
     * Check if this video is in the database in monitor state.
     *
     * @param video_id
     * @return
     */
    boolean checkVideoExistenceAndBeingMonitored(String video_id);

    /**
     * Check if this video is in the database
     *
     * @param video_id
     * @return
     */
    boolean checkVideoExistenceOnly(String video_id);


    /**
     * Return true if 15 days have passed
     * @param video_id
     * @return
     */
    boolean checkVideoIsFinished(String video_id);
    /**
     * Add a new video record in the database.
     * We need to record  the timestamp of this transaction, since all the calculations are based on creation time
     * The information contained in this object should be all the static data of the video.
     * @param videoObject
     * @return
     */
    boolean addNewVideo(String video_id, JsonObject videoObject);

    /**
     * Add dynamic data
     * @param video_id
     * @param dynamicData
     * @return
     */
    boolean addDynamicData(String video_id,JsonObject dynamicData);

    /**
     * If the video id is found, it deletes it from videos collection
     * @param video_id
     * @return
     */
    boolean deleteVideo(String video_id);

    /**
     * Add video comments
     * @param comments
     * @return
     */
    int addComments(String video_id,JsonObject comments);

    /**
     * Add Tweet
     * @param video_id The id of the video
     * @param tweet The tweet object
     * @return
     */
    boolean addTweet(long video_id,JsonObject tweet);


    /**
     * Add a new YouTubeAPIKey.
     * @return
     */
    boolean addYouTubeAPIKey(String key);

    /**
     * Get the key that has the least cost.
     * @return
     */
    String getYouTubeAPIKey();

    /**
     * Get total number of youtube API keys in the database
     * @return
     */
    int getTotalYouTubeAPIKeys();

    /**
     * Get a twitter app and set it to being used. It contains the 4 keys
     * @return
     */
    JsonObject getTwitterAppForUse();

    /**
     * Release a twitter app that is currently in use
     * @return
     */
    boolean releaseTwitterApp(String name);

    /**
     * Get the total number of twitter applications
     * @return
     */
    int getTotalTwitterApps();

    /**
     * Get the total number of twitter applications that are free
     * @return
     */
    int getTotalFreeTwitterApps();

    /**
     * Add a new twitter application.
     * We need to specify if it is used
     * @return
     */
    boolean addTwitterApp(String name, String consumer_key,String consumer_secret,String token,String token_secret);



    JsonObject getStatistics();
    /**
     * Configuration
     */

    JsonObject getConfiguration();


    /**
     * Set the max number of videos being monitored
     * @param max
     * @return
     */
    boolean setMaxVideosBeingMonitored(int max);

    /**
     * Get max number of videos to monitor
     * @return
     */
    int getMaxVideosBeingMonitored();


    int getMaxCommentsPerVideo();
    boolean setMaxCommentsPerVideo(int max);

    /**
     * Get the channel id of the video
     * @param videoId
     * @return
     */
    String getChannelId(String videoId);


    /**
     * Set statistics of status monitoring
     */
    void setStatusMonitorStats();

    /**
     * Get total number of tweets in the database
     * @return
     */
    int getTotalTweets();


    boolean setVideoAsIncomplete(String videoId);

}
