package it.polimi.tiw.verbalizationrich.bean;

import java.io.Serializable;
import java.sql.Date;

public class Exam implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7166767731813095101L;
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
