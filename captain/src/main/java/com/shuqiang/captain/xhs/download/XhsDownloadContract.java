package com.shuqiang.captain.xhs.download;

import android.content.Context;
import android.content.Intent;

import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsSaveSummary;

/**
 * 下载服务和页面之间共享的 action/extra 常量。
 */
public final class XhsDownloadContract {
    public static final String ACTION_START_DOWNLOAD = "com.shuqiang.captain.xhs.action.START_DOWNLOAD";
    public static final String ACTION_PROGRESS = "com.shuqiang.captain.xhs.action.PROGRESS";
    public static final String EXTRA_PARSE_RESULT = "extra_parse_result";
    public static final String EXTRA_SAVE_SUMMARY = "extra_save_summary";

    private XhsDownloadContract() {
    }

    public static Intent buildStartIntent(Context context, XhsParseResult parseResult) {
        Intent intent = new Intent(context, XhsDownloadService.class);
        intent.setAction(ACTION_START_DOWNLOAD);
        intent.putExtra(EXTRA_PARSE_RESULT, parseResult);
        return intent;
    }

    public static Intent buildProgressIntent(Context context, XhsSaveSummary saveSummary) {
        Intent intent = new Intent(ACTION_PROGRESS);
        intent.setPackage(context.getPackageName());
        intent.putExtra(EXTRA_SAVE_SUMMARY, saveSummary);
        return intent;
    }
}
