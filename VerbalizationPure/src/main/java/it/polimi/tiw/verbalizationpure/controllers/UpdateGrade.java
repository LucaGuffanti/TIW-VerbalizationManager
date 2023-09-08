package it.polimi.tiw.verbalizationpure.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
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
 * Servlet implementation class UpdateGrade
 */
@WebServlet("/UpdateGrade")
public class UpdateGrade extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private HashMap<String, String> gradeToEnum = new HashMap<>();
	private Connection connection;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateGrade() {
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
    	
    	// INITIALIZING THE VOTE MAP
    	gradeToEnum.put("assente", "ASSENTE");
    	gradeToEnum.put("rimandato", "RIMANDATO");
    	gradeToEnum.put("riprovato", "RIPROVATO");
    	gradeToEnum.put("18", "18");
    	gradeToEnum.put("19", "19");
    	gradeToEnum.put("20", "20");
    	gradeToEnum.put("21", "21");
    	gradeToEnum.put("22", "22");
    	gradeToEnum.put("23", "23");
    	gradeToEnum.put("24", "24");
    	gradeToEnum.put("25", "25");
    	gradeToEnum.put("26", "26");
    	gradeToEnum.put("27", "27");
    	gradeToEnum.put("28", "28");
    	gradeToEnum.put("29", "29");
    	gradeToEnum.put("30", "30");
    	gradeToEnum.put("lode", "30 E LODE");
    	
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String examDateAsString = request.getParameter("examDate");
		String courseIdAsString = request.getParameter("courseId");
		String studentIdAsString = request.getParameter("studentId");
		String submittedGrade = request.getParameter("grade");
		HttpSession s = request.getSession();
		User teacher = (User) s.getAttribute("user");
		
		int courseId = 0;
		int studentId = 0;
		Date examDate = null;


		if (!ParameterChecker.checkString(courseIdAsString) ||
				!ParameterChecker.checkString(examDateAsString) ||
				!ParameterChecker.checkString(studentIdAsString) ||
				!ParameterChecker.checkString(submittedGrade)) {
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
			studentId = Integer.parseInt(studentIdAsString);
		} catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The id you submitted for the student is not a number");
			return;
		}
		
		try {
			examDate = Date.valueOf(examDateAsString);
		} catch(IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The date you submitted for the exam is badly formatted");
			return;
		}
		
		if (!gradeToEnum.containsKey(submittedGrade)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The grade you submitted is not recognized by the system");
			return;
		}
		
		submittedGrade = gradeToEnum.get(submittedGrade);		
		
		ExamDAO examDAO = new ExamDAO(connection);
		CourseDAO courseDAO = new CourseDAO(connection);
		try {
			if (!courseDAO.checkCourseExistenceAndTeaching(courseId, teacher.getId())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying to update a grade of an exam for a course that doesn't exist or that you don't teach");
				return;
			}
			if (!examDAO.checkExamExistence(courseId, examDate)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The exam you selected doesn't exist");
				return;
			}
			if (!examDAO.checkStudentSubscription(courseId, examDate, studentId)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can't update the grade of a non-subscribed student");
				return;
			}
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		}
		
		// actual update of the grade
		try {
			examDAO.updateExamGradeAndStatus(courseId, examDate, studentId, submittedGrade);			
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Error during database access");
		}
		
		String pathToExamSubscribers = "GoToExamSubscribers?examDate="+examDateAsString+"&courseId="+courseIdAsString;
		response.sendRedirect(pathToExamSubscribers);
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
