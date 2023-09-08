package it.polimi.tiw.verbalizationrich.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.verbalizationrich.bean.Course;
import it.polimi.tiw.verbalizationrich.bean.User;

public class CourseDAO {
	private Connection connection;
	
	public CourseDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Course> getCoursesFromTeacherId(int id) throws SQLException {
		String query = "SELECT id, name FROM course WHERE teacherId=? ORDER BY name DESC";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, id);
			try (ResultSet result = p.executeQuery()) {
				List<Course> courses = new ArrayList<>();

				while(result.next()) {
					Course c = new Course();
					c.setId(result.getInt("id"));
					c.setName(result.getString("name"));
					courses.add(c);
				}
				return courses;
			} 
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}
	
	public Course getCoursesFromGivenCourseId(int id) throws SQLException {
		String query = "SELECT id, name, teacherId FROM course WHERE id=? ORDER BY name DESC";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, id);
			try (ResultSet result = p.executeQuery()) {
				Course c = null;
				if (result.next()) {
					c = new Course();
					c.setName(result.getString("name"));
					c.setId(result.getInt("id"));
					c.setTeacherId(result.getInt("teacherId"));
					System.out.println("Found a course");
				}
				return c;
			} 
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public List<Course> getSubscriptionFromStudentId(int id) throws SQLException {
		String query = "SELECT S.courseId, U.name AS tName, U.surname AS tSurname, C.name "+
		"FROM subscription AS S JOIN course AS C JOIN user AS U ON S.courseId=C.id AND C.teacherId=U.userID "+
		"WHERE studentId=? GROUP BY S.courseId, U.name, U.surname, C.name ORDER BY C.name DESC";

		try(PreparedStatement p = connection.prepareStatement(query)){
			p.setInt(1, id);
			try (ResultSet result = p.executeQuery()) {
				List<Course> courses = new ArrayList<>();
				while (result.next()) {
					Course c = new Course();
					c.setId(result.getInt("courseId"));
					c.setName(result.getString("name"));
					c.setTeacherName(result.getString("tName"));
					c.setTeacherSurname(result.getString("tSurname")	);
					courses.add(c);
				}
				return courses;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public User getTeacherDataFromCourseId(int courseId) throws SQLException {
		String query = "SELECT T.name AS name, T.surname as surname FROM course AS C JOIN User as T ON C.teacherId=T.userID WHERE C.id=?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, courseId);
			try (ResultSet result = p.executeQuery()) {
				User u = null;
				if (result.next()) {
					u = new User();
					u.setName(result.getString("name"));
					u.setSurname(result.getString("surname"));
				}
				return u;
			} 
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}
	
	public boolean checkCourseExistenceAndTeaching(int courseId, int teacherId) throws SQLException {
		Course c = getCoursesFromGivenCourseId(courseId);
		return c != null && c.getTeacherId() == teacherId;
	}

	public boolean checkCourseExistence(int courseId) throws SQLException {
		Course c = getCoursesFromGivenCourseId(courseId);
		return c != null;
	}
}
