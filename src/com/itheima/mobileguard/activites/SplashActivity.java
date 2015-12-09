package com.itheima.mobileguard.activites;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.itheima.mobileguard.R;
import com.itheima.mobileguard.utils.StreamTools;
import com.itheima.mobileguard.utils.UIUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.widget.TextView;

public class SplashActivity extends Activity {
	private static final int GO_HOME = 1;
	private static final int LOAD_NEW_VERSION = 0;
	private TextView tv_splash_version;
	private TextView tv_info;
	//包管理类
	private PackageManager packageManager;
	//客户端的版本号
	private int clientVersionCode;
	//新版本下载地址
	private String downloadurl;
	//下载描述
	private String desc;
	//访问网络开始时间
	private long startTime;
	//访问网络结束时间
	private long endTime;
	//下载保存路径
	private String savepath; 
	
	//消息处理器
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GO_HOME:
				//进入主界面
					Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
					startActivity(intent);
					finish();//关闭当前页面
				break;

			case LOAD_NEW_VERSION:
					AlertDialog.Builder builder = new Builder(SplashActivity.this);
					builder.setTitle("有新版本");
					builder.setMessage(desc);
					builder.setPositiveButton("下载", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							//下载 
							System.out.println("下载新版本");
							download(downloadurl);
						}

					});
					builder.setNegativeButton("下次再说", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							//进入主界面
							goHomeMsg();
						}
					});
					builder.show();
				break;
			}
		};
	};
	/**
	 * 下载新版本  安装
	 * @param downloadurl
	 */
	private void download(String downloadurl) {
		HttpUtils http = new HttpUtils();
		downloadurl.substring(downloadurl.lastIndexOf("/")+1);
		savepath = Environment.getExternalStorageDirectory().getPath() + "/" +
				downloadurl.substring(downloadurl.lastIndexOf("/")+1);
		System.out.println("CacheDir"+getCacheDir().getPath());
		System.out.println("ExternalStorageDirectory"+Environment.getExternalStorageDirectory().getPath());
		System.out.println("savepath="+savepath);
		http.download(downloadurl, savepath,
				true, true,
				new RequestCallBack<File>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						// TODO Auto-generated method stub
						goHomeMsg();
					}
					
					@Override
					public void onSuccess(ResponseInfo<File> arg0) {
						// TODO Auto-generated method stub
						  
						UIUtils.showToast(SplashActivity.this, "下载成功");
						//安装新版本
						Intent intent = new Intent();
						intent.setAction("android.intent.action.VIEW");
						intent.addCategory("android.intent.category.DEFAULT");
						intent.setDataAndType(Uri.fromFile(new File(savepath)),
								"application/vnd.android.package-archive");
						startActivity(intent);
					}
					
					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						tv_info.setText(current + "/" +total);
						super.onLoading(total, current, isUploading);
					}
			
				});
		
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_info = (TextView) findViewById(R.id.tv_info);
		packageManager = getPackageManager();//得到包管理器
		try {
			//根据包名称 得到具体的包信息
			PackageInfo pInfo = packageManager.getPackageInfo(getPackageName(), 0);
			String versionName = pInfo.versionName;
			clientVersionCode = pInfo.versionCode;
			tv_splash_version.setText(versionName);
			checkVersion();
		} catch (Exception e) {
			e.printStackTrace();
			//不会发生
		}
	}
	/**
	 * 检测应用版本更新
	 */
	public void checkVersion(){
		new Thread(){
			public void run() {
				try {
					startTime = System.currentTimeMillis();
					//得到url
					URL url = new URL(getResources().getString(R.string.url));
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					int code = conn.getResponseCode();
					if(code == 200){
						InputStream is = conn.getInputStream();
						String infostr = StreamTools.readStream(is);
						if(TextUtils.isEmpty(infostr)){
							//读取inputstream失败   错误码5001 请联系客服
							UIUtils.showToast(SplashActivity.this, " 错误码5001 流解析错误  请联系客服");
							goHomeMsg();
						}else {
							//通过一个字符串 构造一个jsonobj
							JSONObject jsonObj = new JSONObject(infostr);
							int serverVersionCode = jsonObj.getInt("version");
							downloadurl = jsonObj.getString("url");
							desc = jsonObj.getString("desc");
							//服务器端和客户端的版本号一致
							if(serverVersionCode == clientVersionCode){
								//相同，进入主界面
								goHomeMsg();
							}else{
								//不相同，弹出提示框,下载新版本
								loadNewVersionMsg();
							}
						}
					}else{
						//访问网络失败
						//错误码5002 请联系客服
						UIUtils.showToast(SplashActivity.this, " 错误码5002 访问网络错误  请联系客服");
						goHomeMsg();
					}
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
					//错误码5003 url错误  请联系客服
					UIUtils.showToast(SplashActivity.this, "错误码5003 url错误  请联系客服");
					goHomeMsg();
				} catch (IOException e) {
					e.printStackTrace();
					//错误码5004 IO异常  请联系客服
					UIUtils.showToast(SplashActivity.this, "错误码5004 IO异常  请联系客服");
					goHomeMsg();
				} catch (JSONException e) {
					e.printStackTrace();
					//错误码5005  json错误  请联系客服
					UIUtils.showToast(SplashActivity.this, "错误码5005  json错误  请联系客服");
					goHomeMsg();
				}
			};
		}.start();
	}
	//进入主界面
	private void goHomeMsg() {
		sleepForMinist();
		Message msg = Message.obtain();
		msg.what = GO_HOME;
		handler.sendMessage(msg);
	}
	//下载新版本提示
	private void loadNewVersionMsg() {
		Message msg = Message.obtain();
		msg.what = LOAD_NEW_VERSION;
		handler.sendMessage(msg);
	}
	//在splash界面停留2秒
	private void sleepForMinist(){
		endTime = System.currentTimeMillis();
		long betweenTime = endTime - startTime;
		if(betweenTime < 2000){
			try {
				Thread.sleep(2000-betweenTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
