package com.e2e;

import com.couchbase.client.java.document.json.JsonObject;
import com.storage.Queue;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class e2eTest {

    /*
    Currently doing Two kinds of Model to show the function of distributed queue

    JSON Model1:
    {
    "type": "shell",
    "command" : "to be executed"
    }
    */
    Queue cb = null;

    @Before
    public void setUp() throws Exception {
        cb = Queue.getInstance("tiny-url", "password");
    }

    @After
    public void tearDown() throws Exception {
        cb.disconnect();
    }

    @Test
    public void basicFunctions() {
        /* The property of Queue will remain
          First key entered will be removed.
         */
        final JsonObject object1 = JsonObject.empty()
                .put("id", "1234")
                .put("zip", "CA-940" + String.valueOf(1234));
        cb.push(object1);
        final JsonObject object2 = JsonObject.empty()
                .put("id", "1235")
                .put("zip", "CA-940" + String.valueOf(1235));
        cb.push(object2);

        JsonObject val = cb.pop();
        assertEquals(val.get("id"),"1234");
    }

}
