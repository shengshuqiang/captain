package com.shuqiang.captain.mvppresenter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;

import com.shuqiang.captain.data.MorseMessageIntermediateItemData;
import com.shuqiang.captain.mvp.IMVPContract;
import com.shuqiang.captain.mvp.MVPPresenter;
import com.shuqiang.captain.mvpmodule.MorseMessageData;
import com.shuqiang.captain.mvpmodule.MorseMessageItemData;
import com.shuqiang.captain.mvpview.MorseMVPAdapter;
import com.shuqiang.captain.mvpview.MorseMVPListData;
import com.shuqiang.captain.widgets.MorseItemDetailInfoPopupWindow;

import java.util.Arrays;
import java.util.List;

/**
 * Created by shengshuqiang on 2017/4/29.
 */

public class MorseMVPPresenter extends MVPPresenter {
    // 点击
    public static final int CLICK_ITEM_ACTION_ID = 0;
    // 修改
    public static final int MODIFY_ITEM_ACTION_ID = 1;
    // 新增
    public static final int ADD_ITEM_ACTION_ID = 2;
    // 删除
    public static final int DELETE_ITEM_ACTION_ID = 3;

    private Context context;
    private MorseMessageData morseMessageData;
    private MorseItemDetailInfoPopupWindow itemDetailInfoPopupWindow;

    public MorseMVPPresenter(Context context) {
        this.context = context;

        morseMessageData = new MorseMessageData();
    }

    public void start() {
    }

    public void handleMorseMessageData(MorseMessageData morseMessageData) {
        List<MorseMessageItemData> itemList = (morseMessageData != null ? morseMessageData.getItemDataList() : null);
        new SameUserNameConfirmReplace(context, MorseMVPPresenter.this.morseMessageData).addListConfirmReplace(itemList, new AddConfirmCallBack() {
            @Override
            public void finish(boolean confirmed) {
                List<MorseMVPAdapter.IMorseMVPAdapterItemData> list = MorseMVPPresenter.this.morseMessageData.getAdapterItemList();
                MorseMVPListData data = new MorseMVPListData(list);
                mvpView.show(data);
            }
        });
    }

    @Override
    public void doAction(IMVPContract.IMVPActionData data) {
        int id = data.getID();
        final MorseMessageItemData itemData = ((MorseMessageItemActionData) data).getItemData();
        switch (id) {
            case CLICK_ITEM_ACTION_ID:
                showItemDetailInfoPopupWindow(MorseMessageIntermediateItemData.readMorseMessageItemData(itemData));
                break;

            case MODIFY_ITEM_ACTION_ID:
                showItemDetailInfoPopupWindow(MorseMessageIntermediateItemData.modifyMorseMessageItemData(itemData));
                break;

            case ADD_ITEM_ACTION_ID:
                showItemDetailInfoPopupWindow(MorseMessageIntermediateItemData.addMorseMessageItemData());
                break;

            case DELETE_ITEM_ACTION_ID:
                deleteItemConfirm(context, itemData, new AddConfirmCallBack() {
                    @Override
                    public void finish(boolean confirmed) {
                        if (confirmed) {
                            morseMessageData.removeItemData(itemData);
                            mvpView.show(new MorseMVPListData(morseMessageData.getAdapterItemList()));
                        }
                    }
                });
                break;

            default:
                break;
        }
    }

    public MorseMessageData getMorseMessageData() {
        return morseMessageData;
    }

