package com.advantech.wpcprinter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.advantech.wpcprinter.util.MLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.lang.ref.WeakReference;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by IChen.Chu on 31/12/2019.
 */

public class WpcButtonView extends SimpleViewManager<FrameLayout> implements LifecycleEventListener {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // LifecycleEventListener
    private WeakReference<ViewGroup> RNLayoutRef;

    public static final String REACT_CLASS = "WpcButton";

    public WpcButtonView(ReactApplicationContext reactContext) {
        super();
//        receiver = createOrientationReceiver();
        reactContext.addLifecycleEventListener(this);
    }

    // extends SimpleViewManager<FrameLayout>
    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    // extends SimpleViewManager<FrameLayout>
    @NonNull
    @Override
    protected FrameLayout createViewInstance(final @NonNull ThemedReactContext reactContext) {

        LayoutInflater inflater = LayoutInflater.from(reactContext);
        final FrameLayout wpc_button_layout = (FrameLayout) inflater.inflate(R.layout.wpc_button_layout, null);
        Button wpc_button_view = (Button) wpc_button_layout.findViewById(R.id.wpc_button_view);
        wpc_button_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLog.d(TAG, "WPC Button onClick");


                // Native callBack to => RN
                ReactContext context = reactContext;
                WritableMap event = Arguments.createMap();
                event.putString("result", "click success");
                context.getJSModule(RCTEventEmitter.class).receiveEvent(wpc_button_layout.getId(),
                        "wpcButtonClicked", event);

            }
        });


        return wpc_button_layout;
    }

    // implements LifecycleEventListener
    @Override
    public void onHostResume() {
        mLog.d(TAG, " * onHostResume * ");
        if (RNLayoutRef == null) return;
        ViewGroup layout = RNLayoutRef.get();
        if (layout == null) return;
    }

    // implements LifecycleEventListener
    @Override
    public void onHostPause() {
        mLog.d(TAG, "onHostPause");
        if (RNLayoutRef == null) return;
        ViewGroup layout = RNLayoutRef.get();
        if (layout == null) return;
//        unregisterReceiver(layout.getContext());
//        SettingsCamera camera = (SettingsCamera) layout.findViewById(R.id.camera_view);
//        camera.disableView();

    }

    // implements LifecycleEventListener
    @Override
    public void onHostDestroy() {
        mLog.d(TAG, "onHostDestroy");
        if (RNLayoutRef == null) return;
        ViewGroup layout = RNLayoutRef.get();
        if (layout == null) return;
//        unregisterReceiver(layout.getContext());
    }

    @Nullable
    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
                "trainCompleted",
                MapBuilder.of("registrationName", "onTrained"),
                "TrainUncompleted",
                MapBuilder.of("registrationName", "onUntrained"),
                "recognizeCompleted",
                MapBuilder.of("registrationName", "onRecognized"),
                "recognizeUncompleted",
                MapBuilder.of("registrationName", "onUnrecognized"),
                "faceCapturedCompleted",
                MapBuilder.of("registrationName", "onFaceCaptured"),
                "wpcButtonClicked",
                MapBuilder.of("registrationName", "onWpcButtonClicked")
        );
    }
}
