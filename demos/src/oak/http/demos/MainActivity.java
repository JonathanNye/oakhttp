package oak.http.demos;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import oak.http.OAKCacheHelper;
import oak.http.OAKGsonRequest;
import oak.http.OAKRequest;
import oak.http.OAKResponse;
import oak.http.OAKResponseParser;
import oak.http.OAKStringRequest;

// User: jnye
// Date: 9/18/12
// Time: 6:20 PM
// Copyright (c) 2012 WillowTree Apps, Inc. All rights reserved.

public class MainActivity extends Activity {

    private int taskNumber = 0;
    private TestAPI api;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OAKCacheHelper.initialize();
        Log.d("OAKHttpDemos", "Uninstalling old cache success: " + OAKCacheHelper.uninstallCache());
        boolean enable = OAKCacheHelper.enableCache(getApplication().getCacheDir(), 1024*1024);
        Log.d("OAKHttpDemos", "Installing new cache success: " + enable);
        api = new TestAPI();
    }

    public void onResume() {
        super.onResume();
        new TestTask(false).execute((Void[])null);
        new StringTask().execute((Void[]) null);
        new GsonTask().execute((Void[]) null);
        new JustTheDataTask().execute((Void[]) null);
    }

    private void doHandleResponse(OAKResponse<String> response) {
        Log.d("OAKHttpDemos", "Hand-crafted response: " + response.getData());
    }

    private class TestTask extends AsyncTask<Void, Void, OAKResponse<String>> {

        boolean forceServerHit = false;
        Exception e;
        int taskNum;


        public TestTask(boolean forceServerHit) {
            this.forceServerHit = forceServerHit;
            taskNum = taskNumber;
            taskNumber++;
        }

        @Override
        protected OAKResponse<String> doInBackground(Void... voids) {

            try {
                return api.getTest(forceServerHit);
            } catch(Exception e) {
                this.e = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(OAKResponse<String> response) {
            if(e == null) {
                doHandleResponse(response);
            } else {
                Log.d("OAKHttpDemos", "MainActivity onPostExecute got exception " + e.getClass().getSimpleName());
            }
        }
    }

    private class StringTask extends AsyncTask<Void, Void, OAKResponse<String>> {
        Exception e;

        @Override
        protected OAKResponse<String> doInBackground(Void... unused) {
            try {
                return api.getStringTest();
            } catch(Exception e) {
                this.e = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(OAKResponse<String> response) {
            if(e == null) {
                Log.d("OAKHttpDemos", "Successful string response: " + response.getData().substring(0, 20));
            } else {
                Log.d("OAKHttpDmos", "StringTask got " + e.getClass().getSimpleName());
            }
        }
    }

    private class GsonTask extends AsyncTask<Void, Void, OAKResponse<Entry.EntryResponse>> {
        Exception e;

        @Override
        protected OAKResponse<Entry.EntryResponse> doInBackground(Void... unused) {
            try {
                return api.getGsonTest();
            } catch(Exception e) {
                this.e = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(OAKResponse<Entry.EntryResponse> response) {
            if(e == null) {
                Log.d("OAKHttpDemos", "Got GSONRepsonse with " + response.getData().entries.size() + " objects");
                for(Entry e : response.getData().getList()) {
                    Log.d("OAKHttpDemos", "---- " + e.getTitle());
                }
            } else {
                Log.d("OAKHttpDemos", "StringTask got " + e.getClass().getSimpleName());
            }
        }
    }

    private class JustTheDataTask extends AsyncTask<Void, Void, List<Entry>> {
        Exception e;

        @Override
        protected List<Entry> doInBackground(Void... unused) {
            try {
                return api.getJustDataTest();
            } catch(Exception e) {
                this.e = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Entry> entries) {
            if(e == null) {
                Log.d("OAKHttpDemos", "Got just a list with " + entries.size() + " objects");
                for(Entry e : entries) {
                    Log.d("OAKHttpDemos", "---- " + e.getTitle());
                }
            } else {
                Log.d("OAKHttpDemos", "StringTask got " + e.getClass().getSimpleName());
            }
        }
    }

    private static class TestAPI {

        public OAKResponse<String> getTest(boolean forceServerHit) throws Exception {
            OAKRequest<String> request = new OAKRequest<String>("http://www.google.com",
                    new OAKResponseParser<String>() {
                        @Override
                        public String parseResponse(InputStream is) throws Exception {
                            StringBuilder sb = new StringBuilder();
                            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
                            String s;

                            while(true) {
                                s = buf.readLine();
                                if(s == null) {
                                    break;
                                }
                                sb.append(s);
                            }
                            buf.close();

                            return sb.toString().substring(0, 30) + "...";
                        }
                    }
            );
            request.setMethod("GET").setMaxStale(20).setNoCache(forceServerHit);
            return request.execute();
        }

        public OAKResponse<String> getStringTest() throws Exception {
            OAKStringRequest req = new OAKStringRequest("http://www.google.com");
            return req.execute();
        }

        public OAKResponse<Entry.EntryResponse> getGsonTest() throws Exception {
            OAKGsonRequest<Entry.EntryResponse> req = new OAKGsonRequest<Entry.EntryResponse>(
                    "http://nacd.willowtreemobile.com/dailynews.json?limit=5",
                    Entry.EntryResponse.class
            );
            return req.execute();
        }

        public List<Entry> getJustDataTest() throws Exception {
            OAKGsonRequest<Entry.EntryResponse> req = new OAKGsonRequest<Entry.EntryResponse>(
                    "http://nacd.willowtreemobile.com/dailynews.json?limit=5",
                    Entry.EntryResponse.class
            );
            OAKResponse<Entry.EntryResponse> resp = req.execute();
            return resp.getData().entries;
        }
    }

    private static class Entry {
        private static DateTimeFormatter mdyFormatter = DateTimeFormat.forPattern("MM/dd/yy");
        private static final String VIDEO_SOURCE = "BoardVision";

        private String category;
        private long updated;
        private String title;
        private String content;
        private String link;
        private String source;
        private String author;
        private String id;
        @SerializedName("image") private String imageUrl;
        @SerializedName("video_length") private String videoLength;
        @SerializedName("content_type") private String contentType;
        @SerializedName("metacategory") private List<String> metaCategories;
        @SerializedName("auth_required") private boolean authRequired;
        @SerializedName("youtube_image") private String youtubeImage;

        public String getCategory() {
            return category;
        }

        public long getUpdated() {
            return updated;
        }

        public String getFormattedDate() {
            DateTime d = new DateTime(updated * 1000L);
            return d.toString(mdyFormatter);
        }

        public String getTimeAgo() {
            DateTime before = new DateTime(updated * 1000L);
            DateTime now = DateTime.now();
            int months = Months.monthsBetween(before, now).getMonths();
            if (months > 0) {
                return months + ( months == 1 ? " month ago" : " months ago");
            }
            int days = Days.daysBetween(before, now).getDays();
            if (days > 0) {
                return days + ( days == 1 ? " day ago" : " days ago");
            }
            int hours = Hours.hoursBetween(before, now).getHours();
            if (hours > 0) {
                return hours + ( hours == 1 ? " hour ago" : " hours ago");
            }
            int minutes = Minutes.minutesBetween(before, now).getMinutes();
            return minutes + ( minutes == 1 ? " minute ago" : " minutes ago");
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getLink() {
            return link;
        }

        public String getContentType() {
            return contentType;
        }

        public List<String> getMetaCategories() {
            return metaCategories;
        }

        public boolean isVideo() {
            return source.equalsIgnoreCase(VIDEO_SOURCE);
        }

        public String getVideoLength() {
            return videoLength;
        }

        public boolean isAuthRequired() {
            return authRequired;
        }

        public String getSource() {
            return source;
        }

        public String getAuthor() {
            return author;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public boolean hasImage() {
            return imageUrl != null && imageUrl.length() > 0;
        }

        public String getYoutubeImage () {
            return youtubeImage;
        }

        public boolean hasYoutubeImage() {
            return youtubeImage != null && youtubeImage.length() > 0;
        }

        public boolean shouldShowReadMore() {
            return metaCategories.contains("dailynews") && getLink().length() > 0;
        }

        public String getId() {
            return id;
        }

        public static class EntryResponse {
            List<Entry> entries;
            @SerializedName("more_entries") boolean moreEntries;

            public List<Entry> getList() {
                return entries;
            }

            public boolean moreEntries() {
                return moreEntries;
            }

        }
    }

}