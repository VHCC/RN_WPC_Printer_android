package com.advantech.wpcprinter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
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
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.rt.printerlibrary.bean.Position;
import com.rt.printerlibrary.bean.UsbConfigBean;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.enumerate.SettingEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.factory.connect.PIFactory;
import com.rt.printerlibrary.factory.connect.UsbFactory;
import com.rt.printerlibrary.factory.printer.PrinterFactory;
import com.rt.printerlibrary.factory.printer.UniversalPrinterFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.setting.TextSetting;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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

    public static WpcButtonView mWpcButtonView;

    // Printer
    public static RTPrinter rtPrinter = null;
    private PrinterFactory printerFactory = new UniversalPrinterFactory();


    // LifecycleEventListener
    private WeakReference<ViewGroup> RNLayoutRef;

    public static final String REACT_CLASS = "WpcButton";

    public WpcButtonView(ReactApplicationContext reactContext) {
        super();
//        receiver = createOrientationReceiver();
        reactContext.addLifecycleEventListener(this);
    }

    public static WpcButtonView getInstance() {
        return mWpcButtonView;
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

//                if (rtPrinter != null) {
//                    CmdFactory cmdFactory = new EscFactory();
//                    Cmd cmd = cmdFactory.create();
//                    cmd.append(cmd.getBeepCmd());
//                    rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//                }

//                if (rtPrinter != null) {
//                    try {
//                        textPrint();
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    } catch (SdkException e) {
//                        e.printStackTrace();
//                    }
//                }

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
        mWpcButtonView = this;
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


    private String mChartsetName = "UTF-8";
    private TextSetting textSetting = new TextSetting();
    private ESCFontTypeEnum curESCFontType = null;

    private void escPrintBold(String textString) throws UnsupportedEncodingException {
        mLog.d(TAG, "escPrintBold");
        if (rtPrinter != null) {
            CmdFactory escFac = new EscFactory();
            Cmd escCmd = escFac.create();
            escCmd.append(escCmd.getHeaderCmd());//初始化, Initial

            escCmd.setChartsetName(mChartsetName);
            textSetting.setEscFontType(curESCFontType);
            textSetting.setBold(SettingEnum.Enable);
            textSetting.setCpclFontSize(48);
            textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);

            escCmd.append(escCmd.getTextCmd(textSetting, textString));

            escCmd.append(escCmd.getLFCRCmd());
//            escCmd.append(escCmd.getLFCRCmd());
//            escCmd.append(escCmd.getLFCRCmd());
//            escCmd.append(escCmd.getLFCRCmd());
//            escCmd.append(escCmd.getLFCRCmd());
//            escCmd.append(escCmd.getHeaderCmd());//初始化, Initial
//            escCmd.append(escCmd.getLFCRCmd());

            rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
        }
    }

    private synchronized void escPrint(final ReadableMap printInfo) throws UnsupportedEncodingException {
        mLog.d(TAG, "escPrint");
        if (rtPrinter != null) {
            CmdFactory escFac = new EscFactory();
            Cmd escCmd = escFac.create();
            escCmd.append(escCmd.getHeaderCmd());//初始化, Initial

            escCmd.setChartsetName(mChartsetName);
            textSetting.setEscFontType(curESCFontType);
            textSetting.setBold(SettingEnum.Disable);
            textSetting.setCpclFontSize(14);
            textSetting.setAlign(CommonEnum.ALIGN_LEFT);

            String line_2 = "Guest Name: " + printInfo.getString("userName");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.TAIWAN);
            String currentDateandTime = sdf.format(new Date());
            String line_2_1 = currentDateandTime;
            String line_3 = "------------------------------------------------";
            String line_4 = printInfo.getString("productName");
            String line_5 = "------------------------------------------------";

            escCmd.append(escCmd.getTextCmd(textSetting, line_2));
            escCmd.append(escCmd.getLFCRCmd());

            escCmd.append(escCmd.getTextCmd(textSetting, line_2_1));
            escCmd.append(escCmd.getLFCRCmd());

            escCmd.append(escCmd.getTextCmd(textSetting, line_3));
            escCmd.append(escCmd.getLFCRCmd());

            escCmd.append(escCmd.getTextCmd(textSetting, line_4));
            escCmd.append(escCmd.getLFCRCmd());

            escCmd.append(escCmd.getTextCmd(textSetting, line_5));
            escCmd.append(escCmd.getLFCRCmd());

            rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
        }
    }

    private int bmpPrintWidth = 120;

    private void escPrintImage(final boolean isBonus) throws SdkException {

        new Thread(new Runnable() {
            @Override
            public void run() {

//                showProgressDialog("Loading...");

                CmdFactory cmdFactory = new EscFactory();
                Cmd cmd = cmdFactory.create();
                cmd.append(cmd.getHeaderCmd());

                CommonSetting commonSetting = new CommonSetting();
                commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
                cmd.append(cmd.getCommonSettingCmd(commonSetting));

                BitmapSetting bitmapSetting = new BitmapSetting();

                /**
                 * MODE_MULTI_COLOR - 适合多阶灰度打印<br/> Suitable for multi-level grayscale printing<br/>
                 * MODE_SINGLE_COLOR-适合白纸黑字打印<br/>Suitable for printing black and white paper
                 */
                bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_MULTI_COLOR);


                bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);
                Bitmap bm = BitmapFactory.decodeResource(
                        ActivityUtils.getTopActivity().getResources(), isBonus ? R.mipmap.wpc_face_true : R.mipmap.wpc_face_false);

                try {
                    cmd.append(cmd.getBitmapCmd(bitmapSetting, bm));
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getHeaderCmd());//初始化, Initial
                cmd.append(cmd.getLFCRCmd());
                if (rtPrinter != null) {
                    rtPrinter.writeMsg(cmd.getAppendCmds());//Sync Write
                }
                allCutTest();
            }
        }).start();


        //将指令保存到bin文件中，路径地址为sd卡根目录
//        final byte[] btToFile = cmd.getAppendCmds();
//        TonyUtils.createFileWithByte(btToFile, "Esc_imageCmd.bin");
//        TonyUtils.saveFile(FuncUtils.ByteArrToHex(btToFile), "Esc_imageHex");

    }

    public void printItem(final ReadableMap printInfo) {
        mLog.d(TAG, printInfo.getString("orderNumber"));
        mLog.d(TAG, printInfo.getString("userName"));
        mLog.d(TAG, printInfo.getString("productName"));
        mLog.d(TAG, "" + printInfo.getBoolean("isBonus"));
        if (rtPrinter != null) {

            String line_1 = "Order <" + printInfo.getString("orderNumber") + ">";
//            String line_2 = "Guest Name: " + printInfo.getString("userName");
//            String line_2_1 = currentDateandTime;
//            String line_3 = "------------------------------------------------";
//            String line_4 = printInfo.getString("productName");
//            String line_5 = "------------------------------------------------";
            try {
                escPrintBold(line_1);
//                escPrint(line_2);
//                escPrint(line_2_1);
//                escPrint(line_3);
//                escPrint(line_4);
//                escPrint(line_5);
                escPrint(printInfo);
                escPrintImage(printInfo.getBoolean("isBonus"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (SdkException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 全切测试
     */
    private void allCutTest() {
        if (rtPrinter != null) {
            CmdFactory cmdFactory = new EscFactory();
            Cmd cmd = cmdFactory.create();
            cmd.append(cmd.getAllCutCmd());
            rtPrinter.writeMsgAsync(cmd.getAppendCmds());
        }
    }

}