    public static void deleteItemConfirm(Context context, final MorseMessageItemData itemData, final AddConfirmCallBack addConfirmCallBack) {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(context);
        normalDialog.setIcon(android.R.drawable.ic_dialog_alert);
        normalDialog.setTitle("删除");
        normalDialog.setMessage("确认删除" + itemData.userName + "?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addConfirmCallBack.finish(true);
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                        addConfirmCallBack.finish(false);
                    }
                });
        // 显示
        normalDialog.show();
    }

    private void showItemDetailInfoPopupWindow(MorseMessageIntermediateItemData intermediateItemData) {
        if (itemDetailInfoPopupWindow == null) {
            itemDetailInfoPopupWindow = new MorseItemDetailInfoPopupWindow(context);
            itemDetailInfoPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    boolean validClose = itemDetailInfoPopupWindow.isValidClose();
                    if (validClose) {
                        MorseMessageIntermediateItemData morseMessageIntermediateItemData = itemDetailInfoPopupWindow.getIntermediateItemData();
                        MorseMessageItemData morseMessageItemData = morseMessageIntermediateItemData.itemData;
                        switch (morseMessageIntermediateItemData.status) {
                            case READ:
                                // do nothing
                                break;

                            case MODIFY:
                                morseMessageData.changeItemData(morseMessageItemData);
                                mvpView.show(new MorseMVPListData(morseMessageData.getAdapterItemList()));
                                break;

                            case ADD:
                                new SameUserNameConfirmReplace(context, morseMessageData).addItemConfirmReplace(morseMessageItemData, new AddConfirmCallBack() {
                                    @Override
                                    public void finish(boolean confirmed) {
                                        mvpView.show(new MorseMVPListData(morseMessageData.getAdapterItemList()));
                                    }
                                });
                                break;

                            case DELETE:
                                morseMessageData.removeItemData(morseMessageItemData);
                                mvpView.show(new MorseMVPListData(morseMessageData.getAdapterItemList()));
                                break;
                        }

                        Log.e("SSU", "MorseMVPPresenter#showItemDetailInfoPopupWindow#onDismiss: morseMessageIntermediateItemData=" + morseMessageIntermediateItemData);
                    }
                    Log.e("SSU", "MorseMVPPresenter#showItemDetailInfoPopupWindow#onDismiss: validClose=" + validClose);
                }
            });
        }

        itemDetailInfoPopupWindow.setData(intermediateItemData);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                itemDetailInfoPopupWindow.show((View) mvpView);
            }
        }, 300);
    }

    public interface AddConfirmCallBack {
        void finish(boolean confirmed);
    }

    private static class SameUserNameConfirmReplace {
        private Context context;
        private MorseMessageData morseMessageData;

        public SameUserNameConfirmReplace(Context context, MorseMessageData morseMessageData) {
            this.context = context;
            this.morseMessageData = morseMessageData;
        }

        public void addItemConfirmReplace(final MorseMessageItemData itemData, final AddConfirmCallBack addConfirmCallBack) {
            MorseMessageItemData messageItemData = morseMessageData.get(itemData.userName);
            Log.e("SSU", "addItemConfirmReplace: itemData=" + itemData + ", get messageItemData=" + messageItemData);

            if (messageItemData != null) {
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(context);
                normalDialog.setIcon(android.R.drawable.ic_dialog_alert);
                normalDialog.setTitle("替换");
                normalDialog.setMessage("新增信息项用户名" + itemData.userName + "已存在，确定要替换?");
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                morseMessageData.addItemData(itemData);
                                addConfirmCallBack.finish(true);
                            }
                        });
                normalDialog.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                                addConfirmCallBack.finish(false);
                            }
                        });
                // 显示
                normalDialog.show();
            } else {
                morseMessageData.addItemData(itemData);
                addConfirmCallBack.finish(true);
            }
        }

        public void addListConfirmReplace(final List<MorseMessageItemData> list, final AddConfirmCallBack addConfirmCallBack) {
            int size = (list != null ? list.size() : 0);
            Log.e("SSU", "addListConfirmReplace: list=" +  Arrays.toString(list.toArray()));
            if (size > 0) {
                addItemConfirmReplace(list.get(0), new AddConfirmCallBack() {
                    @Override
                    public void finish(boolean confirmed) {
                        Log.e("SSU", "addListConfirmReplace#finish: confirmed=" + confirmed + ", list=" +  Arrays.toString(list.toArray()));
                        if (confirmed) {
                            morseMessageData.addItemData(list.get(0));
                        }
                        list.remove(0);
                        addListConfirmReplace(list, addConfirmCallBack);
                    }
                });
            } else {
                addConfirmCallBack.finish(true);
            }
        }
    }
}
