db.getCollection('videos').updateMany({},{$set : {"meta.processed" : false, "meta.finished":true}})