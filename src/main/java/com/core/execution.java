package com.core;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.storage.Queue;
import java.util.UUID;

public class execution {

    public Queue taskQueue = null;
    public Queue scheduleQueue = null;

    public void connect() {
        taskQueue = Queue.getInstance("task-queue", "password");
        scheduleQueue = Queue.getInstance("schedule", "password");
    }

    public void publish(JsonObject content, Integer... b ) {
        /*
        It was supposed to implement job scheduling. Not implemented
         */
        Integer expiry = b.length > 0 ? b[0] : 0;
        if (expiry > 0 ) {
            scheduleQueue.getBucket().upsert(JsonDocument.create(UUID.randomUUID().toString(), expiry, content));
            //scheduleQueue.getBucket().
        }
    }
}
