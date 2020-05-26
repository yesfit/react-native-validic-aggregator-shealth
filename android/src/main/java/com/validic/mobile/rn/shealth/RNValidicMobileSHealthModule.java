package com.validic.mobile.rn.shealth;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.validic.mobile.NotificationParams;
import com.validic.mobile.record.Record;
import com.validic.mobile.shealth.SHealthError;
import com.validic.mobile.shealth.SHealthManager;
import com.validic.mobile.shealth.SHealthServiceStopReceiver;
import com.validic.mobile.shealth.SHealthSubscription;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import static com.validic.mobile.rn.shealth.Utils.sendEvent;


/**
 * Created by griffinwilson on 2/21/18.
 */

public class RNValidicMobileSHealthModule extends ReactContextBaseJavaModule implements SHealthManager.SHealthListener {

    private static final String NOTIFICATION_ID_KEY = "notification_id";
    private static final String NOTIFICATION_TITLE_KEY = "notification_title";
    private static final String NOTIFICATION_ICON_KEY = "notification_icon";
    private static final String NOTIFICATION_MESSAGE_KEY = "notification_message";
    private static final String NOTIFICATION_CLEAR_TEXT_KEY = "notification_clear_text";

    private static final String EVENT_ON_RECORDS = "validic:shealth:onrecords";
    private static final String EVENT_ON_HISTORY_FETCH = "validic:shealth:onhistoryfetch";
    private static final String EVENT_ON_ERROR = "validic:shealth:onerror";
    private static final String EVENT_ON_PERMISSION_CHANGE = "validic:shealth:onpermissionchange";

    private NotificationManager notificationManager;
    private final String notificationChannelId = UUID.randomUUID().toString();
    private static final String notificationChannelName = "SHealthNotificationChannel";
    private static final int NOTIFICATION_CANCEL_REQUEST_CODE = 200;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationParams notificationParams;

    public RNValidicMobileSHealthModule(ReactApplicationContext reactContext) {
        super(reactContext);
        notificationManager = (NotificationManager) reactContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(reactContext, notificationChannelId);
        createNotificationChannel();
    }

    @Override
    public void initialize() {
        super.initialize();
        SHealthManager.getInstance().setSHealthListener(this);
    }

    @Override
    public String getName() {
        return "RNValidicMobileSHealth";
    }

    @ReactMethod
    public void configure(ReadableMap config) {
        notificationParams = parseNotificationArgs(config);
        SHealthManager.getInstance().observeCurrentSubscriptions(notificationParams);
    }

