package com.shuqiang.captain.qr.scan;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.captain.base.BasePermissionActivity;
import com.captain.base.PermissionUtils;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.QRScanActivity;
import com.luck.picture.lib.utils.ToastUtils;
import com.shuqiang.captain.qr.QRActivity;

import captain.R;

public class QrScanActivity extends BasePermissionActivity {
    private static final int QR_SCAN_REQUEST_CODE = 2001;
    private static final String STATE_RESULT_TEXT = "qr_scan_result_text";
    private static final String STATE_GENERATOR_VISIBLE = "qr_scan_generator_visible";
    private static final String STATE_GENERATOR_TEXT = "qr_scan_generator_text";

    private TextView statusTextView;
    private TextView resultTextView;
    private LinearLayout resultContainer;
    private LinearLayout generatorContainer;
    private Button copyButton;
    private Button openLinkButton;
    private Button decryptButton;
    private Button showGeneratorButton;
    private EditText generatorInputView;
    private ImageView qrPreviewImageView;

    private QrScanResultClassifier.ResultInfo currentResult =
            QrScanResultClassifier.classify(null);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViews();
        bindActions();
        restoreState(savedInstanceState);
        renderResult(currentResult);
        if (savedInstanceState == null) {
            startScanWithPrivacyCheck();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentResult.canCopy()) {
            outState.putString(STATE_RESULT_TEXT, currentResult.getDisplayText());
        }
        outState.putBoolean(STATE_GENERATOR_VISIBLE, generatorContainer.getVisibility() == View.VISIBLE);
        outState.putString(STATE_GENERATOR_TEXT, generatorInputView.getText().toString());
    }

    @Override
    protected int getContentLayoutResource() {
        return R.layout.activity_qr_scan;
    }

    private void bindViews() {
        statusTextView = findViewById(R.id.qr_scan_status);
        resultTextView = findViewById(R.id.qr_scan_result_text);
        resultContainer = findViewById(R.id.qr_scan_result_container);
        generatorContainer = findViewById(R.id.qr_scan_generator_container);
        copyButton = findViewById(R.id.qr_scan_copy);
        openLinkButton = findViewById(R.id.qr_scan_open_link);
        decryptButton = findViewById(R.id.qr_scan_decrypt);
        showGeneratorButton = findViewById(R.id.qr_scan_show_generator);
        generatorInputView = findViewById(R.id.qr_scan_generator_input);
        qrPreviewImageView = findViewById(R.id.qr_scan_preview);
    }

    private void restoreState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        currentResult = QrScanResultClassifier.classify(savedInstanceState.getString(STATE_RESULT_TEXT));
        if (savedInstanceState.getBoolean(STATE_GENERATOR_VISIBLE, false)) {
            generatorContainer.setVisibility(View.VISIBLE);
            String generatorText = savedInstanceState.getString(STATE_GENERATOR_TEXT, "");
            generatorInputView.setText(generatorText);
            generatorInputView.setSelection(generatorInputView.length());
        }
    }

    private void bindActions() {
        findViewById(R.id.qr_scan_start).setOnClickListener(view -> startScanWithPrivacyCheck());
        copyButton.setOnClickListener(view -> copyCurrentResult());
        openLinkButton.setOnClickListener(view -> openCurrentLink());
        decryptButton.setOnClickListener(view -> openEncryptedQrDecoder());
        showGeneratorButton.setOnClickListener(view -> showGenerator());
        findViewById(R.id.qr_scan_generate).setOnClickListener(view -> generateQrPreview());
    }

    private void startScanWithPrivacyCheck() {
        PermissionUtils.showPolicyDialog(this, new PermissionUtils.OnPrivacyAgreeListener() {
            @Override
            public void agree() {
                startScanner();
            }

            @Override
            public void disAgree() {
                ToastUtils.showToast(QrScanActivity.this, getString(R.string.qr_scan_privacy_rejected));
            }
        });
    }

    private void startScanner() {
        Intent intent = new Intent(this, QRScanActivity.class);
        intent.setAction(Intents.Scan.ACTION);
        intent.putExtra(Intents.Scan.MODE, Intents.Scan.QR_CODE_MODE);
        intent.putExtra(Intents.Scan.SAVE_HISTORY, false);
        intent.putExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS, 0L);
        startActivityForResult(intent, QR_SCAN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == QR_SCAN_REQUEST_CODE) {
            handleScanResult(resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleScanResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            String rawText = data.getStringExtra(Intents.Scan.RESULT);
            currentResult = QrScanResultClassifier.classify(rawText);
            renderResult(currentResult);
            return;
        }
        if (!currentResult.canCopy()) {
            statusTextView.setText(R.string.qr_scan_cancelled_status);
        }
    }

    private void renderResult(QrScanResultClassifier.ResultInfo resultInfo) {
        boolean hasResult = resultInfo.canCopy();
        resultContainer.setVisibility(hasResult ? View.VISIBLE : View.GONE);
        statusTextView.setText(hasResult
                ? R.string.qr_scan_result_status
                : R.string.qr_scan_idle_status);
        resultTextView.setText(resultInfo.getDisplayText());
        copyButton.setEnabled(resultInfo.canCopy());
        decryptButton.setEnabled(resultInfo.canCopy());
        openLinkButton.setVisibility(resultInfo.canOpenUrl() ? View.VISIBLE : View.GONE);
    }

    private void copyCurrentResult() {
        if (!currentResult.canCopy()) {
            return;
        }
        ClipboardManager clipboardManager =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(
                    getString(R.string.qr_scan_clip_label),
                    currentResult.getDisplayText()));
            ToastUtils.showToast(this, getString(R.string.qr_scan_copied));
        }
    }

    private void openCurrentLink() {
        if (!currentResult.canOpenUrl()) {
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(currentResult.getNormalizedText())));
        } catch (ActivityNotFoundException | SecurityException ignored) {
            ToastUtils.showToast(this, getString(R.string.qr_scan_open_link_failed));
        }
    }

    private void openEncryptedQrDecoder() {
        if (!currentResult.canCopy()) {
            return;
        }
        Intent intent = new Intent(this, QRActivity.class);
        intent.putExtra(QRActivity.EXTRA_QR_MESSAGE_TO_DECODE, currentResult.getDisplayText());
        startActivity(intent);
    }

    private void showGenerator() {
        generatorContainer.setVisibility(View.VISIBLE);
        if (currentResult.canGenerateQrCode()
                && generatorInputView.getText().toString().trim().length() == 0) {
            generatorInputView.setText(currentResult.getDisplayText());
            generatorInputView.setSelection(generatorInputView.length());
        }
    }

    private void generateQrPreview() {
        String text = generatorInputView.getText().toString();
        if (text.trim().length() == 0) {
            ToastUtils.showToast(this, getString(R.string.qr_scan_generate_empty));
            return;
        }
        Bitmap bitmap = QrCodeBitmapFactory.createTextQrCode(this, text);
        if (bitmap == null) {
            ToastUtils.showToast(this, getString(R.string.qr_scan_generate_failed));
            return;
        }
        qrPreviewImageView.setImageBitmap(bitmap);
        qrPreviewImageView.setVisibility(View.VISIBLE);
    }
}
