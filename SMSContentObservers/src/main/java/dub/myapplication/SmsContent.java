package dub.myapplication;

import android.app.Activity;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsContent extends ContentObserver {

		public static final String SMS_URI_INBOX = "content://sms/inbox";

		private Activity activity = null;

		private String smsContent = "";

		private TextView verifyText = null;

		public SmsContent(Activity activity, Handler handler,TextView verifyText) {
			super(handler);
			this.activity = activity;
			this.verifyText = verifyText;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Cursor cursor = null;
			// 读取收件箱中指定号码的未读短信 ,按id排序,防止手机更改时间后读取短信混乱
			cursor = activity.managedQuery(Uri.parse(SMS_URI_INBOX),
					new String[] { "_id", "address", "body", "read" },
					"address=? and read=?", new String[] { "10690278071714", "0" },
					"_id desc");
			if (cursor != null) {// 如果短信为未读模式
				cursor.moveToFirst();
				if (cursor.moveToFirst()) {
					//将未读改为已读模式
					ContentValues values = new ContentValues();
					values.put("read", "1"); 
					
					String smsbody = cursor.getString(cursor.getColumnIndex("body"));
					System.out.println("smsbody======================="	+ smsbody);
					String regEx = "[^0-9]";
					Pattern p = Pattern.compile(regEx);
					Matcher m = p.matcher(smsbody.toString());
					//截取短信中的数字  个数为3
					smsContent = m.replaceAll("").trim().toString().substring(0, 3);
					verifyText.setText(smsContent);
				}

			}

		}

	}