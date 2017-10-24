package com.storage;

import com.couchbase.client.java.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;
import com.couchbase.client.java.document.JsonLongDocument;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import java.util.concurrent.atomic.AtomicInteger;

public  class Queue  {

    Cluster cluster = null;
    Bucket bucket = null;
    int   count = 0;
    JsonObject value = null;
    private static Queue instance = null;
    private Queue() {}

    public static Queue getInstance(final String bucket_name, final String password) {
        if(instance == null) {
            //This one used to prevent lot of connections in a single machine
            synchronized (Queue.class) {
                instance = new Queue();
                instance.connect(bucket_name, password);
            }
        }
        return instance;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void connect(final String bucket_name, final String password) {
        cluster = CouchbaseCluster.create();
        bucket = cluster.openBucket(bucket_name, password);
    }

    public void push(final JsonObject object){
        bucket.async()
                .counter("queue:front", 1, 1).map(new Func1<JsonLongDocument, String>() {
            public String call(JsonLongDocument counter) {
                return "ADJ_TXN_ID:" + counter.content();
            }
        }).flatMap(new Func1<String, rx.Observable<JsonDocument>>() {
            public Observable<JsonDocument> call(String id) {
                return bucket.async().insert(JsonDocument.create(id, object));
            }
        }).toBlocking().single();
    }

    public JsonObject pop(){

        bucket.async()
                .counter("queue:rear", 1, 1).map(new Func1<JsonLongDocument, String>() {

            public String call(JsonLongDocument counter) {
                return "ADJ_TXN_ID:" + counter.content();
            }
        }).flatMap(new Func1<String, rx.Observable<JsonDocument>>() {

            public Observable<JsonDocument> call(String id) {
                value = bucket.get(id).content(); return bucket.async().remove(id);
            }
        }).toBlocking().single();
        return value;
    }

    public void disconnect() {
        if (bucket != null) {
            bucket.close();
            cluster.disconnect();
            bucket = null;
            cluster = null;
        }
    }

}

