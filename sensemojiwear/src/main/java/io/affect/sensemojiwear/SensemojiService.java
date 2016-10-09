package io.affect.sensemojiwear;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Kevin on 12/18/2015.
 */
public class SensemojiService extends WearableListenerService {
    private static final String START_WEAR = "start_wear";
    private static final String STOP_WEAR = "stop_wear";
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(START_WEAR.equalsIgnoreCase(messageEvent.getPath())) {
            Intent wearIntent = new Intent(this, SensemojiWearActivity.class);
            wearIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(wearIntent);
        }
    }
}
