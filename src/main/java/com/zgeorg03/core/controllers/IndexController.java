package com.zgeorg03.core.controllers;

import com.zgeorg03.core.controllers.helpers.GetRequest;
import com.zgeorg03.core.controllers.helpers.Parameter;
import com.zgeorg03.core.utils.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * Created by zgeorg03 on 2/20/17.
 */
public class IndexController{

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    public IndexController(){

       new GetRequest("/"){

           @Parameter(description = "id")
           public int id;

           @Override
           public Object execute(Request request, Response response, JsonResult result) {
               result.addString("msg","Welcome to Yttresearch project");

               return result.build();
           }

           @Override
           public void handleParams(Request request, Response response, JsonResult result) {

           }
       };
    }


}
