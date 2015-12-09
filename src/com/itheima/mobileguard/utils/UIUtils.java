package com.itheima.mobileguard.utils;

import android.app.Activity;
import android.widget.Toast;

/**
 * UI ������  ���ò���
 * @author admin
 *
 */
public class UIUtils {
	//��ӡtoast��ʾ
	public static void showToast(final Activity context, final String msg){
		if("main".equals(Thread.currentThread().getName())){
			Toast.makeText(context, msg, 1).show();
		}else {
			//the action is posted to the event queue of the UI thread.
			//main�̶߳����У��ȴ�ִ��
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context, msg, 1).show();
				}
			});
		}
	}

}