    @ReactMethod
    public void addSubscriptions(ReadableArray subscriptions) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < subscriptions.size(); i++) {
            set.add(subscriptions.getString(i));
        }
        SHealthManager.getInstance().addSubscriptions(notificationParams, set);
    }

    @ReactMethod
    public void removeSubscriptions(ReadableArray dataTypes) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < dataTypes.size(); i++) {
            set.add(dataTypes.getString(i));
        }
        SHealthManager.getInstance().removeSubscriptions(set);
    }

    @ReactMethod
    public void getCurrentSubscriptions(Callback callback) {
        WritableArray array = Arguments.createArray();
        Set<String> subs = SHealthManager.getInstance().getCurrentSubscriptions();
        for (String s : subs) {
            array.pushString(s);
        }
        callback.invoke(array);
    }

    @ReactMethod
    public void fetchHistory(ReadableArray historicalSets) {
        Set<SHealthSubscription.SHealthHistoricalSet> set = new HashSet<>();
        if (historicalSets != null) {
            for (int i = 0; i < historicalSets.size(); i++) {
                try {
                    String s = historicalSets.getString(i);
                    if (s != null) {
                        set.add(SHealthSubscription.SHealthHistoricalSet.valueOf(s));
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (!set.isEmpty())
            SHealthManager.getInstance().fetchHistoricalSets(set);
    }


    @Override
    public void onError(SHealthError sHealthError) {
        WritableMap map = Arguments.createMap();
        map.putString("error", sHealthError.name());
        sendEvent(getReactApplicationContext(), EVENT_ON_ERROR, map);
    }

    @Override
    public void onPermissionChange(String[] accepted, String[] denied) {
        WritableMap map = Arguments.createMap();
        WritableArray acc = Arguments.createArray();
        for (String s : accepted) {
            acc.pushString(s);
        }
        WritableArray den = Arguments.createArray();
        for (String s : denied) {
            den.pushString(s);
        }
        map.putArray("accepted", acc);
        map.putArray("denied", den);
        sendEvent(getReactApplicationContext(), EVENT_ON_PERMISSION_CHANGE, map);
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId,
                    notificationChannelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Samsung Health Foreground Channel");

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private NotificationParams parseNotificationArgs(ReadableMap object) {
        if (object != null && object.hasKey(NOTIFICATION_ID_KEY)) {
            int id = object.getInt(NOTIFICATION_ID_KEY);
            if (id != 0) {
                if (!object.isNull(NOTIFICATION_TITLE_KEY))
                    notificationBuilder.setContentTitle(object.getString(NOTIFICATION_TITLE_KEY));
                if (object.hasKey(NOTIFICATION_ICON_KEY)) {
                    String icon = object.getString(NOTIFICATION_ICON_KEY);
                    notificationBuilder.setSmallIcon(AssetUtil.getInstance(getReactApplicationContext()).getResId(icon));
                }
                if (object.hasKey(NOTIFICATION_MESSAGE_KEY))
                    notificationBuilder.setContentText(object.getString(NOTIFICATION_MESSAGE_KEY));
                if (object.hasKey(NOTIFICATION_CLEAR_TEXT_KEY)) {
                    String cancelMessage = object.getString(NOTIFICATION_CLEAR_TEXT_KEY);
                    if (!TextUtils.isEmpty(cancelMessage)) {
                        Intent intent = new Intent(getReactApplicationContext(), SHealthServiceStopReceiver.class);
                        intent.setAction(SHealthServiceStopReceiver.ACTION_STOP_SHEALTH_SERVICE);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getReactApplicationContext(), NOTIFICATION_CANCEL_REQUEST_CODE,
                                intent, 0);
                        notificationBuilder.addAction(0, cancelMessage, pendingIntent);
                        notificationBuilder.setContentIntent(pendingIntent);
                    }
                }
                Notification notification = notificationBuilder.build();
                notificationManager.notify(id, notification);
                return new NotificationParams(id, notification);
            }
        }

        return null;
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> map = new HashMap<>();
        map.put("EVENT_ON_RECORDS", EVENT_ON_RECORDS);
        map.put("EVENT_ON_ERROR", EVENT_ON_ERROR);
        map.put("EVENT_ON_HISTORY_FETCH", EVENT_ON_HISTORY_FETCH);
        map.put("EVENT_ON_PERMISSION_CHANGE", EVENT_ON_PERMISSION_CHANGE);

        map.put("HISTORICAL_SET_ROUTINE", SHealthSubscription.SHealthHistoricalSet.ROUTINE.name());
        map.put("HISTORICAL_SET_FITNESS", SHealthSubscription.SHealthHistoricalSet.FITNESS.name());
        return map;
    }

    @Override
    public void onRecords(Map<Record.RecordType, Integer> summary) {
        WritableMap sum = Arguments.createMap();
        for (Map.Entry<Record.RecordType, Integer> type : summary.entrySet()) {
            sum.putInt(type.getKey().getTypeName(), type.getValue());
        }
        sendEvent(getReactApplicationContext(), EVENT_ON_RECORDS, sum);
    }

    @Override
    public void onHistoricalPull(Map<Record.RecordType, Integer> summary) {
        WritableMap sum = Arguments.createMap();
        for (Map.Entry<Record.RecordType, Integer> type : summary.entrySet()) {
            sum.putInt(type.getKey().getTypeName(), type.getValue());
        }
        sendEvent(getReactApplicationContext(), EVENT_ON_HISTORY_FETCH, sum);
    }

}