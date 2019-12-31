package com.advantech.wpcprinter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IChen.Chu on 31/12/2019.
 */

public class WpcMethods {

    interface onPrinterCMDSend {
        void onComplete();

        void onFail(String err);
    }

}
