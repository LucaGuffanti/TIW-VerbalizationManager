package it.polimi.tiw.verbalizationpure.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.verbalizationpure.bean.User;
import it.polimi.tiw.verbalizationpure.bean.Verbal;
import it.polimi.tiw.verbalizationpure.dao.CourseDAO;
import it.polimi.tiw.verbalizationpure.dao.ExamDAO;
import it.polimi.tiw.verbalizationpure.util.ParameterChecker;

/**
 * Servlet implementation class PresentVerbal
 */
@WebServlet("/PresentVerbal")
public class PresentVerbal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PresentVerbal() {
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
		HttpSession s = request.getSession();
		User teacher = (User) s.getAttribute("user");
		
		String examDateAsString = request.getParameter("examDate");
		String courseIdAsString = request.getParameter("courseId");
		String verbalIdAsString = request.getParameter("verbalId");
		String verbalizedAsString = request.getParameter("Verbalized");
		
		int courseId = 0;
		Date examDate = null;
		int verbalId = 0;
		int verbalized = -1;



		if (!ParameterChecker.checkString(courseIdAsString) ||
				!ParameterChecker.checkString(examDateAsString) ||
				!ParameterChecker.checkString(verbalIdAsString) ||
				!ParameterChecker.checkString(verbalizedAsString)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formatted request parameters");
			return;
		}
		
		try {
			courseId = Integer.parseInt(courseIdAsString);
			
		} catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The id you submitted for the course is not valid");
			return;
		}
	
		try {
			examDate = Date.valueOf(examDateAsString);
		} catch(IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The date you submitted for the exam is badly formatted");
			return;
		}
		
		try {
			verbalId = Integer.parseInt(verbalIdAsString);
		} catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The verbal id you submitted for the exam is not valid");
			return;
		}

		try {
			verbalized = Integer.parseInt(verbalizedAsString);
			if (verbalized != 0 && verbalized != 1) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Baldy formatted request parameter");
			}
		} catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formatted request parameter");
			return;
		}
		
		ExamDAO examDAO = new ExamDAO(connection);
		CourseDAO courseDAO = new CourseDAO(connection);
		try {
			if (!courseDAO.checkCourseExistenceAndTeaching(courseId, teacher.getId())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying to access the verbal of an exam for a course that doesn't exist or that you don't teach");
				return;
			}
			if (!examDAO.checkExamExistence(courseId, examDate)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The exam you selected doesn't exist");
				return;
			}
			if (verbalized == 1 && verbalId != examDAO.checkVerbalExistence(courseId, examDate)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You're trying to access a verbal of an exam that has never been verbalized before or"
						+ "a verbal which is not the newly generated one.");
				return;
			}
			// TODO TEST THE LAST IF HERE
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		} 
		
		List<User> subscribers = null;
		Verbal verbal = null;
		
		
		
		String pathToResource = "WEB-INF/verbal.jsp";
		RequestDispatcher dispatcher = request.getRequestDispatcher(pathToResource);
		
		if (verbalized == 0) {
			request.setAttribute("teacher", null);
			request.setAttribute("subs", null);
			request.setAttribute("verbal", null);
		} else {
			
			try {
				subscribers = examDAO.getVerbalizedResults(courseId, examDate, verbalId);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Problem with the database");
				return;
			}
			
			try {
				verbal = examDAO.getVerbalData(verbalId);
				System.out.println(verbal.toString());
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Problem with the database");
				return;
			}
			
			request.setAttribute("teacher", teacher);
			request.setAttribute("subs", subscribers);
			request.setAttribute("verbal", verbal);
		}
		
		request.setAttribute("examDate", examDateAsString);
		request.setAttribute("courseId", courseIdAsString);
		
		request.setAttribute("verbalized", verbalized);
		
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
			} catch (SQLException e) {
				System.out.println("Couldn't close database connection after use");
			}
		}
	}

}
