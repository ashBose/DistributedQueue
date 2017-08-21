package main.java.com.Queue;

import com.couchbase.client.java.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;
import com.couchbase.client.java.document.JsonLongDocument;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import java.util.concurrent.atomic.AtomicInteger;

public class distQueue  {

    Cluster cluster = null;
    Bucket bucket = null;
    int   count = 0;


    public void connect(String bucket_name, String password) {
        cluster = CouchbaseCluster.create();
        bucket = cluster.openBucket(bucket_name, password);

    }

    void push(){
        int val = count++; // count is just any variable
        final JsonObject object = JsonObject.empty()
                .put("id", val)
                .put("zip", "CA-940" + String.valueOf(val));

        bucket.async()
                .counter("queue:front", 1, 1).map(new Func1<JsonLongDocument, String>() {

            @Override
            public String call(JsonLongDocument counter) {
                return "ADJ_TXN_ID:" + counter.content();
            }
        }).flatMap(new Func1<String, rx.Observable<JsonDocument>>() {

            @Override
            public Observable<JsonDocument> call(String id) {
                return bucket.async().insert(JsonDocument.create(id, object));
            }
        }).toBlocking().single();
    }


    void pop(){

        bucket.async()
                .counter("queue:rear", 1, 1).map(new Func1<JsonLongDocument, String>() {

            @Override
            public String call(JsonLongDocument counter) {
                return "ADJ_TXN_ID:" + counter.content();
            }
        }).flatMap(new Func1<String, rx.Observable<JsonDocument>>() {

            @Override
            public Observable<JsonDocument> call(String id) {
                return bucket.async().remove(id);
            }
        }).toBlocking().single();
    }

    public void disconnect() {
        bucket.close();
        cluster.disconnect();
    }

    public static void main(String args[]) {
        final distQueue cb = new distQueue();
        cb.connect("tiny-url", "password");
        Runnable cbpush = new Runnable() {
            public void run() {
                cb.push();
            }
        };
        Runnable cbpop = new Runnable() {
            public void run() {
                cb.pop();
            }
        };
        new Thread(cbpush).start();
        new Thread(cbpush).start();
        new Thread(cbpush).start();
        new Thread(cbpop).start();
    }
}

