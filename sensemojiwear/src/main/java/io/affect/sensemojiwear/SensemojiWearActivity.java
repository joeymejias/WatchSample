package io.affect.sensemojiwear;

import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Collections;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import watch.nudge.gesturelibrary.AbstractGestureClientActivity;
import watch.nudge.gesturelibrary.GestureConstants;

public class SensemojiWearActivity extends AbstractGestureClientActivity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private static final String START_WEAR = "start_wear";
    private static final String STOP_WEAR = "stop_wear";
    private static final String THUMBS = "thumbs";

    private BoxInsetLayout mContainerView;
    private ImageView mSensemoji;
    private TextView mTilt;

    private GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensemoji_wear);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mSensemoji = (ImageView) findViewById(R.id.sensemoji);
        mTilt = (TextView) findViewById(R.id.tilt);

        mApiClient = new GoogleApiClient.Builder( this )
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        setSubscribeWindowEvents(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.MessageApi.removeListener(mApiClient, this);
        mApiClient.disconnect();
    }

    @Override
    public void onGestureWindowClosed() {

    }

    @Override
    public ArrayList<GestureConstants.SubscriptionGesture> getGestureSubscpitionList() {
        ArrayList<GestureConstants.SubscriptionGesture> gestures = new ArrayList<GestureConstants.SubscriptionGesture>();
        gestures.add(GestureConstants.SubscriptionGesture.TILT);

        return gestures;
    }

    @Override
    public boolean sendsGestureToPhone() {
        return false;
    }

    @Override
    public void onSnap() {

    }

    @Override
    public void onFlick() {

    }

    @Override
    public void onTwist() {

    }

    @Override
    public void onTiltX(float v) {

    }

    @Override
    public void onTilt(float tiltX, float tiltY, float tiltZ) {
        ArrayList<Float> values = new ArrayList<Float>();
        values.add(Math.abs(tiltX));
        values.add(Math.abs(tiltY));
        values.add(Math.abs(tiltZ));

        Collections.sort(values);
        Float max = values.get(values.size()-1);
        if(max.equals(Math.abs(tiltX))) {
            mSensemoji.setImageResource(R.mipmap.sensemoji);
            mTilt.setText("X:" + tiltX);
        } else if(max.equals(Math.abs(tiltY))) {
            sendReaction(tiltY);
        } else if(max.equals(Math.abs(tiltZ))) {
            mSensemoji.setImageResource(R.mipmap.sensemoji);
            mTilt.setText("Z:" + tiltZ);
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
        } else {
            mContainerView.setBackground(null);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener( mApiClient, this );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(STOP_WEAR.equalsIgnoreCase(messageEvent.getPath())) {
            finish();
        }
    }
    private void sendReaction(float tiltY) {
        Observable.just(String.valueOf(tiltY))
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String tiltValue) {
                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                        for(Node node : nodes.getNodes()) {
                            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                    mApiClient, node.getId(), THUMBS, tiltValue.getBytes() ).await();
                            System.out.println(result);
                        }
                        return tiltValue;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String tiltY) {
                        setSubscribeWindowEvents(true);
                        mSensemoji.setImageResource(R.mipmap.thumbs);
                        mTilt.setText("Y:" + tiltY);
                    }
                });
    }
}
