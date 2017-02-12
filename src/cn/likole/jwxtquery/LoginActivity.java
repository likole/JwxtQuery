package cn.likole.jwxtquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends Activity {

	// 变量声明
	private static String username;
	private static String password;
	private EditText et_username;
	private EditText et_password;
	private static Button btn_request;
	public OkHttpClient client = new OkHttpClient.Builder()
			.cookieJar(new CookieJar() {  
			    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<String, List<Cookie>>();  
			  
			    @Override  
			    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {  
			        cookieStore.put(url.host(), cookies);  
			    }  
			  
			    @Override  
			    public List<Cookie> loadForRequest(HttpUrl url) {  
			        List<Cookie> cookies = cookieStore.get(url.host());  
			        return cookies != null ? cookies : new ArrayList<Cookie>();  
			    }  
			})
		    .build();
	private Toast mToast;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				btn_request.setEnabled(true);
				btn_request.setText("登录");
			}else if(msg.what==1){
				btn_request.setEnabled(true);
				btn_request.setText("登录");
				Intent intent=new Intent(LoginActivity.this, ResultsActivity.class);
				intent.putExtra("username", username);
				intent.putExtra("password", password);
				startActivity(intent);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onRestart() {
		client = new OkHttpClient.Builder()
				.cookieJar(new CookieJar() {  
				    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<String, List<Cookie>>();  
				  
				    @Override  
				    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {  
				        cookieStore.put(url.host(), cookies);  
				    }  
				  
				    @Override  
				    public List<Cookie> loadForRequest(HttpUrl url) {  
				        List<Cookie> cookies = cookieStore.get(url.host());  
				        return cookies != null ? cookies : new ArrayList<Cookie>();  
				    }  
				})
			    .build();
		super.onRestart();
	}

	protected void init() {
		et_username = (EditText) findViewById(R.id.username);
		et_password = (EditText) findViewById(R.id.password);
		btn_request = (Button) findViewById(R.id.requset);
		btn_request.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				btn_request.setEnabled(false);
				btn_request.setText("正在连接教务系统，请稍候......");
				username = et_username.getText().toString();
				password = et_password.getText().toString();
				login();			
			}
		});
	}

	private void login() {
		RequestBody formBody = new FormBody.Builder()
                .add("zjh", username)
                .add("mm", password)
                .build();
        Request request = new Request.Builder()
                .url("http://jwxt.imu.edu.cn/loginAction.do")
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            	runOnUiThread(new Runnable() {
					public void run() {
						if(mToast==null){
							mToast=Toast.makeText(LoginActivity.this, "暂时无法连接教务系统，请稍后重试", Toast.LENGTH_SHORT);
						}else{
							mToast.setText("暂时无法连接教务系统，请稍后重试");
						}				
						mToast.show();
					}
				});
            	Message message=mHandler.obtainMessage();
            	message.what=0;
            	mHandler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                if(str.contains("mainFrame.jsp")){
                	Message message=mHandler.obtainMessage();
	            	message.what=1;
	            	mHandler.sendMessage(message);
                }else{
                	runOnUiThread(new Runnable() {
						public void run() {
							if(mToast==null){
								mToast=Toast.makeText(LoginActivity.this, "无法登陆教务系统,可能是用户名或密码错误", Toast.LENGTH_SHORT);
							}else{
								mToast.setText("无法登陆教务系统,可能是用户名或密码错误");
							}
							mToast.show();
							
						}				
					});			
					Message message=mHandler.obtainMessage();
	            	message.what=0;
	            	mHandler.sendMessage(message);
                }
            }

        });
	}




}
