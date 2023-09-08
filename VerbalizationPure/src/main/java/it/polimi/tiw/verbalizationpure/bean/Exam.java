package it.polimi.tiw.verbalizationpure.bean;

import java.sql.Date;

public class Exam {
	private int courseId;
	private Date date;
	private int verbalId;
	
	public int getCourseId() {
		return courseId;
	}
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getVerbalId() {
		return verbalId;
	}
	public void setVerbalId(int verbalId) {
		this.verbalId = verbalId;
	}
}
