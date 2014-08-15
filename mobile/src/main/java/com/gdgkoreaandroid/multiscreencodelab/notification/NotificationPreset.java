package com.gdgkoreaandroid.multiscreencodelab.notification;

import android.app.Notification;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.gdgkoreaandroid.multiscreencodelab.R;

/**
 * Created by FlaShilver on 2014. 8. 10..
 */
public abstract class NotificationPreset extends NamedPreset {

    public final int titleResId;
    public final int textResId;

    private static final String EXAMPLE_GROUP_KEY = "example";

    public static final NotificationPreset PRESETS = new BasicNotificationPreset();

    public NotificationPreset(int nameResId, int titleResId, int textResId) {
        super(nameResId);
        this.titleResId = titleResId;
        this.textResId = textResId;
    }

    public static class BuildOptions {
        public final CharSequence titlePreset;
        public final CharSequence textPreset;
        public final PriorityPreset priorityPreset;
        public final ActionsPreset actionsPreset;
        public final boolean includeLargeIcon;
        public final boolean hasContentIntent;
        public final Integer[] backgroundIds;

        public BuildOptions(CharSequence titlePreset, CharSequence textPreset,
                            PriorityPreset priorityPreset, ActionsPreset actionsPreset,
                            boolean includeLargeIcon, boolean hasContentIntent,Integer[] backgroundIds) {
            this.titlePreset = titlePreset;
            this.textPreset = textPreset;
            this.priorityPreset = priorityPreset;
            this.actionsPreset = actionsPreset;
            this.includeLargeIcon = includeLargeIcon;
            this.hasContentIntent = hasContentIntent;
            this.backgroundIds = backgroundIds; }
    }

    /** Build a notification with this preset and the provided options */
    public abstract Notification[] buildNotifications(Context context, BuildOptions options);

    /** Whether actions are required to use this preset. */
    public boolean actionsRequired() {
        return false;
    }

    /** Number of background pickers required */
    public int countBackgroundPickersRequired() {
        return 0;
    }

    private static NotificationCompat.Builder applyBasicOptions(Context context,
                                                                NotificationCompat.Builder builder, NotificationCompat.WearableExtender wearableOptions,
                                                                NotificationPreset.BuildOptions options) {
        builder.setContentTitle(options.titlePreset)
                .setContentText(options.textPreset)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDeleteIntent(NotificationUtil.getExamplePendingIntent(
                        context, R.string.example_notification_deleted));
        options.actionsPreset.apply(context, builder, wearableOptions);
        options.priorityPreset.apply(builder, wearableOptions);

        builder.setLargeIcon(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.example_large_icon));
        builder.setContentIntent(NotificationUtil.getExamplePendingIntent(context,
                R.string.content_intent_clicked));

        return builder;
    }

    private static class BasicNotificationPreset extends NotificationPreset {
        public BasicNotificationPreset() {
            super(R.string.basic_example, R.string.example_content_title,
                    R.string.example_content_text);
        }

        @Override
        public Notification[] buildNotifications(Context context, BuildOptions options) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            NotificationCompat.WearableExtender wearableOptions =
                    new NotificationCompat.WearableExtender();
            applyBasicOptions(context, builder, wearableOptions, options);
            builder.extend(wearableOptions);
            return new Notification[]{builder.build()};
        }
    }
}