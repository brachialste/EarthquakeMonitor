package com.brachialste.earthquakemonitor.data;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by brachialste on 3/03/15.
 */
public class StaticDataHandlerFactory {

    public static StaticHandler create(IStaticDataHandler ref) {
        return new StaticHandler(ref);
    }

    // This has to be nested.
    static class StaticHandler extends Handler {
        WeakReference<IStaticDataHandler> weakRef;

        public StaticHandler(IStaticDataHandler ref) {
            this.weakRef = new WeakReference<IStaticDataHandler>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            if (weakRef.get() == null) {
                throw new RuntimeException("Something goes wrong.");
            } else {
                weakRef.get().handleMessage(msg);
            }
        }
    }
}
