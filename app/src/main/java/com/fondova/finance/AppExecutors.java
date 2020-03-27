package com.fondova.finance;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
@Singleton
public class AppExecutors {

    private final Executor networkIO;

    private final Executor mainThread;

    private final Executor dataThread;

    private final Executor syncThread;

    private AppExecutors(Executor networkIO, Executor mainThread, Executor dataThread, Executor syncThread) {
        this.networkIO = networkIO;
        this.mainThread = mainThread;
        this.dataThread = dataThread;
        this.syncThread = syncThread;
    }

    @Inject
    public AppExecutors() {
        this(Executors.newSingleThreadExecutor(),
                new MainThreadExecutor(), Executors.newSingleThreadExecutor(),
                Executors.newSingleThreadExecutor());
    }

    public Executor networkIO() {
        return networkIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public Executor dataThread() {
        return dataThread;
    }

    public Executor syncThread() {
        return syncThread;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
