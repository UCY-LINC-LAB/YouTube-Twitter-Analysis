package com.zgeorg03.video;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zgeorg03.utils.DateUtil;
import com.zgeorg03.utils.JsonModel;
import org.bson.Document;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zgeorg03 on 3/2/17.
 */
public class Video implements JsonModel{

    //Static
    private final String video_id;
    private final String title;
    private final String description;
    private final int category;
    private final int artificial_category;
    private final long published_at;
    private final long collected_at;
    private final long duration;

    private final List<Day> days;

    //Count these
    private final  long total_views;
    private final  long total_likes;
    private final  long total_dislikes;
    private final  long total_comments;
    private final  long total_tweets;
    private final  long total_original_tweets;
    private final  long total_retweets;
    private final  long total_channel_views;
    private final  long total_channel_comments;
    private final  long total_channel_subscribers;
    private final  long total_channel_videos;


    public static class Builder {

        public Video create(Document document){
            String video_id = document.getString("_id");
            String title = document.getString("title");
            String description = document.getString("description");
            int category = document.getInteger("category");
            int artificial_category = document.getInteger("artificial_category");
            long published_at = document.getLong("published_at_timestamp");
            long collected_at = document.getLong("collected_at_timestamp");
            long duration = document.getLong("duration");
            long total_views = document.getLong("total_views");
            long total_likes = document.getLong("total_likes");
            long total_dislikes = document.getLong("total_dislikes");
            long total_comments = document.getLong("total_comments");
            long total_tweets = document.getLong("total_tweets");
            long total_original_tweets = document.getLong("total_original_tweets");
            long total_retweets = document.getLong("total_retweets");
            long total_channel_views = document.getLong("total_channel_views");
            long total_channel_comments = document.getLong("total_channel_comments");
            long total_channel_subscribers = document.getLong("total_channel_subscribers");
            long total_channel_videos = document.getLong("total_channel_videos");
            List<Document> documents = (List<Document>) document.get("days");
            List<Day> days = new LinkedList<>();

            for(Document doc : documents) {
                days.add(Day.Builder.create(doc));
            }
            return  new Video(video_id, title, description, category, artificial_category, published_at, collected_at, duration,
                    total_views, total_likes, total_dislikes, total_comments, total_tweets, total_original_tweets,
                    total_retweets, total_channel_views, total_channel_comments, total_channel_subscribers, total_channel_videos,days);
        }
    }
    public Video(String video_id, String title, String description, int category, int artificial_category, long published_at, long collected_at, long duration,
                 long total_views, long total_likes, long total_dislikes, long total_comments, long total_tweets, long total_original_tweets,
                 long total_retweets, long total_channel_views, long total_channel_comments, long total_channel_subscribers, long total_channel_videos,List<Day> days){

        this.video_id = video_id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.artificial_category = artificial_category;
        this.published_at = published_at;
        this.collected_at = collected_at;
        this.duration = duration;
        this.total_views = total_views;
        this.total_likes = total_likes;
        this.total_dislikes = total_dislikes;
        this.total_comments = total_comments;
        this.total_tweets = total_tweets;
        this.total_original_tweets = total_original_tweets;
        this.total_retweets = total_retweets;
        this.total_channel_views = total_channel_views;
        this.total_channel_comments = total_channel_comments;
        this.total_channel_subscribers = total_channel_subscribers;
        this.total_channel_videos = total_channel_videos;
        this.days = days;
    }


    @Override
    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.addProperty("video_id",video_id);
        result.addProperty("title",title);
        result.addProperty("description", description);
        result.addProperty("category",category);
        result.addProperty("artificial_category",artificial_category);
        result.addProperty("published_at",DateUtil.toDate(published_at));
        result.addProperty("published_at_timestamp",published_at);
        result.addProperty("collected_at", DateUtil.toDate(collected_at));
        result.addProperty("collected_at_timestamp",collected_at);
        result.addProperty("duration",duration);
        result.addProperty("total_views",total_views);
        result.addProperty("total_likes",total_likes);
        result.addProperty("total_dislikes",total_dislikes);
        result.addProperty("total_comments",total_comments);
        result.addProperty("total_tweets",total_tweets);
        result.addProperty("total_original_tweets",total_original_tweets);
        result.addProperty("total_retweets",total_retweets);
        result.addProperty("total_channel_views",total_channel_views);
        result.addProperty("total_channel_comments",total_channel_comments);
        result.addProperty("total_channel_subscribers",total_channel_subscribers);
        result.addProperty("total_channel_videos",total_channel_videos);

        JsonArray jsonDays = new JsonArray();
        days.stream().map(day-> day.toJson()).forEach(x->jsonDays.add(x));
        result.add("days",jsonDays);
        return result;
    }

    @Override
    public JsonObject toJson(Map<String, Integer> view) {
        return toJson();
    }
}
