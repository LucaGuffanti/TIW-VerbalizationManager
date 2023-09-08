package it.polimi.tiw.verbalizationrich.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.*;
import it.polimi.tiw.verbalizationrich.bean.Course;
import it.polimi.tiw.verbalizationrich.bean.User;
import it.polimi.tiw.verbalizationrich.dao.CourseDAO;
import it.polimi.tiw.verbalizationrich.dao.ExamDAO;
import it.polimi.tiw.verbalizationrich.util.ParameterChecker;

/**
 * Servlet implementation class GoToExamSubscribers
 */
@WebServlet("/GoToExamSubscribers")
public class GoToExamSubscribers extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    public GoToExamSubscribers() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException{
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

		String dateAsString = request.getParameter("examDate");
		String courseIdAsString = request.getParameter("courseId");
		HttpSession s = request.getSession();
		User teacher = (User) s.getAttribute("user");
		
		if (!(ParameterChecker.checkString(dateAsString) && ParameterChecker.checkString(courseIdAsString))) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print("Badly formatted request parameters");
			return;
		}

		Date date = null;
		
		try {
			date = Date.valueOf(dateAsString);
		} catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print("Badly formatted date");
			return;
		}
		
		int courseId = 0;
		
		try {
			courseId = Integer.parseInt(courseIdAsString);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print("Badly formatted id");
			return;
		}

		CourseDAO courseDAO = new CourseDAO(connection);
		ExamDAO examDAO = new ExamDAO(connection);

		
		try {
			if (!courseDAO.checkCourseExistenceAndTeaching(courseId, teacher.getId())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("You are trying to see the subscribers of an exam for a course that doesn't exist or that you don't teach");
				return;
			}
			if (!examDAO.checkExamExistence(courseId, date)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("The exam you selected doesn't exist");
				return;
			}
		}catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
				response.getWriter().print("Something went wrong in the database");
				return;
			} 
		
		List<User> subscribers = null;
		try {
			subscribers = examDAO.getAllSubscribersOfExamOrderByGrade(courseId, date);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().print("Error during database access");
			return;
		}
		
		Course selectedCourse = null;
		try {
			selectedCourse = courseDAO.getCoursesFromGivenCourseId(courseId); 
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().print("Error during database access");
			return;
		}

		Gson g = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();

		JsonArray subscriberJson = g.toJsonTree(subscribers).getAsJsonArray();
		JsonObject courseJson = g.toJsonTree(selectedCourse).getAsJsonObject();
		JsonElement dateJson = g.toJsonTree(dateAsString);

		JsonObject result = new JsonObject();

		result.add("subscribers", subscriberJson);
		result.add("course", courseJson);
		result.add("date", dateJson);

		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print(result);
		
	}
	
	public void destroy() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				System.out.println("Couldn't close database connection after use");
			}
		}
	}
}
