package com.itheima.mobileguard.utils;

import android.app.Activity;
import android.widget.Toast;

/**
 * UI 工具类  常用操作
 * @author admin
 *
 */
public class UIUtils {
	//打印toast提示
	public static void showToast(final Activity context, final String msg){
		if("main".equals(Thread.currentThread().getName())){
			Toast.makeText(context, msg, 1).show();
		}else {
			//the action is posted to the event queue of the UI thread.
			//main线程队列中，等待执行
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context, msg, 1).show();
				}
			});
		}
	}

}
