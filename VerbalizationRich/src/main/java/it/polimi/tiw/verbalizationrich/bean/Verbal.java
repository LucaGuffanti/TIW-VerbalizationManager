package it.polimi.tiw.verbalizationrich.bean;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;

public class Verbal implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3669010913972230640L;
	private int id;
	private LocalDateTime date;
	private int courseId;
	private Date examDate;
	private String courseName;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public LocalDateTime getDate() {
		return date;
	}
	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	public int getCourseId() {
		return courseId;
	}
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	public Date getExamDate() {
		return examDate;
	}
	public void setExamDate(Date examDate) {
		this.examDate = examDate;
	}
	
}
