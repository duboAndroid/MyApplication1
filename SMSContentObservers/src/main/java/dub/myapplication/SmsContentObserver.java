package dub.myapplication;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsContentObserver extends ContentObserver {
	private static final String TAG = "SmsContentObserver";
		//发送验证码的号码
		private static final String VERIFY_CODE_FROM = "106903331094102";
		//5位纯数字验证码
		private static final String PATTERN_CODER = "(?<!\\d)\\d{5}(?!\\d)";
		private Context mContext;
		private Handler mHandler;


		public SmsContentObserver(Context context, Handler handler) {
			super(handler);
			this.mContext = context;
			this.mHandler = handler;
		}


		@Override
		public void onChange(boolean selfChange) {
			Uri inBoxUri = Uri.parse("content://sms/inbox");
			Cursor c = mContext.getContentResolver().query(inBoxUri, null,null, null, "date desc");
			if (c != null) {
//				while (c.moveToNext()) {
				//只取最新的一条短信
				if (c.moveToNext()) {
					String number = c.getString(c.getColumnIndex("address"));//手机号
					Log.d(TAG, "number:"+number);
					String body = c.getString(c.getColumnIndex("body"));
					if (number.equals(VERIFY_CODE_FROM)) {
						String verifyCode = patternCode(body);
						Message msg = Message.obtain();
						msg.what = 1;
						msg.obj = verifyCode;
						mHandler.sendMessage(msg);
//						break;
					}
				}
				c.close();
			}
		}
		
		private String patternCode(String patternContent) {
			if (TextUtils.isEmpty(patternContent)) {
				return null;
			}
			Pattern p = Pattern.compile(PATTERN_CODER);
			Matcher matcher = p.matcher(patternContent);
			if (matcher.find()) {
				return matcher.group();
			}
			return null;
		}
	}