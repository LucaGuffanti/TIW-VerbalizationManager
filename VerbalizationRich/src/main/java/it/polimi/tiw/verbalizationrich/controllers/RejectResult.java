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
 * Servlet implementation class RejectResult
 */
@MultipartConfig
@WebServlet("/RejectResult")
public class RejectResult extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RejectResult() {
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
		HttpSession s = request.getSession();
		User student = (User) s.getAttribute("user");
		int studentId = student.getId();
		
		String courseIdAsString = request.getParameter("courseId");
		String examDateAsString = request.getParameter("examDate");
		
		int courseId = 0;
		Date examDate = null;
		boolean alreadyVerbalized = false;

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
			if (!courseDAO.checkCourseExistence(courseId)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("You are trying to reject your grade in an exam for a course that doesn't exist");
				return;
			}
			if (!examDAO.checkExamExistence(courseId, examDate)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("The exam you selected doesn't exist");
				return;
			}
			if (!examDAO.checkSubscriptionToAtLeastOneExam(courseId, student.getId())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("You are trying to reject your grade in an exam for a course you aren't subscribed to");
				return;
			}
			if (!examDAO.checkStudentSubscription(courseId, examDate, student.getId())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("You can't reject the grade for an exam you didn't subscribe to");
				return;
			}
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().print("Something went wrong in the database");
			return;
		} 
		
		int possibleVerbalId = -1;
		
		try {
			possibleVerbalId = examDAO.updateGradeStatusToRejected(courseId, examDate, studentId);
			if (possibleVerbalId == -1) {
				alreadyVerbalized = true;
			}
		} catch (SQLException e ) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().print("Something went wrong in the database");
			return;
		}

		Gson g = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();
		JsonObject result = new JsonObject();
		JsonElement alreadyVerbalizedJson = g.toJsonTree(alreadyVerbalized);
		JsonElement courseJson = g.toJsonTree(courseId);
		JsonElement examDateJson = g.toJsonTree(examDate);

		result.add("alreadyVerbalized", alreadyVerbalizedJson);
		result.add("courseId", courseJson);
		result.add("examDate", examDateJson);
		System.out.println(result);

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
