package com.blundell.viewvideoyoutube.data;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;

import com.blundell.viewvideoyoutube.BuildConfig;

public class VideoContract {

    public static final String SYNC_ACC_TYPE = "youtube.blundell.com";
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_VIDEO = "video";

    public static class VideoEntry {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEO).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

        public static final String TABLE_NAME = "video";
        public static final String _ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_THUMB_URL = "thumb_url";
        public static final String COLUMN_VIDEO_URL = "video_url";

        public static Uri buildVideoUri() {
            return CONTENT_URI;
        }

        public static Uri buildVideoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getTitle(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        }

        public static String getThumbnailUrl(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_THUMB_URL));
        }
    }

}
