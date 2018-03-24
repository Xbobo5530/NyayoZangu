package com.nyayozangu.sean.nyayozangustore;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by Sean on 3/23/18.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "Sean";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        /*
        the incoming message bares the following format
        {
            "to":"/topics/UPDATES",
                "data":{
            "targetUrl":"https://store.nyayozangu.com/collections/footwear",
                    "title":"Easter Sale",
                    "message":"Get up to 60% off on you next purchase during the easter season"
        }
        }
        */

        if (remoteMessage.getData().size() > 0) {

            Map<String, String> data = remoteMessage.getData();

            String title = data.get("title");
            String message = data.get("message");
            String targetUrl = data.get("targetUrl");
            Log.d(TAG, "onMessageReceived: Message Received: \n" +
                    "Title: " + title + "\n" +
                    "Message: " + message + "\n" +
                    "targetUrl: " + targetUrl);

            sendNotification(title, message, targetUrl);
        }
    }

    private void sendNotification(String title, String messageBody, String targetUrl) {
        Log.d(TAG, "at sendNotification");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("targetUrl", targetUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setColor(getColor(R.color.colorPrimaryDark))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(), R.mipmap.ic_notif_logo_white))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(messageBody));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NotificationChannel.DEFAULT_CHANNEL_ID,
                    getString(R.string.default_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }

    @Override
    public void onDeletedMessages() {

    }
}
