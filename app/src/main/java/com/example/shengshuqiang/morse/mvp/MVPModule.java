package com.example.shengshuqiang.morse.mvp;

import android.util.SparseArray;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shengshuqiang on 2017/4/29.
 */

public abstract class MVPModule implements IMVPContract.IMVPModule {

    protected IMVPContract.IMVPPresenter mvpPresenter;

    private ExecutorService executorService;

    private SparseArray<IMVPContract.IMVPLoaderCallbacks> loaderCallbacksSparseArray;

    public MVPModule() {

    }

    @Override
    public void setMVPPresenter(IMVPContract.IMVPPresenter mvpPresenter) {
        this.mvpPresenter = mvpPresenter;
    }

    @Override
    public <DATA> void loadData(final IMVPContract.IMVPLoadRequest<DATA> loadRequest, IMVPContract.IMVPLoaderCallbacks<DATA> loaderCallbacks) {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(5);
        }

        putLoaderCallbacks(loadRequest, loaderCallbacks);

        executorService.execute(new LoaderRequestRunnable(loadRequest));
    }

    private synchronized <DATA> void putLoaderCallbacks(IMVPContract.IMVPLoadRequest<DATA> loadRequest, IMVPContract.IMVPLoaderCallbacks<DATA> loaderCallbacks) {
        if (loaderCallbacksSparseArray == null) {
            loaderCallbacksSparseArray = new SparseArray<>();
        }

        loaderCallbacksSparseArray.put(loadRequest.getID(), loaderCallbacks);
    }

    private synchronized <DATA> IMVPContract.IMVPLoaderCallbacks<DATA> getLoaderCallbacks(IMVPContract.IMVPLoadRequest<DATA> loadRequest) {
        if (loaderCallbacksSparseArray != null) {
            return loaderCallbacksSparseArray.get(loadRequest.getID());
        }

        return null;
    }

    private class LoaderRequestRunnable<DATA> implements Runnable {
        private IMVPContract.IMVPLoadRequest<DATA> loadRequest;

        public LoaderRequestRunnable(IMVPContract.IMVPLoadRequest<DATA> loadRequest) {
            this.loadRequest = loadRequest;
        }

        @Override
        public void run() {
            DATA data = null;
            Exception exception = null;
            try {
                data = loadRequest.getData();
            } catch (Exception ex) {
                exception = ex;
            }

            IMVPContract.IMVPLoaderCallbacks<DATA> loaderCallbacks = getLoaderCallbacks(loadRequest);
            if (loaderCallbacks != null) {
                if (exception != null) {
                    loaderCallbacks.onError(exception);
                } else {
                    loaderCallbacks.onSuccess(data);
                }
            }
        }
    }
}
