package com.shuqiang.captain.qr.data;

import com.shuqiang.captain.qr.mvpmodule.MorseMessageItemData;
import com.shuqiang.captain.qr.widgets.MorseItemDetailInfoView;

/**
 * 摩斯信息单元中间产物
 * 用于加工处理摩斯信息单元
 * <p>
 * Created by shengshuqiang on 2018/3/1.
 */
public class MorseMessageIntermediateItemData {
    public enum STATUS {
        // 读 -> 读、删、改
        READ,
        // 删 -> 删
        DELETE,
        // 增 -> 增
        ADD,
        // 改 -> 改、读、删
        MODIFY;

        public boolean isEditOperateVisibility() {
            boolean visibility = false;

            switch (this) {
                case READ:
                    visibility = true;
                    break;

                case MODIFY:
                    visibility = false;
                    break;

                case ADD:
                    visibility = false;
                    break;
            }

            return visibility;
        }

        public boolean isDelOperateVisibility() {
            if (this == MODIFY) {
                return true;
            }

            return false;
        }

        public boolean isOkOperateVisibility() {
            return true;
        }

        public boolean isCancelOperateVisibility() {
            if (this == MODIFY || this == ADD) {
                return true;
            }

            return false;
        }

        public boolean isUserNameEditable() {
            return (this == ADD);
        }

        public boolean isPasswordEditable() {
            return (this == MODIFY || this == ADD);
        }

        public boolean isRemarksEditable() {
            return (this == MODIFY || this == ADD);
        }
    }

    public STATUS status;
    public MorseMessageItemData itemData;

    public MorseMessageIntermediateItemData(STATUS status) {
        this(status, new MorseMessageItemData());
    }

    public MorseMessageIntermediateItemData(STATUS status, MorseMessageItemData itemData) {
        this.status = status;
        this.itemData = itemData;
    }

    public void bindView(MorseItemDetailInfoView.IBindView bindView) {
        String userName = (null != itemData ? itemData.userName : null);
        bindView.setUserName(userName);

        String password = (null != itemData ? itemData.password : null);
        bindView.setPassword(password);

        String remarks = (null != itemData ? itemData.remarks : null);
        bindView.setRemarks(remarks);

        boolean userNameEditable = status.isUserNameEditable();
        bindView.setUserNameEditable(userNameEditable);

        boolean passwordEditable = status.isPasswordEditable();
        bindView.setPasswrodEditable(passwordEditable);

        boolean remarksEditable = status.isRemarksEditable();
        bindView.setRemarksEditable(remarksEditable);

        boolean editOperateVisibility = status.isEditOperateVisibility();
        bindView.setEditVisible(editOperateVisibility);

        boolean deleteOperateVisibility = status.isDelOperateVisibility();
        bindView.setDeleteVisible(deleteOperateVisibility);

        boolean okOperateVisibility = status.isOkOperateVisibility();
        bindView.setOKVisible(okOperateVisibility);

        boolean cancelOperateVisibility = status.isCancelOperateVisibility();
        bindView.setCancelVisible(cancelOperateVisibility);
    }

    @Override
    public String toString() {
        return "MorseMessageIntermediateItemData { status=" + status + ", itemData=" + itemData + "}";
    }

    public static MorseMessageIntermediateItemData readMorseMessageItemData(MorseMessageItemData itemData) {
        return new MorseMessageIntermediateItemData(STATUS.READ, itemData);
    }

    public static MorseMessageIntermediateItemData addMorseMessageItemData() {
        return new MorseMessageIntermediateItemData(STATUS.ADD);
    }

    public static MorseMessageIntermediateItemData modifyMorseMessageItemData(MorseMessageItemData itemData) {
        return new MorseMessageIntermediateItemData(STATUS.MODIFY, itemData);
    }

}
