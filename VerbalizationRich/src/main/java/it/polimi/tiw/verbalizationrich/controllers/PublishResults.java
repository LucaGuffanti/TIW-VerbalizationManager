package it.polimi.tiw.verbalizationrich.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.polimi.tiw.verbalizationrich.bean.User;
import it.polimi.tiw.verbalizationrich.dao.CourseDAO;
import it.polimi.tiw.verbalizationrich.dao.ExamDAO;
import it.polimi.tiw.verbalizationrich.util.ParameterChecker;

/**
 * Servlet implementation class PublishResults
 */
@MultipartConfig
@WebServlet("/PublishResults")
public class PublishResults extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PublishResults() {
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
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String courseIdAsString = request.getParameter("courseId");
		String examDateAsString = request.getParameter("examDate");
		HttpSession s = request.getSession();
		User teacher = (User) s.getAttribute("user");
		
		int courseId = 0;
		Date examDate = null;

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
		
		try {
			if (!courseDAO.checkCourseExistenceAndTeaching(courseId, teacher.getId())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("You are trying to publish the results of an exam for a course that doesn't exist or that you don't teach");
				return;
			}
			if (!examDAO.checkExamExistence(courseId, examDate)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("The exam you're publishing the result of doesn't exist");
				return;
			}
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().print("Something went wrong in the database");
			return;
		} 
		
		try {
			examDAO.publishGrades(courseId, examDate);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().print("Problems in accessing the database");
			return;
		}

		Gson g = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();

		JsonElement courseJson = g.toJsonTree(courseId);
		JsonElement examDateJson = g.toJsonTree(examDate);

		JsonObject result = new JsonObject();

		result.add("course", courseJson);
		result.add("examDate", examDateJson);

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
