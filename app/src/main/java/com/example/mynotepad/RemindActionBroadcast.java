package com.example.mynotepad;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;


public class RemindActionBroadcast extends BroadcastReceiver {
    public static int id=0;
    @Override
    public void onReceive(Context context, Intent intent) {

        PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intent,0);
        NotificationManager notificationManager=(NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        Notification.Builder mbuilder=new Notification.Builder(context);
        mbuilder.setContentTitle(intent.getStringExtra("title"));
        mbuilder.setContentText(intent.getStringExtra("context"));
        mbuilder.setSmallIcon(R.mipmap.ic_launcher);
        mbuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher));
        mbuilder.setContentIntent(pendingIntent);
        mbuilder.setAutoCancel(true);
        Notification notification=mbuilder.build();
        notificationManager.notify(id++,notification);//能够传送多条消息
    }
}
