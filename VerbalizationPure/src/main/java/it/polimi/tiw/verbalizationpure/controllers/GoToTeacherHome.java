package it.polimi.tiw.verbalizationpure.controllers;

import java.io.IOException;
import java.sql.Connection;
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

import it.polimi.tiw.verbalizationpure.bean.Course;
import it.polimi.tiw.verbalizationpure.bean.Exam;
import it.polimi.tiw.verbalizationpure.bean.User;
import it.polimi.tiw.verbalizationpure.dao.CourseDAO;
import it.polimi.tiw.verbalizationpure.dao.ExamDAO;

/**
 * Servlet implementation class GoToTeacherHome
 */
@WebServlet("/GoToTeacherHome")
public class GoToTeacherHome extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToTeacherHome() {
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
		User teacher = (User) session.getAttribute("user");
		
		List<Course> teachedCourses = null;
		CourseDAO courseDAO = new CourseDAO(connection);
		
		try {
			teachedCourses = courseDAO.getCoursesFromTeacherId(teacher.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		}
		
		String chosenCourseIdAsString = request.getParameter("chosenCourse");
		int chosenCourse = 0;
		if (chosenCourseIdAsString == null) {
			chosenCourse = teachedCourses.get(0).getId();
		} else {
			try {
				chosenCourse = Integer.parseInt(chosenCourseIdAsString);
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formatted course selection");
				return;
			}
		}
		try {
			if (!courseDAO.checkCourseExistenceAndTeaching(chosenCourse, teacher.getId())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying to see all the exams for a course that doesn't exist or that you don't teach");
				return;
			}
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		} 
		
		ExamDAO examDAO = new ExamDAO(connection);
		List<Exam> courseExams = null;
		try {
			courseExams = examDAO.getAllDatesFromcourseId(chosenCourse);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
			return;
		}
		
		final int _course = chosenCourse;
		
		Course chosenC = teachedCourses.stream()
				.filter(c -> c.getId()==_course)
				.findFirst().get();
		
		String pathToResource = "WEB-INF/teacherHome.jsp";
		RequestDispatcher dispatcher = request.getRequestDispatcher(pathToResource);
		request.setAttribute("courses", teachedCourses);
		request.setAttribute("exams", courseExams);
		request.setAttribute("chosenCourse", chosenC);
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
