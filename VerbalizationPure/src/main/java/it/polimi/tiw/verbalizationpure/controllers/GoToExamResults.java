package it.polimi.tiw.verbalizationpure.controllers;

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

import it.polimi.tiw.verbalizationpure.bean.Course;
import it.polimi.tiw.verbalizationpure.bean.User;
import it.polimi.tiw.verbalizationpure.dao.CourseDAO;
import it.polimi.tiw.verbalizationpure.dao.ExamDAO;
import it.polimi.tiw.verbalizationpure.util.ParameterChecker;

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
		String alreadyVerbalizedAsString = request.getParameter("alreadyVerbalized");
		
		int courseId;
		Date examDate;
		Boolean alreadyVerbalized;

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
		
		if(alreadyVerbalizedAsString != null && !(alreadyVerbalizedAsString.equals("true")||alreadyVerbalizedAsString.equals("false"))){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formatted 'alreadyVerbalized parameter'");
			return;
		} 
		
		alreadyVerbalized = Boolean.valueOf(alreadyVerbalizedAsString);
		
		ExamDAO examDAO = new ExamDAO(connection);
		CourseDAO courseDAO = new CourseDAO(connection);
		User u = null;
		User teacher = null;
		Course c = null;
		
		try {
			if (!courseDAO.checkCourseExistence(courseId)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying to see the results of an exam for a course that doesn't exist");
				return;
			}
			if (!examDAO.checkSubscriptionToAtLeastOneExam(courseId, student.getId())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying the result of an exam for a course you aren't subscribed to");
				return;
			}
			if (!examDAO.checkExamExistence(courseId, examDate)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The exam you selected doesn't exist");
				return;
			}
			if (!examDAO.checkStudentSubscription(courseId, examDate, student.getId())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You aren't subscribed to exam you want to see the results of");
				return;
			}
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		} 
		
		try {
			u = examDAO.getExamGradeAndStatus(student.getId(), courseId, examDate);
			c = courseDAO.getCoursesFromGivenCourseId(courseId);
			teacher = courseDAO.getTeacherDataFromCourseId(courseId);
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		}
		
		String pathToResource = "WEB-INF/examResult.jsp";
		RequestDispatcher dispatcher = request.getRequestDispatcher(pathToResource);
		
		request.setAttribute("student", student);
		request.setAttribute("status", u.getGradeStatus());
		if (!u.getGradeStatus().equals("NON INSERITO")) {
			request.setAttribute("grade", u.getGrade());			
		}
		request.setAttribute("courseName", c.getName());
		request.setAttribute("courseId", courseId);
		request.setAttribute("examDate", examDateAsString);
		request.setAttribute("teacher", teacher);
		request.setAttribute("alreadyVerbalized", alreadyVerbalized);
		
		dispatcher.forward(request, response);
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
