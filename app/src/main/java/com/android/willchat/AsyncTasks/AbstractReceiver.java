package com.android.willchat.AsyncTasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.android.willchat.ChatActivity;
import com.android.willchat.MainActivity;
import com.android.willchat.R;
import com.android.willchat.Entities.Message;

public class AbstractReceiver extends AsyncTask<Void, Message, Void>{
	
	@Override
	protected Void doInBackground(Void... params) {
		return null;
	}
	
	protected void playNotification(Context context, Message message){
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		//Intent exactly like Android intent to launch the app => the app is simply brought back to front
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
		/*
		Notification mNotification = new Notification.Builder(context)
			.setContentTitle(message.getChatName())
			.setContentText(message.getmText())
			.setSmallIcon(R.drawable.icon_notification)
			.setContentIntent(pIntent)
			.setSound(notification)			
			.build();*/

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notif = new Notification(R.drawable.icon_notification, message.getmText(), System.currentTimeMillis());
		notif.defaults |= Notification.DEFAULT_SOUND;

		Intent notificationIntent = new Intent(context,ChatActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

		notif.setLatestEventInfo(context, message.getChatName(), message.getmText(), contentIntent);
		mNotificationManager.notify(1, notif);
/*
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotification.defaults |= Notification.DEFAULT_VIBRATE; */
		
		//Check if the app is in foreground or not. Display the notification only if the app is in background
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if(!pref.getBoolean("isForeground", false)){
			mNotificationManager.notify(0, notif);
			//mNotificationManager.notify(0, mNotification);
		}
	}
}
