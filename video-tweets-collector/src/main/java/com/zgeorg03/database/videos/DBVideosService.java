package com.zgeorg03.database.videos;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.zgeorg03.database.DBConnection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

/**
 * Created by zgeorg03 on 3/1/17.
 */
public class DBVideosService implements DBVideosI {
    private final Logger logger = LoggerFactory.getLogger(DBVideosService.class);

    private final DBConnection dbConnection;
    private final MongoCollection videos;


    public DBVideosService(DBConnection dbConnection, MongoCollection videos) {
        this.dbConnection = dbConnection;
        this.videos = videos;
    }

    @Override
    public int getTotalMonitoredVideosAndNotFinished() {
        return (int) videos.count(
                and( eq("meta.monitored",true) ,eq("meta.finished",false)) );
    }

    @Override
    public boolean setVideoAsFinished(String video_id) {
        Document video =(Document) videos.find(eq("_id",video_id)).first();
        if(video==null){
            logger.error("Video not found:"+video_id);
            return false;
        }

        Document meta = (Document) video.get("meta");

        meta.put("finished",true);
        meta.put("monitored",false);

        video.put("meta",meta);
        videos.replaceOne(eq("_id",video_id),video);
        return true;
    }

    @Override
    public int getTotalFinishedVideos() {
        return (int) videos.count( and(
                eq("meta.finished",true),eq("meta.incomplete",false)));
    }

    @Override
    public int getTotalIncompleteVideos() {
        return (int) videos.count( eq("meta.incomplete",true) );
    }

    @Override
    public List<String> getVideosThatNeedDynamicUpdate(long duration, TimeUnit unit) {
        long durationInMillis = unit.toMillis(duration);
        long time = System.currentTimeMillis();

        Document projection = new Document("meta",1).append("_id",1);
        Collection documents = videos.find(eq("meta.monitored",true)).projection(projection).into(new LinkedList<Document>());
        List<String> videos = new LinkedList<>();

        for(Object obj : documents){
            Document doc = (Document)obj;
            String video_id = (String) doc.get("_id");

            long last_update = ((Document)doc.get("meta")).getLong("last_update");
            if(time-last_update>=durationInMillis*2){
                //Mark as incomplete
                try{
                    Document update = new Document("meta.incomplete",true);
                    this.videos.updateOne(eq("_id", video_id),update);
                } catch(MongoWriteException we){
                    logger.error(we.getError().getMessage());
                } catch(MongoException ex) {
                    logger.error(ex.getLocalizedMessage());
                }

            }else if(time-last_update>=durationInMillis)
                videos.add(video_id);

        }

        return videos;
    }

    @Override
    public Map<String,Integer> getVideosThatNeedComments(int maxComments) {
        Map<String,Integer> ids = new HashMap<>();
        MongoCursor<Document> cursor = videos.find(and(eq("meta.monitored",true),
                eq("meta.comments_finished",false)))
                .projection(fields(include("_id","days","meta"))).iterator();

        while(cursor.hasNext()){
            Document document  = cursor.next();
            String id= document.getString("_id");
            Document meta = (Document) document.get("meta");
            List<Document> days = (List<Document>) document.get("days");
            if(days.isEmpty())
                continue;
            long comments = days.get(days.size()-1).getLong("comment_count");
            long commentsCollected = meta.getLong("comments_collected");
            int commentsToReach = (int) Math.min(comments,maxComments);
            int remaining  = (int) (commentsToReach-commentsCollected);
            if(remaining==0)
                continue;
            ids.put(id,commentsToReach);

        }
        cursor.close();

        return ids;
    }

    @Override
    public boolean checkVideoExistenceAndBeingMonitored(String video_id) {
        return videos.count(and(eq("_id",video_id),eq("meta.monitored",true)))==1;
    }

    @Override
    public boolean checkVideoExistenceOnly(String video_id) {
        return videos.count(eq("_id",video_id))==1;
    }

    @Override
    public boolean checkVideoIsFinished(String video_id) {
        return videos.count(and(eq("_id",video_id),eq("meta.current_date",16)))==1;
    }

