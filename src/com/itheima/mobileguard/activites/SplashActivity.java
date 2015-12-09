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
	//��������
	private PackageManager packageManager;
	//�ͻ��˵İ汾��
	private int clientVersionCode;
	//�°汾���ص�ַ
	private String downloadurl;
	//��������
	private String desc;
	//�������翪ʼʱ��
	private long startTime;
	//�����������ʱ��
	private long endTime;
	//���ر���·��
	private String savepath; 
	
	//��Ϣ������
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GO_HOME:
				//����������
					Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
					startActivity(intent);
					finish();//�رյ�ǰҳ��
				break;

			case LOAD_NEW_VERSION:
					AlertDialog.Builder builder = new Builder(SplashActivity.this);
					builder.setTitle("���°汾");
					builder.setMessage(desc);
					builder.setPositiveButton("����", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							//���� 
							System.out.println("�����°汾");
							download(downloadurl);
						}

					});
					builder.setNegativeButton("�´���˵", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							//����������
							goHomeMsg();
						}
					});
					builder.show();
				break;
			}
		};
	};
	/**
	 * �����°汾  ��װ
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
						  
						UIUtils.showToast(SplashActivity.this, "���سɹ�");
						//��װ�°汾
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
		packageManager = getPackageManager();//�õ���������
		try {
			//���ݰ����� �õ�����İ���Ϣ
			PackageInfo pInfo = packageManager.getPackageInfo(getPackageName(), 0);
			String versionName = pInfo.versionName;
			clientVersionCode = pInfo.versionCode;
			tv_splash_version.setText(versionName);
			checkVersion();
		} catch (Exception e) {
			e.printStackTrace();
			//���ᷢ��
		}
	}
	/**
	 * ���Ӧ�ð汾����
	 */
	public void checkVersion(){
		new Thread(){
			public void run() {
				try {
					startTime = System.currentTimeMillis();
					//�õ�url
					URL url = new URL(getResources().getString(R.string.url));
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					int code = conn.getResponseCode();
					if(code == 200){
						InputStream is = conn.getInputStream();
						String infostr = StreamTools.readStream(is);
						if(TextUtils.isEmpty(infostr)){
							//��ȡinputstreamʧ��   ������5001 ����ϵ�ͷ�
							UIUtils.showToast(SplashActivity.this, " ������5001 ����������  ����ϵ�ͷ�");
							goHomeMsg();
						}else {
							//ͨ��һ���ַ��� ����һ��jsonobj
							JSONObject jsonObj = new JSONObject(infostr);
							int serverVersionCode = jsonObj.getInt("version");
							downloadurl = jsonObj.getString("url");
							desc = jsonObj.getString("desc");
							//�������˺Ϳͻ��˵İ汾��һ��
							if(serverVersionCode == clientVersionCode){
								//��ͬ������������
								goHomeMsg();
							}else{
								//����ͬ��������ʾ��,�����°汾
								loadNewVersionMsg();
							}
						}
					}else{
						//��������ʧ��
						//������5002 ����ϵ�ͷ�
						UIUtils.showToast(SplashActivity.this, " ������5002 �����������  ����ϵ�ͷ�");
						goHomeMsg();
					}
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
					//������5003 url����  ����ϵ�ͷ�
					UIUtils.showToast(SplashActivity.this, "������5003 url����  ����ϵ�ͷ�");
					goHomeMsg();
				} catch (IOException e) {
					e.printStackTrace();
					//������5004 IO�쳣  ����ϵ�ͷ�
					UIUtils.showToast(SplashActivity.this, "������5004 IO�쳣  ����ϵ�ͷ�");
					goHomeMsg();
				} catch (JSONException e) {
					e.printStackTrace();
					//������5005  json����  ����ϵ�ͷ�
					UIUtils.showToast(SplashActivity.this, "������5005  json����  ����ϵ�ͷ�");
					goHomeMsg();
				}
			};
		}.start();
	}
	//����������
	private void goHomeMsg() {
		sleepForMinist();
		Message msg = Message.obtain();
		msg.what = GO_HOME;
		handler.sendMessage(msg);
	}
	//�����°汾��ʾ
	private void loadNewVersionMsg() {
		Message msg = Message.obtain();
		msg.what = LOAD_NEW_VERSION;
		handler.sendMessage(msg);
	}
	//��splash����ͣ��2��
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
