package io.affect.sensemojisdk.api;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.squareup.otto.Subscribe;

import java.io.Serializable;

import io.affect.sensemojisdk.SensemojiAPIActivity;
import io.affect.sensemojisdk.model.Constants;
import io.affect.sensemojisdk.model.SensemojiMoment;
import io.affect.sensemojisdk.util.SensemojiBus;

/**
 * Created by Kevin on 1/10/2016.
 */
public class Sensemoji implements Serializable {
    private static final String TAG = Sensemoji.class.getCanonicalName();
    private static Sensemoji instance = new Sensemoji();
    private SensemojiCallback sensemojiCallback;

    /**
     * This function will launch the Sensemoji Emotion detector
     * @param context - caller Context used for launching detector
     * @param callback - callback object to receive detected emotion
     * @param apiKey - Sensemoji api key used for validating consumer
     */
    public static void launch(Context context, SensemojiCallback callback, String apiKey) {
        instance.sensemojiCallback = callback;
        try {
            SensemojiBus.bus.register(instance);
        } catch(Exception e) {
            Log.w(TAG, "launch(): exception caught registering sensemoji to bus", e);
        }
        Intent sensemojiIntent = new Intent(context, SensemojiAPIActivity.class);
        if((apiKey!=null)&&(apiKey.length()>0)) {
            Bundle extras = new Bundle();
            extras.putString(Constants.API_KEY, apiKey);
            sensemojiIntent.putExtras(extras);
        }
        context.startActivity(sensemojiIntent);
    }
    @Subscribe
    public void emotionSensed(SensemojiMoment moment) {
        if( sensemojiCallback!=null) {
            sensemojiCallback.emojiSensed(moment.getEmoji());
        }
        SensemojiBus.bus.unregister(instance);
    }

}
