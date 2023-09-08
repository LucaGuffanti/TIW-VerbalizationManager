package it.polimi.tiw.verbalizationpure.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.text.html.Option;

import it.polimi.tiw.verbalizationpure.bean.Course;
import it.polimi.tiw.verbalizationpure.bean.Exam;
import it.polimi.tiw.verbalizationpure.bean.User;
import it.polimi.tiw.verbalizationpure.dao.CourseDAO;
import it.polimi.tiw.verbalizationpure.dao.ExamDAO;

/**
 * Servlet implementation class GoToStudentHome
 */
@WebServlet("/GoToStudentHome")
public class GoToStudentHome extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;  
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToStudentHome() {
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
		HttpSession session = request.getSession();
		User student = (User) session.getAttribute("user");
		
		List<Course> subscribedCourses = null;
		List<Exam> subscribedExam = null;
		CourseDAO courseDAO = new CourseDAO(connection);
		ExamDAO examDAO = new ExamDAO(connection);


		try {
			subscribedCourses = courseDAO.getSubscriptionFromStudentId(student.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		}
		
		String chosenCourseIdAsString = request.getParameter("chosenCourse");
		int chosenCourse;
		if (chosenCourseIdAsString == null) {
			chosenCourse = subscribedCourses.get(0).getId();
		} else {
			try {
				chosenCourse = Integer.parseInt(chosenCourseIdAsString);
				
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formatted course selection");
				return;
			} 
		}
		
		try {
			if (!courseDAO.checkCourseExistence(chosenCourse)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying to see the exams for a course that doesn't exist");
				return;
			}
			if (!examDAO.checkSubscriptionToAtLeastOneExam(chosenCourse, student.getId())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying to see the exams for a course you aren't subscribed to");
				return;
			}
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		}


		try {
			subscribedExam = examDAO.getAllDatesFromStudentIdAndCourse(student.getId(), chosenCourse);
		}
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		}
		
		final int _course = chosenCourse;
		
		Optional<Course> chosenCOptional = subscribedCourses.stream()
				.filter(c -> c.getId()==_course)
				.findFirst();

		Course chosenC = chosenCOptional.orElse(null);
		
		String pathToResource = "WEB-INF/studentHome.jsp";
		RequestDispatcher dispatcher = request.getRequestDispatcher(pathToResource);
		request.setAttribute("courses", subscribedCourses);
		request.setAttribute("chosenCourse", chosenC);
		request.setAttribute("exams", subscribedExam);
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
