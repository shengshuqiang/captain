package com.example.shengshuqiang.morse.data;

import com.example.shengshuqiang.morse.mvpmodule.MorseMessageItemData;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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

        public int getEditOperateVisibility() {
            int visibility = GONE;

            switch (this) {
                case READ:
                    visibility = VISIBLE;
                    break;

                case MODIFY:
                    visibility = GONE;
                    break;

                case ADD:
                    visibility = GONE;
                    break;
            }

            return visibility;
        }

        public int getDelOperateVisibility() {
            if (this == MODIFY) {
                return VISIBLE;
            }

            return GONE;
        }

        public int getOkOperateVisibility() {
            return VISIBLE;
        }

        public int getCancelOperateVisibility() {
            if (this == MODIFY || this == ADD) {
                return VISIBLE;
            }

            return GONE;
        }

        public boolean isUserNameEditable() {
            return (this == ADD);
        }

        public boolean isPasswordEditable() {
            return (this == MODIFY || this == ADD);
        }

        public boolean isDescEditable() {
            return (this == MODIFY || this == ADD);
        }
    }

    public STATUS status;
    public MorseMessageItemData itemData;

    public MorseMessageIntermediateItemData(STATUS status) {
        this(status, null);
    }

    public MorseMessageIntermediateItemData(STATUS status, MorseMessageItemData itemData) {
        this.status = status;
        this.itemData = itemData;
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
