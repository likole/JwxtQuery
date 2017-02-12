package cn.likole.jwxtquery;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.widget.TextView;
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

	/*
	 * data name 	课程名称
	 * 		credit 	学分
	 * 		grade	成绩
	 * 		jd		绩点
	 * 		type	0.及格
	 * 				1.尚不及格
	 * 				2.曾不及格
	 * 				3.体育
	 */
	private List<Map<String, String>> data=new ArrayList<Map<String, String>>();
	private String username;
	private String password;
	private ListView listView;
	private TextView tv_info;
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
				SimpleAdapter adapter=new SimpleAdapter(ResultsActivity.this, data, R.layout.item, new String[]{"name","credit","grade","jd"}, new int[]{R.id.courseName,R.id.xf,R.id.cj,R.id.jd});
				listView.setAdapter(adapter);
				try {
					getInfo();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		username=getIntent().getExtras().getString("username");
		password=getIntent().getExtras().getString("password");
		listView=(ListView) findViewById(R.id.listView1);
		tv_info=(TextView) findViewById(R.id.info);
		Toast.makeText(this, "正在加载，请稍等......", Toast.LENGTH_SHORT).show();
		login();	
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
            	getPassedGrade();
            } 
       });
    }
        
	protected void getPassedGrade() {
		Request.Builder requestBuilder = new Request.Builder().url("http://jwxt.imu.edu.cn/gradeLnAllAction.do?type=ln&oper=qbinfo");
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
                for (Element element : tr) {
                	Elements td=element.select("td");
                	Map<String, String> m=new HashMap<String, String>();
                	m.put("name", td.get(2).text());
                	m.put("credit", td.get(4).text());
                	m.put("grade", td.get(6).text());
                	if(!td.get(2).text().contains("大学体育")){
                		try {
                			String jd=getPassedJD(Double.parseDouble(filter(td.get(6).text())));
                			m.put("jd","绩点:"+jd);
                			m.put("jd_d", jd);
                			m.put("type", "0");
						} catch (Exception e) {
						}
                	}else{
                		m.put("type", "3");
                	}
                	data.add(m);
				}
                getUnpassedGrade();
            }
        });
	}
	
	protected void getUnpassedGrade() {
		Request.Builder requestBuilder = new Request.Builder().url("http://jwxt.imu.edu.cn/gradeLnAllAction.do?type=ln&oper=bjg");
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
                for (Element element : tr) {
                	Elements td=element.select("td");
                	Map<String, String> m=new HashMap<String, String>();
                	m.put("name", td.get(2).text());
                	m.put("credit", td.get(4).text());
                	m.put("grade", td.get(6).text());
                	if(!td.get(2).text().contains("大学体育")){
                		try {
                			String jd=getUnpassedJD(Double.parseDouble(filter(td.get(6).text())));
                			m.put("jd","绩点:"+jd);
                			m.put("jd_d", jd);
                			if("0.0".equals(jd)){
                				m.put("type", "1");
                			}else if("1.0".equals(jd)){
                				m.put("type", "2");
                			}
						} catch (Exception e) {						
						}               		
                	}else{
                		m.put("type", "3");
                	}
                	data.add(m);
				}
                getUnpublishedGrade();
            }
        });
	}

	protected void getUnpublishedGrade() {
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
                for (Element element : tr) {
                	Elements td=element.select("td");
                	if(!td.get(6).hasText()){
                		Map<String, String> m=new HashMap<String, String>();
                    	m.put("name", td.get(2).text());
                    	m.put("credit", td.get(4).text());
                    	m.put("grade","未发布");
                    	data.add(m);
                	}
                	
				}
                Message msg=mHandler.obtainMessage();
                msg.what=1;
                mHandler.sendMessage(msg);
            }
        });
	}

	protected String getUnpassedJD(double grade) {
		if(grade>=60)return "1.0";
		else return "0.0";
	}

	private String getPassedJD(double grade){
		if(grade>=90){
			return "4.0";
		}else if(grade>=85){
			return "3.7";
		}else if(grade>=82){
			return "3.3";
		}else if(grade>=78){
			return "3.0";
		}else if(grade>=75){
			return "2.7";
		}else if(grade>=72){
			return "2.3";
		}else if(grade>=68){
			return "2.0";
		}else if(grade>=65){
			return "1.7";
		}else if(grade>=62){
			return "1.3";
		}else{
			return "1.0";
		}
	}
	
	private void getInfo() throws Exception {
		//define
		int creditSum=0;
		int creditPassed=0;
		int divide=0;
		double GPS=0;
		double GPA;
		for (Map<String, String> m : data) {
			
			//get data
			int type=-1;
			double jd=0;
			int credit=Integer.parseInt(m.get("credit"));
			if(m.get("type")!=null){
				type=Integer.parseInt(m.get("type"));
			}
			if(m.get("jd_d")!=null){
				jd=Double.parseDouble(m.get("jd_d"));
			}

			//credit
			creditSum+=credit;
			if(type==0||type==2){
				creditPassed+=credit;
				divide+=credit;
			}
			if(type==3){
				creditPassed+=credit;
			}
				
			//GPA
			if(type==0||type==2){
				GPS+=credit*jd;
			}
			
		}
		
		//GPA
		GPA=GPS/divide;
		DecimalFormat decimalFormat=new DecimalFormat("0.000");
		String info="总学分:"+creditSum+" 已修读学分:"+creditPassed+" 平均绩点:"+decimalFormat.format(GPA);
		tv_info.setText(info);
	}
	
	private String filter(String s){
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");  
        Matcher m = p.matcher(s);  
        String str1 = m.replaceAll(""); 
        return str1;
	}

}
