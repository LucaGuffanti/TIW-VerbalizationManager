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
 * Servlet implementation class VerbalizeResults
 */
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
			if (!courseDAO.checkCourseExistenceAndTeaching(courseId, teacher.getId())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying to verbalize the results of an exam for a course that doesn't exist or that you don't teach");
				return;
			}
			if (!examDAO.checkExamExistence(courseId, examDate)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The exam you selected doesn't exist");
				return;
			}
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		} 
		
		int verbalId = -1;
		try {
			if (examDAO.getVerbalizableGrades(courseId, examDate) == 0) {
				String path = "PresentVerbal?Verbalized=0&examDate="+examDateAsString+"&courseId="+courseIdAsString+"&verbalId="+verbalId;
				response.sendRedirect(path);
				return;
			} else {				
				verbalId = examDAO.verbalizeExamResults(courseId, examDate);
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Problems with the database");
			return;
		}
		
		String pathToResource = "PresentVerbal?Verbalized=1&examDate="+examDateAsString+"&courseId="+courseIdAsString+"&verbalId="+verbalId;
		response.sendRedirect(pathToResource);
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
