package edu.noctrl.ydeleon.project2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Created by Christian on 5/11/2015.
 */
public class MessageMaker {
    public static void toastMaker(String message){
        Toast toast = Toast.makeText(MainActivity.mainContext, message, Toast.LENGTH_SHORT);
        toast.show();
    }
    public static void notificationMaker(String title, String text){
        NotificationCompat.Builder b = new NotificationCompat.Builder(MainActivity.mainContext);

        b.setSmallIcon(R.drawable.weather);
        b.setContentTitle(title);
        b.setContentText(text);

        Intent im = new Intent(Intent.ACTION_VIEW, Uri.parse(text));

        TaskStackBuilder stackB = TaskStackBuilder.create(MainActivity.mainContext);
        stackB.addParentStack(MainActivity.class);
        stackB.addNextIntent(im);

        PendingIntent p = stackB.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        b. setContentIntent(p);

        NotificationManager nm = (NotificationManager)MainActivity.mainContext.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(0, b.build());

    }
}
