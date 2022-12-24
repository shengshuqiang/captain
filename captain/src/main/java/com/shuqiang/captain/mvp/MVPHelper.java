package com.shuqiang.captain.mvp;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class MVPHelper {

    public static void init(IMVPContract.IMVPView mvpView, IMVPContract.IMVPPresenter mvpPresenter, IMVPContract.IMVPModule mvpModule) {
        mvpView.setMVPPresenter(mvpPresenter);
        mvpPresenter.setMVPViewAndModule(mvpView, mvpModule);
        mvpModule.setMVPPresenter(mvpPresenter);

//        mvpPresenter.start();
    }
}
