package com.captain.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.KeyEvent;
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
    // 提示确认弹窗
    private Dialog warningDialog;

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
        View windowView = inflater.inflate(R.layout.privacy_popup_window, container, false);
        Activity activity = getActivity();
        Dialog dialog = getDialog();
        dialog.setCancelable(true);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                Log.d("SSU", "warningDialog.setOnKeyListener " + keyCode + event);
                if(keyCode == KeyEvent.KEYCODE_BACK){
                    showWarningDialog();
                    return true;
                }
                return false;
            }
        });
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

    /**
     * 背景：单机申诉问题已为您核实：断网后，隐私政策无法打开。建议您参考审核意见，完成app备案后再进行提审，以您后续提审的结果为准，谢谢。
     * 方案：那我把隐私政策写在本地吧！
     * 拦截超链接
     * @param textview
     */
    private void interceptHyperLink(TextView textview) {
        textview.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = textview.getText();
        if (text instanceof Spannable) {
            int end = text.length();
            Spannable spannable = (Spannable) textview.getText();
            URLSpan[] urlSpans = spannable.getSpans(0, end, URLSpan.class);
            if (urlSpans.length == 0) {
                return;
            }

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
            // 循环遍历并拦截 所有http://开头的链接
            for (URLSpan uri : urlSpans) {
                String url = uri.getURL();
                // 兼容 https:// 和 http://
                if (url.startsWith("http")) {
                    CustomUrlSpan customUrlSpan = new CustomUrlSpan(textview.getContext(), url);
                    spannableStringBuilder.setSpan(customUrlSpan, spannable.getSpanStart(uri),
                            spannable.getSpanEnd(uri), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            textview.setText(spannableStringBuilder);
        }
    }
    private static class CustomUrlSpan extends ClickableSpan {

        private Context context;
        private String url;
        public CustomUrlSpan(Context context,String url){
            this.context = context;
            this.url = url;
        }

        @Override
        public void onClick(View widget) {
            Intent intent = new Intent(context,WebViewActivity.class);
            intent.putExtra(WebViewActivity.URL_KEY, url);
            context.startActivity(intent);
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView privacyTextView = view.findViewById(R.id.privacy_policy);
        // 拦截超链接
        interceptHyperLink(privacyTextView);
        stripUnderlines(privacyTextView);
        privacyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        TextView privacyDescTextView = view.findViewById(R.id.privacy_policy_desc);
        interceptHyperLink(privacyDescTextView);
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
        if (warningDialog == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            View v = inflater.inflate(R.layout.privacy_warning_dialog, null);
            TextView disagreeButton = v.findViewById(R.id.disagree);
            TextView agreeButton = v.findViewById(R.id.agree);
            TextView subTitle = v.findViewById(R.id.sub_title);
            subTitle.setMovementMethod(LinkMovementMethod.getInstance());
            warningDialog = new Dialog(activity);
            warningDialog.addContentView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            warningDialog.setCancelable(true);
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
        if (!warningDialog.isShowing()) {
            warningDialog.show();
        }
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
