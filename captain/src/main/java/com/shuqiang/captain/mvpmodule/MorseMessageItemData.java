package com.shuqiang.captain.mvpmodule;

/**
 * 摩斯信息单元
 *
 * Created by shengshuqiang on 2017/4/30.
 */
public class MorseMessageItemData {
    // 用户名(唯一标识符)
    public String userName;
    // 用户名
    public String password;
    // 备注
    public String remarks;

    public MorseMessageItemData() {
    }

    public MorseMessageItemData(String userName, String password, String remarks) {
        this.userName = userName;
        this.password = password;
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "MorseMessageItemData { userName=" + userName + ", password=" + password + ", remarks=" + remarks + "}";
    }
}

