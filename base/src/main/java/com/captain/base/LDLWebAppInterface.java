package com.captain.base;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LDLWebAppInterface {
	private BasePermissionActivity permissionActivity;
	private String exportFileContent = null;
	private String exportFileName = null;

	public LDLWebAppInterface(BasePermissionActivity permissionActivity) {
		this.permissionActivity = permissionActivity;
	}

	@JavascriptInterface
	public void exportFile(String fileName, String content) {
		exportFileName = fileName;
		exportFileContent = content;
		permissionActivity.handleRequstPermissionAndReadWriteFile();
	}

	public void onReadWriteFile() {
		if (exportFileContent != null) {
			try {
				// 获取外部文件目录
				File externalFilesDir = permissionActivity.getExternalFilesDir(null);
				if (externalFilesDir != null) {
					// 创建文件
					File file = new File(externalFilesDir, exportFileName);
					// 写入文本内容
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(exportFileContent.getBytes());
					fos.close();
					Toast.makeText(permissionActivity, "文件已保存到: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(permissionActivity, "无法获取外部文件目录", Toast.LENGTH_SHORT).show();
				}
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(permissionActivity, "保存文件失败", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 传入JSON字符串：
	 * {
	 *     "api": "mtop.sports.centre.c.trade.order.create",
	 *     "v": "2.0",
	 *     "type": "POST",
	 *     "data": {
	 *       "stadiumId": "2158",
	 *       "totalAmount": 12500,
	 *       "mainTotalAmount": 12500,
	 *       "mainTotalPayment": 11000,
	 *       "payment": 11000,
	 *       "discountItems": "[{\"actualDiscountAmount\":0,\"availableProductTypes\":null,\"availableStadiumIds\":[2158],\"calculateType\":\"PERCENT\",\"code\":\"56251720\",\"containDiscountTypes\":[],\"containSkuList\":[\"2024061113362388879087011\"],\"discountCondition\":null,\"discountItemExtend\":{\"assetRemainValue\":187578,\"buyerLimitNum\":null,\"couponLimitAmount\":2147483647,\"couponName\":null,\"couponTemplateId\":null,\"couponTemplateType\":null,\"isDisplay\":true,\"limitNumMap\":null,\"maximumPurchaseQuantity\":null,\"minimumPurchaseQuantity\":null,\"notLimitUse\":false,\"numberFreezeCode\":null,\"originalPriceAmount\":0,\"reserveBeginTime\":null,\"reserveEndTime\":null,\"stockAmount\":0,\"stockCycle\":null,\"useBeginTime\":\"2024-09-09 21:30:42\",\"useEndTime\":\"2025-11-20 23:59:59\"},\"discountLimitAmount\":999999999,\"discountLimitNumber\":null,\"discountType\":\"CARDS\",\"discountValue\":8800,\"excludeSkuList\":[],\"id\":\"2025011906585550782598515\",\"isCheckTime\":false,\"name\":\"大牛卡\",\"skuLimitNumberMap\":null,\"timeRuleEnum\":null,\"virtualDiscount\":false}]",
	 *       "requestItems": [
	 *         {
	 *           "commoditySkuId": "2024061113362388879087011",
	 *           "amount": 2
	 *         }
	 *       ],
	 *       "reserveItems": [
	 *         {
	 *           "reserveCalculateType": "PIECE_ORDER",
	 *           "commoditySkuId": "2024061113362388879087011",
	 *           "reservePrice": 12500,
	 *           "reserveId": "202501251500160044002533_6",
	 *           "startDate": "2025-01-19",
	 *           "endDate": "2025-01-19",
	 *           "startTime": "15:00",
	 *           "endTime": "16:00"
	 *         }
	 *       ],
	 *       "contactMobile": "17601613636",
	 *       "businessInfo": {
	 *         "reserveIds": [
	 *           "202501251500160044002533_6"
	 *         ]
	 *       },
	 *       "disPoint": "a21i0.brand_miniapp_order_site.order_view.order_btn"
	 *     }
	 * }
	 * 构造请求：
	 *
	 *
	 * @param fileName
	 * @param content
	 */
	@JavascriptInterface
	public void taobaoFire2(String data) {

	}
}
