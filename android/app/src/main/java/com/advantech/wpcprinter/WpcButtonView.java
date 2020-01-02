package com.advantech.wpcprinter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.advantech.wpcprinter.util.MLog;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.rt.printerlibrary.bean.UsbConfigBean;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.factory.connect.PIFactory;
import com.rt.printerlibrary.factory.connect.UsbFactory;
import com.rt.printerlibrary.factory.printer.PrinterFactory;
import com.rt.printerlibrary.factory.printer.UniversalPrinterFactory;
import com.rt.printerlibrary.printer.RTPrinter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by IChen.Chu on 31/12/2019.
 */

public class WpcButtonView extends SimpleViewManager<FrameLayout> implements LifecycleEventListener {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // USB
    private UsbManager mUsbManager;

    // Printer
    private RTPrinter rtPrinter = null;
    private PrinterFactory printerFactory = new UniversalPrinterFactory();


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

    private UsbDevice mUsbDevice = null;
    private Object configObj;

    // extends SimpleViewManager<FrameLayout>
    @NonNull
    @Override
    protected FrameLayout createViewInstance(final @NonNull ThemedReactContext reactContext) {

        LayoutInflater inflater = LayoutInflater.from(reactContext);
        final FrameLayout wpc_button_layout = (FrameLayout) inflater.inflate(R.layout.wpc_button_layout, null);
        Button wpc_button_view = (Button) wpc_button_layout.findViewById(R.id.wpc_button_view);
        Button wpc_connect_printer = (Button) wpc_button_layout.findViewById(R.id.wpc_connect_printer);
        wpc_button_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLog.d(TAG, "WPC Button onClick");

                if (rtPrinter != null) {
                    CmdFactory cmdFactory = new EscFactory();
                    Cmd cmd = cmdFactory.create();
                    cmd.append(cmd.getBeepCmd());
                    rtPrinter.writeMsgAsync(cmd.getAppendCmds());
                }

                // Native callBack to => RN
                ReactContext context = reactContext;
                WritableMap event = Arguments.createMap();
                event.putString("result", "click success");
                context.getJSModule(RCTEventEmitter.class).receiveEvent(wpc_button_layout.getId(),
                        "wpcButtonClicked", event);

            }
        });


        wpc_connect_printer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsbManager = (UsbManager) ActivityUtils.getTopActivity().getApplicationContext().getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                mLog.d(TAG, "deviceList size = " + deviceList.size());
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    mLog.d(TAG, " - device getDeviceName= " + device.getDeviceName());
                    mLog.d(TAG, " - device getVendorId= " + device.getVendorId());
                    mLog.d(TAG, " - device getProductId= " + device.getProductId());
                    mLog.d(TAG, " ------- ");
                    if (device.getVendorId() == 4070) {
//                        mList.add(device);
                        mUsbDevice = device;

                        mLog.d(TAG, "AAA");
                        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
                                ActivityUtils.getTopActivity(),
                                0,
                                new Intent(ActivityUtils.getTopActivity().getApplicationInfo().packageName),
                                0);
                        mLog.d(TAG, "BBB");
                        configObj = new UsbConfigBean(ActivityUtils.getTopActivity().getApplicationContext(),
                                mUsbDevice, mPermissionIntent);

                        mLog.d(TAG, "CCC");
                        UsbConfigBean usbConfigBean = (UsbConfigBean) configObj;
                        mLog.d(TAG, "DDD");
                        connectUSB(usbConfigBean);
                    }
                }
            }
        });

        rtPrinter = printerFactory.create();

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




    private void connectUSB(UsbConfigBean usbConfigBean) {

        mLog.d(TAG, "connectUSB");

        UsbManager mUsbManager = (UsbManager) ActivityUtils.getTopActivity().getApplicationContext()
                .getSystemService(Context.USB_SERVICE);
        PIFactory piFactory = new UsbFactory();
        PrinterInterface printerInterface = piFactory.create();
        printerInterface.setConfigObject(usbConfigBean);
        rtPrinter.setPrinterInterface(printerInterface);

        if (mUsbManager.hasPermission(usbConfigBean.usbDevice)) {
            try {
                rtPrinter.connect(usbConfigBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mUsbManager.requestPermission(usbConfigBean.usbDevice, usbConfigBean.pendingIntent);
        }

    }
}
