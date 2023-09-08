package it.polimi.tiw.verbalizationpure.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.verbalizationpure.bean.User;
import it.polimi.tiw.verbalizationpure.dao.CourseDAO;
import it.polimi.tiw.verbalizationpure.dao.ExamDAO;
import it.polimi.tiw.verbalizationpure.util.ParameterChecker;

/**
 * Servlet implementation class RejectResult
 */
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
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formatted request parameters");
			return;
		}
		
		try {
			courseId = Integer.parseInt(courseIdAsString);
		} catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The id you submitted for the course is not a number");
			return;
		}
	
		try {
			examDate = Date.valueOf(examDateAsString);
		} catch(IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The date you submitted for the exam is badly formatted");
			return;
		}
		
		ExamDAO examDAO = new ExamDAO(connection);
		CourseDAO courseDAO = new CourseDAO(connection);
		
		try {
			if (!courseDAO.checkCourseExistence(courseId)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying to reject your grade in an exam for a course that doesn't exist");
				return;
			}
			if (!examDAO.checkSubscriptionToAtLeastOneExam(courseId, student.getId())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying reject a grade for an exam of a course you aren't subscribed to");
				return;
			}
			if (!examDAO.checkExamExistence(courseId, examDate)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The exam you selected doesn't exist");
				return;
			}
			if (!examDAO.checkStudentSubscription(courseId, examDate, student.getId())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can't reject the grade for an exam you didn't subscribe to");
				return;
			}
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		} 
		
		int possibleVerbalId = -1;
		
		try {
			possibleVerbalId = examDAO.updateGradeStatusToRejected(courseId, examDate, studentId);
			if (possibleVerbalId == -1) {
				alreadyVerbalized = true;
			} else {
				alreadyVerbalized = false;
			}
		} catch (SQLException e ) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		}
		
		String pathToServlet = "GoToExamResults?courseId="+courseIdAsString+"&examDate="+examDateAsString+"&alreadyVerbalized="+alreadyVerbalized;
		response.sendRedirect(pathToServlet);
		
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
