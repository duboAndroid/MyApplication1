package dub.myapplication;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;


public class MainActivity extends Activity {

    private TextView get;
    private Uri uri;
    public static final String SMS_URI_INBOX = "content://sms/inbox";

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1 :
                    get.setText((String)msg.obj);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        get = (TextView) findViewById(R.id.get);
        uri = Uri.parse("content://sms/");
        //test 1
        /*ContentResolver resolver1 = getContentResolver();
        resolver1.registerContentObserver(uri, true, new SmsContent1(new Handler()));*/

        //test 2
        /*ContentResolver resolver = getContentResolver();
        // 注册内容观察者
        resolver.registerContentObserver(uri, true, new MyObserver(new Handler()));*/

        //test 3
        ContentResolver resolver1 = getContentResolver();
        resolver1.registerContentObserver(uri, true, new SmsContentObserver1(new Handler()));
    }

    // 自定义的内容观察者
    private class MyObserver extends ContentObserver {
        public MyObserver(Handler handler) {
            super(handler);
        }

        // 当内容观察者 观察到是数据库的内容变化了 调用这个方法
        // 观察到 消息邮箱里面有一条数据库内容变化的通知.
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Toast.makeText(MainActivity.this, "数据库的内容变化了.", Toast.LENGTH_SHORT).show();
            Uri uri = Uri.parse("content://sms/");
            // 获取ContentResolver对象
            ContentResolver resolver = getContentResolver();
            // 查询变化的数据
            Cursor cursor = resolver.query(uri, new String[]{"address", "date", "type", "body"}, null, null, null);
            // 因为短信是倒序排列 因此获取最新一条就是第一个
            cursor.moveToFirst();
            String address = cursor.getString(0);
            String body = cursor.getString(3);
            // 更改UI界面
            get.setText("短信内容：" + body + "\n" + "短信地址：" + address);
            cursor.close();
        }
    }

    private class SmsContent1 extends ContentObserver {
        public SmsContent1(Handler handler) {
            super(handler);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Cursor cursor = null;
            // 读取收件箱中指定号码的未读短信 ,按id排序,防止手机更改时间后读取短信混乱
            cursor = MainActivity.this.managedQuery(Uri.parse(SMS_URI_INBOX),
                    new String[]{"_id", "address", "body", "read"},
                    "address=? and read=?", new String[]{"10690278071714", "0"},
                    "_id desc");
            if (cursor != null) {// 如果短信为未读模式
                cursor.moveToFirst();
                if (cursor.moveToFirst()) {
                    //将未读改为已读模式
                    ContentValues values = new ContentValues();
                    values.put("read", "1");

                    String smsbody = cursor.getString(cursor.getColumnIndex("body"));
                    System.out.println("smsbody=======================" + smsbody);
                    String regEx = "[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(smsbody.toString());
                    //截取短信中的数字  个数为3
                    get.setText(m.replaceAll("").trim().toString().substring(0, 3));
                }

            }

        }
    }

    //5位纯数字验证码
    private static final String PATTERN_CODER = "(?<!\\d)\\d{5}(?!\\d)";

    private class SmsContentObserver1 extends ContentObserver {
        public SmsContentObserver1(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            Uri inBoxUri = Uri.parse("content://sms/inbox");
            Cursor c = MainActivity.this.getContentResolver().query(inBoxUri, null, null, null, "date desc");
            if (c != null) {
//				while (c.moveToNext()) {
                //只取最新的一条短信
                if (c.moveToNext()) {
                    String number = c.getString(c.getColumnIndex("address"));//手机号
                    Log.d(TAG, "number:" + number);
                    String body = c.getString(c.getColumnIndex("body"));
                    String verifyCode = patternCode(body);
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = verifyCode;
                    handler.sendMessage(msg);
//						break;
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


    //test  用没有取消注册！！！
}
