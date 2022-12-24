package com.shuqiang.captain.qr.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.shuqiang.captain.qr.data.MorseMessageIntermediateItemData;
import com.shuqiang.captain.qr.mvpmodule.MorseMessageData;
import com.shuqiang.captain.qr.mvppresenter.MorseMVPPresenter;
import com.shuqiang.captain.qr.utils.Utils;

import captain.R;

/**
 * Created by shengshuqiang on 2018/2/20.
 */
public class MorseItemDetailInfoView extends LinearLayout {
    private View editView;
    private View deleteView;
    private View okView;
    private View cancelView;
    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText remarksEditTxt;

    private OnDetailInfoClick onDetailInfoClick;
    private MorseMessageIntermediateItemData intermediateItemData;

    public MorseItemDetailInfoView(Context context) {
        super(context);

        init(context);
    }

    public MorseItemDetailInfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(Color.WHITE);
        setClickable(true);
        inflate(context, R.layout.normal_item_detail_info_view, this);

        editView = findViewById(R.id.edit);
        deleteView = findViewById(R.id.delete);
        okView = findViewById(R.id.ok);
        cancelView = findViewById(R.id.cancel);
        userNameEditText = (EditText) findViewById(R.id.user_name);
        passwordEditText = (EditText) findViewById(R.id.password);
        remarksEditTxt = (EditText) findViewById(R.id.remarks);

        editView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                intermediateItemData.status = MorseMessageIntermediateItemData.STATUS.MODIFY;
                setData(intermediateItemData);

                if (onDetailInfoClick != null) {
                    onDetailInfoClick.onEditClick(MorseItemDetailInfoView.this, intermediateItemData);
                }
            }
        });

        deleteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(userNameEditText.getText().toString())) {
                    MorseMVPPresenter.deleteItemConfirm(getContext(), intermediateItemData.itemData, new MorseMVPPresenter.AddConfirmCallBack() {
                        @Override
                        public void finish(boolean confirmed) {
                            if (confirmed && onDetailInfoClick != null) {
                                intermediateItemData.status = MorseMessageIntermediateItemData.STATUS.DELETE;
                                onDetailInfoClick.onDelClick(MorseItemDetailInfoView.this, intermediateItemData);
                            }
                        }
                    });
                } else {
                    userNameConfirm();
                }
            }
        });

        okView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = userNameEditText.getText().toString();
                if (!TextUtils.isEmpty(userName)) {
                    intermediateItemData.itemData.userName = MorseMessageData.cleanInvalidString(userName);
                    String password = passwordEditText.getText().toString();
                    intermediateItemData.itemData.password = MorseMessageData.cleanInvalidString(password);
                    String remarks = remarksEditTxt.getText().toString();
                    intermediateItemData.itemData.remarks = MorseMessageData.cleanInvalidString(remarks);

                    if (!userName.equals(intermediateItemData.itemData.userName)
                            || !password.equals(intermediateItemData.itemData.password)
                            || !remarks.equals(intermediateItemData.itemData.remarks)) {
                        Utils.showMessage(MorseItemDetailInfoView.this, "清洗掉非法换行符");
                    }

                    if (onDetailInfoClick != null) {
                        onDetailInfoClick.onOKClick(MorseItemDetailInfoView.this, intermediateItemData);
                    }
                } else {
                    userNameConfirm();
                }
            }
        });

        cancelView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                intermediateItemData.status = MorseMessageIntermediateItemData.STATUS.READ;

                if (onDetailInfoClick != null) {
                    onDetailInfoClick.onCancelClick(MorseItemDetailInfoView.this, intermediateItemData);
                }
            }
        });
    }



    private void userNameConfirm() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getContext());
        normalDialog.setIcon(android.R.drawable.ic_dialog_alert);
        normalDialog.setTitle("账号无效");
        normalDialog.setMessage("账号必须非空");
        // 显示
        normalDialog.show();
    }

    public void setOnDetailInfoClick(OnDetailInfoClick onDetailInfoClick) {
        this.onDetailInfoClick = onDetailInfoClick;
    }

    public void setData(MorseMessageIntermediateItemData intermediateItemData) {
        this.intermediateItemData = intermediateItemData;

        intermediateItemData.bindView(new IBindView() {
            @Override
            public void setUserName(String userName) {
                userNameEditText.setText(userName);
            }

            @Override
            public void setPassword(String password) {
                passwordEditText.setText(password);
            }

            @Override
            public void setRemarks(String remarks) {
                remarksEditTxt.setText(remarks);
            }

            @Override
            public void setUserNameEditable(boolean editable) {
                userNameEditText.setEnabled(editable);
            }

            @Override
            public void setPasswrodEditable(boolean editable) {
                passwordEditText.setEnabled(editable);
            }

            @Override
            public void setRemarksEditable(boolean editable) {
                remarksEditTxt.setEnabled(editable);
            }

            @Override
            public void setEditVisible(boolean visible) {
                editView.setVisibility(visible ? VISIBLE : GONE);
            }

            @Override
            public void setDeleteVisible(boolean visible) {
                deleteView.setVisibility(visible ? VISIBLE : GONE);
            }

            @Override
            public void setOKVisible(boolean visible) {
                okView.setVisibility(visible ? VISIBLE : GONE);
            }

            @Override
            public void setCancelVisible(boolean visible) {
                cancelView.setVisibility(visible ? VISIBLE : GONE);
            }
        });
    }

    public MorseMessageIntermediateItemData getIntermediateItemData() {
        return intermediateItemData;
    }

    public interface OnDetailInfoClick {
        void onEditClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageIntermediateItemData data);
        void onDelClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageIntermediateItemData data);
        void onOKClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageIntermediateItemData data);
        void onCancelClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageIntermediateItemData data);
    }

    public interface IBindView {
        void setUserName(String userName);
        void setPassword(String password);
        void setRemarks(String remarks);
        void setUserNameEditable(boolean editable);
        void setPasswrodEditable(boolean editable);
        void setRemarksEditable(boolean editable);
        void setEditVisible(boolean visible);
        void setDeleteVisible(boolean visible);
        void setOKVisible(boolean visible);
        void setCancelVisible(boolean visible);
    }
}
