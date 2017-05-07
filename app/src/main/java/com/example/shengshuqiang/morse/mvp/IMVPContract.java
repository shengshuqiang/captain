package com.example.shengshuqiang.morse.mvp;

/**
 * Created by shengshuqiang on 2017/4/29.
 */

public interface IMVPContract {

    interface IMVPView {

        void setMVPPresenter(IMVPPresenter mvpPresenter);

        void show(IMVPViewData data);

    }

    interface IMVPPresenter {

        void setMVPViewAndModule(IMVPView mvpView, IMVPModule mvpModule);

        void start();

        void doAction(IMVPActionData data);

    }

    interface IMVPModule {

        void setMVPPresenter(IMVPPresenter mvpPresenter);

        <DATA> void loadData(IMVPLoadRequest<DATA> loadRequest, IMVPLoaderCallbacks<DATA> loaderCallbacks);

    }

    interface IMVPViewData {

        int getID();

    }

    interface IMVPActionData {

        int getID();

    }

    interface IMVPLoadRequest<DATA> {

        int getID();

        DATA getData();
    }

    interface IMVPLoaderCallbacks<DATA> {

        void onSuccess(DATA data);

        void onError(Exception exception);

    }

}
