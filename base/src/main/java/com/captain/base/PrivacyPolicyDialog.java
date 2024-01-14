package com.captain.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/** 隐私策略弹窗 */
public class PrivacyPolicyDialog extends DialogFragment {

    private DialogInterface.OnDismissListener onDismissListener;
    private DialogInterface.OnDismissListener onArgeeListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public void setOnArgeeListener(DialogInterface.OnDismissListener onArgeeListener) {
        this.onArgeeListener = onArgeeListener;
    }

    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        } catch (Exception e) {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        }
    }

    private static void stripUnderlines(TextView textView) {
        if (textView == null) {
            return;
        }
        Spannable s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        if (spans == null) {
            return;
        }
        for (URLSpan span : spans) {
            if (span == null) {
                continue;
            }
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Permission_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false);
        View windowView = inflater.inflate(R.layout.privacy_popup_window, container, false);
        Activity activity = getActivity();
        Dialog dialog = getDialog();
        Window window;
        if (dialog != null && (window = getDialog().getWindow()) != null) {
            Utils.setSysIconColor(window, false);
            Utils.addFullScreenFlags(window);
            Utils.setStatusBarColor(window, Color.TRANSPARENT);
        }

        if (activity != null) {
            int statusBarHeight = Utils.getStatusBarHeight(activity);
            windowView.setPadding(0, statusBarHeight, 0, 0);
        }
        return windowView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView privacyTextView = view.findViewById(R.id.privacy_policy);
        stripUnderlines(privacyTextView);
        privacyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        TextView privacyDescTextView = view.findViewById(R.id.privacy_policy_desc);
        stripUnderlines(privacyDescTextView);
        privacyDescTextView.setMovementMethod(LinkMovementMethod.getInstance());
        view.findViewById(R.id.permission_agree_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtils.grantPrivacyPermission(getContext());
                if (onArgeeListener != null) {
                    onArgeeListener.onDismiss(getDialog());
                }
                dismissAllowingStateLoss();
            }
        });
        view.findViewById(R.id.permission_disagree_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWarningDialog();
            }
        });
    }

    public void showWarningDialog() {
        FragmentActivity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(activity);
        View v = inflater.inflate(R.layout.privacy_warning_dialog, null);
        TextView disagreeButton = v.findViewById(R.id.disagree);
        TextView agreeButton = v.findViewById(R.id.agree);
        TextView subTitle = v.findViewById(R.id.sub_title);
        subTitle.setMovementMethod(LinkMovementMethod.getInstance());
        Dialog warningDialog = new Dialog(activity);
        warningDialog.addContentView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        warningDialog.setCancelable(false);
        warningDialog.show();

        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warningDialog.cancel();
                if (onArgeeListener != null) {
                    onArgeeListener.onDismiss(getDialog());
                }
                dismissAllowingStateLoss();
            }
        });
        disagreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warningDialog.cancel();
                if (onDismissListener != null) {
                    onDismissListener.onDismiss(getDialog());
                }
                dismissAllowingStateLoss();
            }
        });
        Window window = warningDialog.getWindow();
        window.setBackgroundDrawable(new BitmapDrawable());
    }

    private static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }
}
