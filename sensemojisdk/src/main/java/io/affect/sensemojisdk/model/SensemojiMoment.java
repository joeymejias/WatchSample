package io.affect.sensemojisdk.model;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Kevin on 12/22/2015.
 */
public class SensemojiMoment implements Serializable {
    public enum Emotion{ ANGER, DISGUST, FEAR, JOY, SADNESS, SURPRISE, UNKNOWN };
    private Bitmap photo;
    private Bitmap emoji;
    private Emotion emotion;
    private int rating;

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public Bitmap getEmoji() {
        return emoji;
    }

    public void setEmoji(Bitmap emoji) {
        this.emoji = emoji;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
