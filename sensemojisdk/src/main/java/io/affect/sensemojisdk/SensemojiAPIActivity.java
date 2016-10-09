package io.affect.sensemojisdk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.facebook.FacebookSdk;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.mopub.mobileads.MoPubView;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.ViewBinder;

import java.io.ByteArrayOutputStream;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.affect.sensemojisdk.api.Sensemoji;
import io.affect.sensemojisdk.model.Constants;
import io.affect.sensemojisdk.model.SensemojiMoment;
import io.affect.sensemojisdk.util.EmotionDetector;
import io.affect.sensemojisdk.util.SensemojiBus;
import io.affect.sensemojisdk.view.Preview;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SensemojiAPIActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
                                                        CompoundButton.OnCheckedChangeListener, MessageApi.MessageListener,
                                                        EmotionDetector.GotEmotionCallback, MoPubNative.MoPubNativeNetworkListener{
    public static final String TAG = SensemojiAPIActivity.class.getSimpleName();

    private static final String START_WEAR = "start_wear";
    private static final String STOP_WEAR = "stop_wear";
    private static final String THUMBS = "thumbs";

    private RelativeLayout mainContainer;
    private ViewFlipper viewFlipper;
    private RelativeLayout actionContainer;
    private ToggleButton toggleButton;
    private ImageView sensemojiButton;
    private RelativeLayout countdownConainer;
    private TextView countdown;
    private RelativeLayout resultContainer;
    private TextView emotionResults;
    private TextView emotionResultsText;
    private TextView emotionResultsValue;
    private ImageView emotion;
    private ImageView photo;
    private ImageView share;
    private FloatingActionButton submitEmotion;

    private MoPubView adView;
    private MoPubNative nativeAdView;
    private ViewBinder viewBinder;
    private MoPubStaticNativeAdRenderer nativeAdRenderer;

    private EmotionDetector ed;
    private Camera mCamera;
    private Preview mPreview;

    private GoogleApiClient mApiClient;
    int previewWidth = 0;
    int previewHeight = 0;
    private AtomicInteger timeLeft = new AtomicInteger(3);
    private Subscription subscription;

    private SensemojiMoment moment = new SensemojiMoment();
    private Sensemoji api;

    @Override
    public void onBackPressed() {
        try {
            switch (viewFlipper.getDisplayedChild()) {
                case 0: {
                    finish();
                    break;
                }
                default: {
                    reset();
                }
            }
        } catch(Exception e) {
            Log.e(TAG, "onBackPressed(): exception caught:", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_sensemojisdk);

        FacebookSdk.sdkInitialize(getApplicationContext());

        mainContainer = (RelativeLayout) findViewById(R.id.main_container);
        viewFlipper = (ViewFlipper)findViewById(R.id.view_flipper);

        actionContainer = (RelativeLayout) findViewById(R.id.action_container);
        actionContainer.setOnClickListener(this);
        toggleButton = (ToggleButton) findViewById(R.id.toggle_button);
        toggleButton.setOnCheckedChangeListener(this);
        sensemojiButton = (ImageView) findViewById(R.id.sensemoji_button);
        sensemojiButton.setOnClickListener(this);

        countdownConainer = (RelativeLayout) findViewById(R.id.countdown_container);
        countdown = (TextView) findViewById(R.id.countdown_timer);

        resultContainer = (RelativeLayout)findViewById(R.id.result_container);
        emotion = (ImageView) findViewById(R.id.emotion);
        emotion.setOnClickListener(this);
        photo = (ImageView) findViewById(R.id.photo);
        photo.setOnClickListener(this);
        emotionResults = (TextView)findViewById(R.id.emotion_results);
        emotionResultsText = (TextView)findViewById(R.id.emotion_text);
        emotionResultsValue = (TextView)findViewById(R.id.emotion_value);

        submitEmotion = (FloatingActionButton)findViewById(R.id.submit_emotion);
        submitEmotion.setOnClickListener(this);

        share = (ImageView)findViewById(R.id.share);
        share.setOnClickListener(this);

        nativeAdView = new MoPubNative(this, "", this);
        viewBinder = new ViewBinder.Builder(R.layout.activity_sensemojisdk)
                .mainImageId(R.id.native_adview_main)
                .iconImageId(R.id.native_adview_icon)
                .titleId(R.id.native_adview_title)
                .textId(R.id.native_adview_text)
                .build();
        nativeAdRenderer = new MoPubStaticNativeAdRenderer(viewBinder);
        nativeAdView.registerAdRenderer(nativeAdRenderer);

        ed = new EmotionDetector(this);
        ed.setEmotionCallback(this);
        String apiKey = "";
        try {
            apiKey = getIntent().getStringExtra(Constants.API_KEY);
            ed.setApiKey(apiKey);
        } catch (Exception e) {
            Log.w(TAG, "onCreate(): exception caught retrieving api key:", e);
        }

        adView = (MoPubView)findViewById(R.id.adview);
        adView.setAdUnitId("629f059d8d394b7d9b95ba6efc65bb7a");
        //adView.setAdUnitId("cae225815fcc46698d47da23cdae0e97");
        if((apiKey!=null)&&(apiKey.length()>0)) {
            if((!apiKey.equals("22bc1052-9461-40b1-a53d-a432245d6502"))&& //this is the original TouchPal key
                    (!apiKey.contains("Sample-Sensemoji"))) { //use default adunit for example project
                adView.setAdUnitId(apiKey);
            }
        }
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        try {
            this.api = (Sensemoji)this.getIntent().getExtras().get("sensemoji");
        } catch(Exception e) {
            Log.d(TAG, "onCreate(): exception caught.", e);
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Locate MenuItem with ShareActionProvider
        //MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        //mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        // Return true to display menu
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.init(this, "MSCQTSXRV8YZBBXN8X3N");
        FlurryAgent.onStartSession(this);
        mApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mApiClient.disconnect();
        FlurryAgent.onEndSession(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT>=23) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 28);
            }
        }
        try {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mPreview = new Preview(this, mCamera);
            mainContainer.addView(mPreview, 0);

        } catch(Exception e){
            Log.e(TAG, "onResume(): exception caught:", e);
            finish();
        }
        if(ed.start()==false) {
            Log.e(TAG, "onResume(): error starting detector.");

        }
    }

    @Override
    protected void onPause() {
        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
            mainContainer.removeView(mPreview);
            mPreview = null;
            ed.stop();
            Wearable.MessageApi.removeListener(mApiClient, this);
        } catch(Exception e) {
            Log.e(TAG, "onPaush(): exception caught:", e);
        }
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if ((id==R.id.action_container)||(id==R.id.sensemoji_button)){
            if(toggleButton.isChecked()==false) {
                viewFlipper.setDisplayedChild(1);
                subscription = Observable.interval(1, 1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Long>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onNext(Long aLong) {
                                if (timeLeft.decrementAndGet() <= 0) {
                                    subscription.unsubscribe();
                                    countdownConainer.setVisibility(View.GONE);
                                    timeLeft.set(3);
                                    countdown.setText(timeLeft.toString());
                                    mCamera.takePicture(null, null, mPictureCallback);
                                } else {
                                    countdown.setText(timeLeft.toString());
                                }
                            }
                        });
            }
        } else if(id==R.id.emotion) {
            emotion.setVisibility(View.GONE);
            photo.setVisibility(View.VISIBLE);
        } else if(id==R.id.photo) {
            emotion.setVisibility(View.VISIBLE);
            photo.setVisibility(View.GONE);
        } else if(id==R.id.submit_emotion) {
            try {

                Intent result = new Intent();
                if (this.moment.getEmoji() != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    this.moment.getEmoji().compress(Bitmap.CompressFormat.PNG, 25, stream);
                    byte[] byteArray = stream.toByteArray().clone();
                    result.putExtra("io.affect.sensemojisdk.EMOJI", byteArray);
                }
                //result.putExtra("io.affect.sensemojisdk.PHOTO", this.moment.getPhoto());
                //result.putExtra("io.affect.sensemojisdk.EMOTION", this.moment.getEmotion().toString());
                //result.putExtra("io.affect.sensemojisdk.RATING", this.moment.getRating());
                setResult(Activity.RESULT_OK, result);
                SensemojiBus.bus.post(this.moment);

                finish();
            } catch(Exception e) {
                Log.e(TAG, "onClick(): exception caught.", e);
            }
        } else if(id==R.id.share) {
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(moment.getEmoji())
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();
            ShareDialog.show(this, content);
        }
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.postRotate(270f);
            bm =  Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

            if(ed.process(bm)==false){
                Toast.makeText(SensemojiAPIActivity.this, R.string.process_error,  Toast.LENGTH_LONG).show();
                reset();
            } else {
                photo.setImageBitmap(bm);
                moment.setPhoto(bm);
                viewFlipper.setDisplayedChild(2);
            }
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId()==R.id.toggle_button) {
            String command = STOP_WEAR;
            if(isChecked) {
              command = START_WEAR;
            }
            sendMessage(command);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(THUMBS.equalsIgnoreCase(messageEvent.getPath())) {
            //Toast.makeText(SensemojiAPIActivity.this, String.format("Gesture received: %s", new String(messageEvent.getData())), Toast.LENGTH_LONG).show();
            try {
                float tilt = Float.valueOf(new String(messageEvent.getData()));
                if(tilt>7) {
                    emotion.setImageResource(R.mipmap.thumbs_down);
                } else if(tilt<-7) {
                    emotion.setImageResource(R.mipmap.thumbs_up);
                }
            } catch(Exception e) {
                Log.e(TAG, "onMessageReceieved(): exception caught.", e);
            }
            photo.setImageResource(R.mipmap.sensemoji);
            viewFlipper.setDisplayedChild(2);
            sendMessage(STOP_WEAR);
        }
    }

    private void sendMessage(String command) {
        Observable.just(command)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String command) {
                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                        for(Node node : nodes.getNodes()) {
                            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                    mApiClient, node.getId(), command, null ).await();
                            System.out.println(result);
                        }
                        return command;
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
                    public void onNext(String s) {
                        if(s.equals(START_WEAR)) {
                            Toast.makeText(SensemojiAPIActivity.this, "Express yourself with watch gestures!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onMomentCaptured(SensemojiMoment moment) {
        this.moment = moment;
        emotionResults.setText(getString(R.string.all_done));
        emotionResultsText.setText(getString(R.string.all_done_results));

        String emoji = "neutral";
        String valueString = getString(R.string.neutral);
        switch(moment.getEmotion()) {
            case ANGER: {
                valueString = getString(R.string.anger);
                break;
            } case DISGUST: {
                valueString = getString(R.string.disgust);
                break;
            }
            case FEAR: {
                valueString = getString(R.string.fear);
                break;
            }
            case JOY: {
                valueString = getString(R.string.joy);
                break;
            }
            case SADNESS: {
                valueString = getString(R.string.sadness);
                break;
            }
            case SURPRISE: {
                valueString = getString(R.string.surprise);
                break;
            }
            case UNKNOWN: {
                emotionResults.setText(getString(R.string.oops));
                emotionResultsText.setText(getString(R.string.oops_results));
                emotionResultsValue.setText("");
                break;
            }
        }

        String result = String.format(valueString, moment.getRating());
        emotionResultsValue.setText(result);
        emotion.setImageBitmap(moment.getEmoji());
        adView.loadAd();
        //Specify which native assets you want to use in your ad.
        EnumSet<RequestParameters.NativeAdAsset> assetsSet = EnumSet.of(RequestParameters.NativeAdAsset.TITLE, RequestParameters.NativeAdAsset.TEXT,
                RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT, RequestParameters.NativeAdAsset.MAIN_IMAGE,
                RequestParameters.NativeAdAsset.ICON_IMAGE, RequestParameters.NativeAdAsset.STAR_RATING);

        RequestParameters requestParameters = new RequestParameters.Builder()
//                .keywords("gender:m,age:27")
//                .location(exampleLocation)
                .desiredAssets(assetsSet)
                .build();

        nativeAdView.makeRequest(requestParameters);
    }

    private void reset() {
        try {
            mCamera.reconnect();
            mCamera.startPreview();
        } catch (Exception e) {

        }
        toggleButton.setChecked(false);
        viewFlipper.setDisplayedChild(0);
    }

    @Override
    public void onNativeLoad(NativeAd nativeAd) {

    }

    @Override
    public void onNativeFail(NativeErrorCode errorCode) {

    }
}