    @Override
    public boolean addNewVideo(String video_id,JsonObject videoObject) {
        Document document = new Document();


        try {
            document.append("_id",video_id);
            document.append("title",videoObject.get("title").getAsString());
            document.append("channel_id",videoObject.get("channel_id").getAsString());
            document.append("description",videoObject.get("description").getAsString());
            document.append("category_id",videoObject.get("category_id").getAsInt());
            document.append("published_at",videoObject.get("published_at").getAsLong());
            document.append("duration",videoObject.get("duration").getAsLong());

            Document meta = new Document("timestamp",System.currentTimeMillis())
                    .append("monitored",true)
                    .append("finished",false)
                    .append("incomplete",false)
                    .append("comments_collected",0L)
                    .append("comments_finished",false)
                    .append("last_update",-1L)
                    .append("current_date",0);
            JsonArray topicsArray = videoObject.get("topics").getAsJsonArray();
            List<String> topics = new LinkedList<>();
            for (JsonElement element : topicsArray)
                topics.add(element.getAsString());

            document.append("topics",topics);
            document.append("days",new LinkedList<>());
            document.append("meta",meta);
            videos.insertOne(document);
            return true;
        } catch(NullPointerException ex){
            logger.error("Missing fields from the object");
            return  false;
        } catch(MongoWriteException we){
            logger.error(we.getError().getMessage());
            return false;
        } catch(MongoException ex) {
            logger.error(ex.getLocalizedMessage());
            return false;
        }
    }
    @Override
    public boolean addDynamicData(String video_id, JsonObject dynamicData) {
        Document video =(Document) videos.find(eq("_id",video_id)).first();

        if(video==null){
            logger.error("Video not found:"+video_id);
            return false;
        }
        List<Document> days = (List<Document>) video.get("days");
        Document meta = (Document) video.get("meta");
        int current_date = meta.getInteger("current_date");

        try {
            Document day;

            // If its the first day
            if(days.size()==0) {
                day = new Document()
                        .append("day", current_date)
                        .append("timestamp", dynamicData.get("timestamp").getAsLong())
                        .append("view_count", dynamicData.get("view_count").getAsLong())
                        .append("like_count", dynamicData.get("like_count").getAsLong())
                        .append("dislike_count", dynamicData.get("dislike_count").getAsLong())
                        .append("favorite_count", dynamicData.get("favorite_count").getAsLong())
                        .append("comment_count", dynamicData.get("comment_count").getAsLong())
                        .append("channel_view_count", dynamicData.get("channel_view_count").getAsLong())
                        .append("channel_comment_count", dynamicData.get("channel_comment_count").getAsLong())
                        .append("channel_subscriber_count", dynamicData.get("channel_subscriber_count").getAsLong())
                        .append("channel_video_count", dynamicData.get("channel_video_count").getAsLong());
            }else{
                day = new Document();
                Document last_day = days.get(days.size()-1);

                long ld_view_count = last_day.getLong("view_count");
                if(ld_view_count==-1)
                    day.append("view_count", dynamicData.get("view_count").getAsLong());
                else
                    day.append("view_count", dynamicData.get("view_count").getAsLong()-ld_view_count);

                long ld_like_count = last_day.getLong("like_count");
                if(ld_like_count==-1)
                    day.append("like_count", dynamicData.get("like_count").getAsLong());
                else
                    day.append("like_count", dynamicData.get("like_count").getAsLong()-ld_like_count);

                long ld_dislike_count = last_day.getLong("dislike_count");
                if(ld_dislike_count==-1)
                    day.append("dislike_count", dynamicData.get("dislike_count").getAsLong());
                else
                    day.append("dislike_count", dynamicData.get("dislike_count").getAsLong()-ld_dislike_count);

                long ld_favorite_count = last_day.getLong("favorite_count");
                if(ld_favorite_count==-1)
                    day.append("favorite_count", dynamicData.get("favorite_count").getAsLong());
                else
                    day.append("favorite_count", dynamicData.get("favorite_count").getAsLong()-ld_favorite_count);

                long ld_comment_count = last_day.getLong("comment_count");
                if(ld_comment_count==-1)
                    day.append("comment_count", dynamicData.get("comment_count").getAsLong());
                else
                    day.append("comment_count", dynamicData.get("comment_count").getAsLong()-ld_comment_count);

                long ld_channel_view_count = last_day.getLong("channel_view_count");
                if(ld_channel_view_count==-1)
                    day.append("channel_view_count", dynamicData.get("channel_view_count").getAsLong());
                else
                    day.append("channel_view_count", dynamicData.get("channel_view_count").getAsLong()-ld_channel_view_count);

                long ld_channel_comment_count = last_day.getLong("channel_comment_count");
                if(ld_channel_comment_count==-1)
                    day.append("channel_comment_count", dynamicData.get("channel_comment_count").getAsLong());
                else
                    day.append("channel_comment_count", dynamicData.get("channel_comment_count").getAsLong()-ld_channel_comment_count);

                long ld_channel_subscriber_count = last_day.getLong("channel_subscriber_count");
                if(ld_channel_subscriber_count==-1)
                    day.append("channel_subscriber_count", dynamicData.get("channel_subscriber_count").getAsLong());
                else
                    day.append("channel_subscriber_count", dynamicData.get("channel_subscriber_count").getAsLong()-ld_channel_subscriber_count);


            }
            days.add(day);
            meta.put("last_update",System.currentTimeMillis());
            meta.put("comments_finished",false); //Request new comments
            meta.put("current_date",current_date+1);
            video.put("days",days);
            video.put("meta",meta);
            videos.replaceOne(eq("_id",video_id),video);
            return true;
        } catch(NullPointerException ex){
            ex.printStackTrace();
            logger.error("Missing fields from the object");
            return  false;
        } catch(MongoWriteException we){
            logger.error(we.getError().getMessage());
            return false;
        } catch(MongoException ex) {
            logger.error(ex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public boolean deleteVideo(String video_id) {
        try{
            return videos.deleteOne(eq("_id",video_id)).getDeletedCount() == 1;
        } catch(MongoWriteException we){
            logger.error(we.getError().getMessage());
            return false;
        } catch(MongoException ex) {
            logger.error(ex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public boolean setVideoAsIncomplete(String videoId) {
        Document document = (Document) videos.find(eq("_id",videoId)).first();
        if(document==null)
            return false;
        try{
            Document meta = (Document) document.get("meta");
            meta.put("incomplete",true);
            meta.put("monitored",false);
            meta.put("finished",false);
            document.put("meta",meta);
            videos.replaceOne(eq("_id", videoId), document);
        } catch(MongoWriteException we){
            logger.error(we.getError().getMessage());
            return false;
        } catch(MongoException ex) {
            logger.error(ex.getLocalizedMessage());
            return false;
        }
        return true;
    }

    @Override
    public String getChannelId(String video_id) {
        Document video =(Document) videos.find(eq("_id",video_id)).first();

        if(video==null){
            logger.error("Video not found:"+video_id);
            return "error";
        }
        return video.getString("channel_id");
    }
}
