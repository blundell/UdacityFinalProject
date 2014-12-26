package com.blundell.viewvideoyoutube.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.blundell.viewvideoyoutube.R;
import com.blundell.viewvideoyoutube.data.VideoContract;
import com.blundell.viewvideoyoutube.data.VideoContract.VideoEntry;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YouTubeSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = YouTubeSyncAdapter.class.getSimpleName();
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
//
//    private static final String[] NOTIFY_VIDEO_PROJECTION = new String[]{
//            VideoEntry.COLUMN_DESCRIPTION,
//            VideoEntry.COLUMN_DURATION,
//            VideoEntry.COLUMN_TITLE,
//            VideoEntry.COLUMN_THUMB_URL,
//            VideoEntry.COLUMN_VIDEO_URL
//    };
//
//    //these indices must match the projection
//    private static final int INDEX_WEATHER_ID = 0;
//    private static final int INDEX_MAX_TEMP = 1;
//    private static final int INDEX_MIN_TEMP = 2;
//    private static final int INDEX_SHORT_DESC = 3;

    public YouTubeSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        String jsonStr = getJson();
        if (jsonStr == null) {
            Log.e(LOG_TAG, "retrieval error");
            return;
        }

        Vector<ContentValues> cVVector = parseJson(jsonStr);
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().delete(VideoEntry.CONTENT_URI, null, null);

            getContext().getContentResolver().bulkInsert(VideoEntry.CONTENT_URI, cvArray);

            notifyWeather();
        }
        Log.d(LOG_TAG, "Task Complete. " + cVVector.size() + " Inserted");
    }

    private String getJson() {
        HttpURLConnection urlConnection = null;

        String format = "jsonc";
        int version = 2;
        String BASE_URL = "http://gdata.youtube.com/feeds/api/standardfeeds/most_popular";
        String FORMAT_PARAM = "alt";
        String VERSION_PARAM = "v";

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(VERSION_PARAM, Integer.toString(version))
                .build();

        try {
            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }

            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            if (scanner.hasNext()) {
                return scanner.next();
            } else {
                // Stream was empty.  No point in parsing.
                return null;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attempting to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private Vector<ContentValues> parseJson(String jsonStr) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>();
        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONObject data = json.getJSONObject("data");
            JSONArray items = data.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                // Get the JSON object representing the day
                JSONObject videoDetails = items.getJSONObject(i);

                String title = videoDetails.getString("title");
                String description = videoDetails.getString("description");
                long duration = videoDetails.getLong("duration");

                JSONObject thumbObject = videoDetails.getJSONObject("thumbnail");
                String thumbVideoUrl = thumbObject.getString("sqDefault");
                String highQualityThumbVideoUrl = thumbObject.getString("hqDefault");

                JSONObject playerObject = videoDetails.getJSONObject("player");
                String videoUrl = playerObject.optString("default");

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(VideoEntry.COLUMN_TITLE, title);
                weatherValues.put(VideoEntry.COLUMN_DESCRIPTION, description);
                weatherValues.put(VideoEntry.COLUMN_DURATION, duration);
                weatherValues.put(VideoEntry.COLUMN_THUMB_URL, thumbVideoUrl);
                weatherValues.put(VideoEntry.COLUMN_HQ_THUMB_URL, highQualityThumbVideoUrl);
                weatherValues.put(VideoEntry.COLUMN_VIDEO_URL, videoUrl);

                cVVector.add(weatherValues);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return cVVector;
    }

    private void notifyWeather() {
//        Context context = getContext();
//        checking the last update and notify if it' the first of the day
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
//        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey, Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
//
//        if (displayNotifications) {
//
//            String lastNotificationKey = context.getString(R.string.pref_last_notification);
//            long lastSync = prefs.getLong(lastNotificationKey, 0);
//
//            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
//                // Last sync was more than 1 day ago, let's send a notification with the weather.
//                String locationQuery = Utility.getPreferredLocation(context);
//
//                Uri weatherUri = WeatherEntry.buildWeatherLocationWithDate(locationQuery, WeatherContract.getDbDateString(new Date()));
//
//                // we'll query our contentProvider, as always
//                Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);
//
//                if (cursor.moveToFirst()) {
//                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
//                    double high = cursor.getDouble(INDEX_MAX_TEMP);
//                    double low = cursor.getDouble(INDEX_MIN_TEMP);
//                    String desc = cursor.getString(INDEX_SHORT_DESC);
//
//                    int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
//                    String title = context.getString(R.string.app_name);
//
//                    // Define the text of the forecast.
//                    String contentText = String.format(context.getString(R.string.format_notification),
//                            desc,
//                            Utility.formatTemperature(context, high),
//                            Utility.formatTemperature(context, low));
//
//                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
//                    // notifications.  Just throw in some data.
//                    NotificationCompat.Builder mBuilder =
//                            new NotificationCompat.Builder(getContext())
//                                    .setSmallIcon(iconId)
//                                    .setContentTitle(title)
//                                    .setContentText(contentText);
//
//                    // Make something interesting happen when the user clicks on the notification.
//                    // In this case, opening the app is sufficient.
//                    Intent resultIntent = new Intent(context, MainActivity.class);
//
//                    // The stack builder object will contain an artificial back stack for the
//                    // started Activity.
//                    // This ensures that navigating backward from the Activity leads out of
//                    // your application to the Home screen.
//                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//                    stackBuilder.addNextIntent(resultIntent);
//                    PendingIntent resultPendingIntent =
//                            stackBuilder.getPendingIntent(
//                                    0,
//                                    PendingIntent.FLAG_UPDATE_CURRENT
//                            );
//                    mBuilder.setContentIntent(resultPendingIntent);
//
//                    NotificationManager mNotificationManager =
//                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
//                    // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
//                    mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());
//
//                    //refreshing last sync
//                    SharedPreferences.Editor editor = prefs.edit();
//                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
//                    editor.commit();
//                }
//            }
//        }

    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = VideoContract.CONTENT_AUTHORITY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync

            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime)
                    .setExtras(Bundle.EMPTY)
                    .setSyncAdapter(account, authority)
                    .build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), VideoContract.CONTENT_AUTHORITY, bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name), VideoContract.SYNC_ACC_TYPE);

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        YouTubeSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, VideoContract.CONTENT_AUTHORITY, true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
