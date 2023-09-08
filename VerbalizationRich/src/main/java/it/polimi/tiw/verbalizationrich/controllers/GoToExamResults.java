package it.polimi.tiw.verbalizationrich.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.polimi.tiw.verbalizationrich.bean.Course;
import it.polimi.tiw.verbalizationrich.bean.User;
import it.polimi.tiw.verbalizationrich.dao.CourseDAO;
import it.polimi.tiw.verbalizationrich.dao.ExamDAO;
import it.polimi.tiw.verbalizationrich.util.ParameterChecker;

/**
 * Servlet implementation class GoToExamResults
 */
@WebServlet("/GoToExamResults")
public class GoToExamResults extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToExamResults() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException {
    	try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
		    e.printStackTrace();
			throw new UnavailableException("Could't load database driver");
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new UnavailableException("Couldn't connect to the database");
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession s = request.getSession();
		User student = (User) s.getAttribute("user");
		
		String courseIdAsString = request.getParameter("courseId");
		String examDateAsString = request.getParameter("examDate");
		
		int courseId;
		Date examDate;
		Boolean alreadyVerbalized;

		if (!ParameterChecker.checkString(courseIdAsString) ||
		!ParameterChecker.checkString(examDateAsString)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print("Badly formatted request parameters");
		}
		
		try {
			courseId = Integer.parseInt(courseIdAsString);
		} catch(NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print("The id you submitted for the course is not a number");
			return;
		}
	
		try {
			examDate = Date.valueOf(examDateAsString);
		} catch(IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print("The date you submitted for the exam is badly formatted");
			return;
		}

		
		ExamDAO examDAO = new ExamDAO(connection);
		CourseDAO courseDAO = new CourseDAO(connection);
		User u = null;
		User teacher = null;
		Course c = null;
		
		try {
			if (!courseDAO.checkCourseExistence(courseId)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("You are trying to see the results of an exam for a course that doesn't exist");
				return;
			}
			if (!examDAO.checkExamExistence(courseId, examDate)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("The exam you selected doesn't exist");
				return;
			}
			if (!examDAO.checkSubscriptionToAtLeastOneExam(courseId, student.getId())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("You're trying to see the results of an exam for a course you've never subscribed to");
				return;
			}
			if (!examDAO.checkStudentSubscription(courseId, examDate, student.getId())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("You aren't subscribed to exam you want to see the results of");
				return;
			}
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().print("Something went wrong in the database");
			return;
		} 
		
		try {
			u = examDAO.getExamGradeAndStatus(student.getId(), courseId, examDate);
			c = courseDAO.getCoursesFromGivenCourseId(courseId);
			teacher = courseDAO.getTeacherDataFromCourseId(courseId);
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().print("Something went wrong in the database");
			return;
		}
		
		Gson g = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();

		if (u.getGradeStatus().equals("NON INSERITO") || u.getGradeStatus().equals("INSERITO")) {
			u.setGrade("NON DISPONIBILE");
		}

		JsonElement courseJson = g.toJsonTree(c);
		JsonElement examDateJson = g.toJsonTree(examDateAsString);
		JsonElement teacherJson = g.toJsonTree(teacher);
		JsonElement gradeJson = g.toJsonTree(u);
		JsonElement studentJson = g.toJsonTree(student);

		JsonObject result = new JsonObject();


		result.add("course", courseJson);
		result.add("date", examDateJson);
		result.add("teacher", teacherJson);
		result.add("grade", gradeJson);
		result.add("student", studentJson);

		response.setStatus(HttpServletResponse.SC_OK);
		System.out.println(result);
		response.getWriter().print(result);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	public void destroy() {
		if (connection != null) {
			try {
				connection.close();
			} catch(SQLException e) {
				System.out.println("Couldn't close database connection after use");
			}
		}
	}
}
