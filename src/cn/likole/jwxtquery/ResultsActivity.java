package cn.likole.jwxtquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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

public class ResultsActivity extends Activity {

	private List<Map<String, String>> data;
	private String username;
	private String password;
	private ListView listView;
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
	@SuppressLint("HandlerLeak")
	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			if(msg.what==1){
				SimpleAdapter adapter=new SimpleAdapter(ResultsActivity.this, data, R.layout.item, new String[]{"name","credit","grade"}, new int[]{R.id.courseName,R.id.xf,R.id.cj});
				listView.setAdapter(adapter);
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		username=getIntent().getExtras().getString("username");
		password=getIntent().getExtras().getString("password");
		Toast.makeText(this, "正在加载，请稍等......", Toast.LENGTH_SHORT).show();
		login();
		@SuppressWarnings("unused")
		SimpleAdapter adapter=new SimpleAdapter(this, data, R.layout.item, new String[]{"name","credit","grade"}, new int[]{R.id.courseName,R.id.xf,R.id.cj});
		listView=(ListView) findViewById(R.id.listView1);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.results, menu);
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
	
	//登陆教务系统
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
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            	getGrade();
            } 
       });
    }
        
	protected void getGrade() {
		Request.Builder requestBuilder = new Request.Builder().url("http://jwxt.imu.edu.cn/bxqcjcxAction.do?pageSize=300");
        requestBuilder.method("GET",null);
        Request request = requestBuilder.build();
        Call mcall= client.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Document document=Jsoup.parse(response.body().string());
                Elements tr=document.select("tr.odd");
                data=new ArrayList<Map<String, String>>();
                for (Element element : tr) {
                	Elements td=element.select("td");
                	Map<String, String> m=new HashMap<String, String>();
                	m.put("name", td.get(2).text());
                	m.put("credit", td.get(4).text());
                	m.put("grade", td.get(6).text());
                	data.add(m);
				}
                Message msg=mHandler.obtainMessage();
                msg.what=1;
                mHandler.sendMessage(msg);
            }
        });
	}

}
