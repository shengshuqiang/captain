package com.example.shengshuqiang.morse.mvpmodule;

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

    public MorseMessageItemData(String userName, String password, String remarks) {
        this.userName = userName;
        this.password = password;
        this.remarks = remarks;
    }
}

