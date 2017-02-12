package cn.likole.jwxtquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class CourseManage {
	private Map<String, Course> data=new HashMap<String, Course>();
	
	public void add_passed(Element tr) {
		Elements td=tr.select("td");
		String courseID=td.get(0).text();
		Course course=new Course();
		course.setCourseName(td.get(2).text());
		course.setCredit(td.get(4).text());
		course.setGrade(td.get(6).text());
		course.pass();
		data.put(courseID, course);
	}
	
	public void add_unpassed(Element tr) {
		Elements td=tr.select("td");
		String courseID=td.get(0).text();
		if((!data.containsKey(courseID))||(!data.get(courseID).isPassed())){
			Course course=new Course();
			course.setCourseName(td.get(2).text());
			course.setCredit(td.get(4).text());
			course.setGrade(td.get(6).text());
			data.put(courseID, course);
		}
		data.get(courseID).unpassed();		
	}
	
	public void add_unpublished(Element tr){
		Elements td=tr.select("td");
    	if(!td.get(6).hasText()){
    		String courseID=td.get(0).text();
			Course course=new Course();
			course.setCourseName(td.get(2).text());
			course.setCredit(td.get(4).text());
			data.put(courseID, course);
    	}
	}
	
	private void setJD(){
		for (String courseID : data.keySet()) {
			Course course=data.get(courseID);
			course.setJD();
		}
	}

	public List<Map<String, String>> getData() {
		setJD();
		List<Map<String, String>> adapterData =new ArrayList<Map<String, String>>();
		for (String courseID : data.keySet()) {
			Course course=data.get(courseID);
			Map<String, String> m=new HashMap<String, String>();
			m.put("name", course.getCourseName());
			m.put("credit", String.valueOf(course.getCredit()));
			if(course.getType()==3){
				m.put("grade", "未发布");
			}else{
				m.put("grade", String.valueOf(course.getGrade()));
			}			
			if(course.getJd()!=-1){
				m.put("jd", "绩点:"+String.valueOf(course.getJd()));
			}
			adapterData.add(m);
		}
		return adapterData;
	}	
	
}
