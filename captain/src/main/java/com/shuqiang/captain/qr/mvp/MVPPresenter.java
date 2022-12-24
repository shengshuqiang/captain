package com.shuqiang.captain.qr.mvp;

/**
 * Created by shengshuqiang on 2017/4/29.
 */

public abstract class MVPPresenter implements IMVPContract.IMVPPresenter {

    protected IMVPContract.IMVPView mvpView;
    protected IMVPContract.IMVPModule mvpModule;

    @Override
    public void setMVPViewAndModule(IMVPContract.IMVPView mvpView, IMVPContract.IMVPModule mvpModule) {
        this.mvpView = mvpView;
        this.mvpModule = mvpModule;
    }
}
