package io.affect.sensemojisdk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.LicenseException;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.affectiva.android.affdex.sdk.detector.PhotoDetector;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import io.affect.sensemojisdk.R;
import io.affect.sensemojisdk.model.SensemojiMoment;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Kevin on 1/7/2016.
 */
public class EmotionDetector implements Detector.ImageListener{
    public static final String TAG = EmotionDetector.class.getCanonicalName();
    private Context context;
    private PhotoDetector pd;
    private GotEmotionCallback callback;
    private AtomicBoolean validLicense = new AtomicBoolean(true);
    private String apiKey;

    private Subscriber<Boolean> startDetector = new Subscriber<Boolean>(){

        @Override
        public void onCompleted() { }

        @Override
        public void onError(Throwable e) { }

        @Override
        public void onNext(Boolean running) {
            try {
                if (running) {
                    pd.stop();
                } else {
                    pd.start();
                }
            } catch(Exception e) {
                Log.e(TAG, "dectector.onNext(): exception caught:", e);
            }
        }
    };

    public EmotionDetector(Context ctx) {
        this.context = ctx;
        try {
            pd = new PhotoDetector(this.context);
            pd.setLicensePath("sdk.license");
            pd.setImageListener(this);
            pd.setDetectAllEmotions(true);
            pd.setDetectAllExpressions(true);
        } catch(LicenseException le) {
            Log.e(TAG, "dectector.onNext(): affectiva license exception:", le);
            validLicense.set(false);
        } catch (Exception e) {
            Log.e(TAG, "constructor(): exception caught:", e);
        }
    }

    public void setEmotionCallback(GotEmotionCallback callback) {
        this.callback = callback;
    }
    public void setApiKey(String key) { this.apiKey = key; }
    public boolean start() {
        if((this.apiKey!=null)&&(this.apiKey.length()>0)) {
            FlurryAgent.logEvent(String.format("SensemojiAPIEmotionAnalysis-%s", this.apiKey));
        }
        return manageDetctor();
    }

    public boolean stop() {
        return manageDetctor();
    }

    public boolean process(Bitmap bm) {
        if(validLicense.get()) {
            try {
                Frame f = new Frame.BitmapFrame(bm, Frame.COLOR_FORMAT.RGBA);
                //f.setTargetRotation(Frame.ROTATE.BY_90_CCW);
                pd.process(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return validLicense.get();
    }

    @Override
    public void onImageResults(List<Face> list, Frame frame, float v) {
        try {
            SensemojiMoment moment = new SensemojiMoment();
            int resID = this.context.getResources().getIdentifier("noemotion", "mipmap", this.context.getPackageName());
            if ((list == null) || (list.size() == 0)) {
                FlurryAgent.logEvent("Failed_Analysis");
                String emoji = "neutral";
                moment.setEmotion(SensemojiMoment.Emotion.UNKNOWN);
                moment.setRating(0);
                int grade = (Math.abs((new Random()).nextInt())%5)+1;
                Log.d(TAG, "onImageResults(): emoji file:" + emoji + String.valueOf(grade));
                resID = this.context.getResources().getIdentifier(emoji + String.valueOf(grade), "mipmap", this.context.getPackageName());
                Bitmap emojiImage = BitmapFactory.decodeResource(this.context.getResources(), resID);
                moment.setEmoji(emojiImage);
            } else {
                Face face = list.get(0);
                ArrayList<Float> emotions = new ArrayList<Float>();
                emotions.add(face.emotions.getAnger());
                emotions.add(face.emotions.getDisgust());
                emotions.add(face.emotions.getFear());
                emotions.add(face.emotions.getJoy());
                emotions.add(face.emotions.getSadness());
                emotions.add(face.emotions.getSurprise());

                Collections.sort(emotions);
                Float max = emotions.get(emotions.size() - 1);
                int grade = 1;
                if (max < 20) {
                    grade = 1;
                } else if (max < 40) {
                    grade = 2;
                } else if (max < 60) {
                    grade = 3;
                } else if (max < 80) {
                    grade = 4;
                } else {
                    grade = 5;
                }

                String emoji = "neutral";
                String valueString = this.context.getString(R.string.neutral);
                if (max.equals(face.emotions.getAnger())) {
                    moment.setEmotion(SensemojiMoment.Emotion.ANGER);
                    emoji = "angry";
                    valueString = this.context.getString(R.string.anger);
                } else if (max.equals(face.emotions.getDisgust())) {
                    moment.setEmotion(SensemojiMoment.Emotion.DISGUST);
                    emoji = "disgust";
                    valueString = this.context.getString(R.string.disgust);
                } else if (max.equals(face.emotions.getFear())) {
                    moment.setEmotion(SensemojiMoment.Emotion.FEAR);
                    emoji = "fear";
                    valueString = this.context.getString(R.string.fear);
                } else if (max.equals(face.emotions.getJoy())) {
                    moment.setEmotion(SensemojiMoment.Emotion.JOY);
                    emoji = "joy";
                    valueString = this.context.getString(R.string.joy);
                } else if (max.equals(face.emotions.getSadness())) {
                    moment.setEmotion(SensemojiMoment.Emotion.SADNESS);
                    emoji = "sadness";
                    valueString = this.context.getString(R.string.sadness);
                } else if (max.equals(face.emotions.getSurprise())) {
                    moment.setEmotion(SensemojiMoment.Emotion.SURPRISE);
                    emoji = "surprise";
                    valueString = this.context.getString(R.string.surprise);
                }

                Log.d(TAG, String.format("ANGER:%.2f\n", face.emotions.getAnger()));
                Log.d(TAG, String.format("DISGUST:%.2f\n", face.emotions.getDisgust()));
                Log.d(TAG, String.format("FEAR:%.2f\n", face.emotions.getFear()));
                Log.d(TAG, String.format("JOY:%.2f\n", face.emotions.getJoy()));
                Log.d(TAG, String.format("SADNESS:%.2f\n", face.emotions.getSadness()));
                Log.d(TAG, String.format("SURPRISE:%.2f\n", face.emotions.getSurprise()));

                resID = this.context.getResources().getIdentifier(emoji + String.valueOf(grade), "mipmap", this.context.getPackageName());
                int resultRating = (int) max.floatValue() > 0 ? (int) max.floatValue() : 1;
                moment.setRating(resultRating);
                String result = String.format(valueString, resultRating);
                HashMap<String, String> flurryParam = new HashMap<String, String>();
                flurryParam.put("EmotionName", result);
                flurryParam.put("EmotionType", moment.getEmotion().toString());
                flurryParam.put("EmotionStrength", String.valueOf(resultRating));
                FlurryAgent.logEvent("Got_Emotion", flurryParam);
                Bitmap emojiImage = BitmapFactory.decodeResource(this.context.getResources(), resID);
                moment.setEmoji(emojiImage);
            }
            this.callback.onMomentCaptured(moment);
        } catch(Exception e) {
            Log.e(TAG, "onImageResults(): exception caught:", e);
        }
    }

    private boolean manageDetctor() {
        if(validLicense.get()) {
            Observable.just(pd.isRunning())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation())
                    .subscribe(startDetector);
        }
        return validLicense.get();
    }

    public interface GotEmotionCallback {
        public void onMomentCaptured(SensemojiMoment moment);
    }

}
