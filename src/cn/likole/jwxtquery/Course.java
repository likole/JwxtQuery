package cn.likole.jwxtquery;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Course {

	/*
	 * courseName 课程名称 credit 学分 grade 成绩 jd 绩点 passed 及格 unpassed 不及格 type
	 * 种类（0.默认 1.记学分不计绩点 2.不计学分不计绩点 3.未发布4.分数为文字)
	 */
	private String courseName;
	private double credit;
	private double grade = 0;
	private String gradeText;
	private double jd = -1;
	private boolean passed = false;
	private boolean unpassed = false;
	private int type = 0;

	/*
	 * 过滤字符串
	 */
	private String filter(String s) {
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
		Matcher m = p.matcher(s);
		String str1 = m.replaceAll("");
		return str1;
	}

	/*
	 * 设置绩点
	 */
	public void setJD() {
		if (type == 0) {
			if (unpassed == true) {
				jd = getUnpassedJD();
			} else {
				jd = getPassedJD();
			}
		}
	}

	/*
	 * 获取及格课程绩点
	 */
	private Double getPassedJD() {
		if (grade >= 90) {
			return 4.0;
		} else if (grade >= 85) {
			return 3.7;
		} else if (grade >= 82) {
			return 3.3;
		} else if (grade >= 78) {
			return 3.0;
		} else if (grade >= 75) {
			return 2.7;
		} else if (grade >= 72) {
			return 2.3;
		} else if (grade >= 68) {
			return 2.0;
		} else if (grade >= 65) {
			return 1.7;
		} else if (grade >= 62) {
			return 1.3;
		} else {
			return 1.0;
		}
	}

	/*
	 * 获取不及格课程绩点
	 */
	private double getUnpassedJD() {
		if (grade >= 60)
			return 1.0;
		else
			return 0.0;
	}

	/*
	 * setter and getter
	 */
	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
		if (courseName.contains("大学体育")) {
			type = 1;
		} else if (courseName.contains("全国英语")) {
			type = 2;
		}
	}

	public double getCredit() {
		return credit;
	}

	public void setCredit(double credit) {
		this.credit = credit;
	}

	public void setCredit(String credit) {
		this.credit = Double.parseDouble(filter(credit));
	}

	public double getGrade() {
		return grade;
	}

	public void setGrade(double grade) {
		this.grade = grade;
	}

	public void setGrade(String grade) {
		try {
			this.grade = Double.parseDouble(filter(grade));
		} catch (Exception e) {
			this.type = 4;
			gradeText=grade;
		}
	}

	public double getJd() {
		return jd;
	}

	public void pass() {
		passed = true;
	}

	public boolean isPassed() {
		return passed;
	}

	public void unpassed() {
		unpassed = true;
	}

	public void unpublish() {
		type = 3;
	}

	public int getType() {
		return type;
	}

	public String getGradeText() {
		return gradeText;
	}

	/*
	 * test
	 */
	@Override
	public String toString() {
		return "Course [courseName=" + courseName + ", credit=" + credit + ", grade=" + grade + ", jd=" + jd
				+ ", passed=" + passed + ", unpassed=" + unpassed + ", type=" + type + "]";
	}
}
