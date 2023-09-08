package it.polimi.tiw.verbalizationrich.controllers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.*;
import it.polimi.tiw.verbalizationrich.bean.Course;
import it.polimi.tiw.verbalizationrich.bean.User;
import it.polimi.tiw.verbalizationrich.bean.Verbal;
import it.polimi.tiw.verbalizationrich.dao.CourseDAO;
import it.polimi.tiw.verbalizationrich.dao.ExamDAO;
import it.polimi.tiw.verbalizationrich.util.GsonLocalDateTime;
import it.polimi.tiw.verbalizationrich.util.ParameterChecker;

/**
 * Servlet implementation class VerbalizeResults
 */
@MultipartConfig
@WebServlet("/VerbalizeResults")
public class VerbalizeResults extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VerbalizeResults() {
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
		response.getWriter().append("Served at: ").append(request.getContextPath());
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
				response.getWriter().print("You are trying to verbalize the results of an exam for a course that doesn't exist");
				return;
			}
			if (!examDAO.checkExamExistence(courseId, examDate)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("The exam you selected doesn't exist");
				return;
			}
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().print("Something went wrong in the database");
			return;
		} 


		int verbalId = -1;
		List<User> subscribers = null;
		Course course = null;
		Verbal verbal = null;

		try {
			course = courseDAO.getCoursesFromGivenCourseId(courseId);
			if (examDAO.getVerbalizableGrades(courseId, examDate) != 0)  {
				verbalId = examDAO.verbalizeExamResults(courseId, examDate);
				subscribers = examDAO.getVerbalizedResults(courseId, examDate, verbalId);
				verbal = examDAO.getVerbalData(verbalId);
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().print("Problems with the database");
			return;
		}

		Gson g = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd")
				.registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTime()).create();
		JsonArray subscribersJson = null;
		if (subscribers != null) {
			 subscribersJson = g.toJsonTree(subscribers).getAsJsonArray();
		}

		JsonObject courseJson = g.toJsonTree(course).getAsJsonObject();
		JsonElement dateJson = g.toJsonTree(examDateAsString);

		JsonObject verbalJson = null;
		if (verbal != null) {
			verbalJson = g.toJsonTree(verbal).getAsJsonObject();
		}
		JsonElement verbalIdJson = g.toJsonTree(verbalId);

		JsonObject result = new JsonObject();

		result.add("subscribers", subscribersJson);
		result.add("course", courseJson);
		result.add("examDate", dateJson);
		result.add("verbal", verbalJson);
		result.add("verbalId", verbalIdJson);

		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print(result);
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

