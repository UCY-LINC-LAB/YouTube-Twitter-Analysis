package com.zgeorg03;

import com.google.gson.JsonObject;
import com.zgeorg03.database.DBServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by zgeorg03 on 2/25/17.
 */
public class DynamicInfoMonitor implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(DynamicInfoMonitor.class);
    private  final DBServices dbServices;
    private final StatusMonitor statusMonitor;
    private final Arguments arguments;

    public DynamicInfoMonitor(DBServices dbServices, StatusMonitor statusMonitor, Arguments arguments) {
        this.dbServices = dbServices;
        this.statusMonitor = statusMonitor;
        this.arguments = arguments;
    }

    @Override
    public void run() {

        int count=1;
        while(true){
            List<String> videos = dbServices.getDbVideosService().getVideosThatNeedDynamicUpdate();

            if(videos.size()==0){
                try {
                    TimeUnit.MINUTES.sleep(count);
                    if(count<4)
                        count++;
                } catch (InterruptedException e) { e.printStackTrace(); }
            }else{
                count=1;
            }



            videos.forEach(video -> {
                //This is a new video
                String key = dbServices.getYouTubeAPIKey();
                if(arguments.isDebug_mode())
                    key = "AIzaSyCSC05BGk8RIdoHBT81RP4CWj2SnLFoHnA";
                if (!key.isEmpty()) {

                    YouTubeRequests requests = new YouTubeRequests(video, key);
                    String channelId = dbServices.getDbVideosService().getChannelId(video);
                    JsonObject dynamicData = requests.getDynamicData(channelId);

                    //Checking if a video reached 15th day
                    if (dbServices.getDbVideosService().checkVideoIsFinished(video)) {

                        if (dbServices.getDbVideosService().setVideoAsFinished(video)) {
                            logger.info("Video:" + video + " has finished!");
                            statusMonitor.setReachedMonitorCapacity(false);
                        }

                        if (dbServices.getDbVideosService().addDynamicData(video, dynamicData))
                            logger.info("Dynamic data added for " + video);

                    }else if (dynamicData.get("error") != null) {
                        logger.error("Dynamic data couldn't be fetched for " + video + " because " + dynamicData.get("error").getAsString());

                        if (dbServices.getDbVideosService().setVideoAsIncomplete(video)) {
                            logger.error(video + " is set as incomplete");
                            statusMonitor.setReachedMonitorCapacity(false);
                        }
                    } else if (dbServices.getDbVideosService().addDynamicData(video, dynamicData))
                        logger.info("Dynamic data added for " + video);

                }else {
                    logger.error("YouTube key not available");
                }
            });
        }
    }
}
