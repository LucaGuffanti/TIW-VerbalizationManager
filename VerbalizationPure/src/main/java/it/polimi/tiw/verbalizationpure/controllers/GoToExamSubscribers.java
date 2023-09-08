package it.polimi.tiw.verbalizationpure.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
 * Servlet implementation class GoToExamSubscribers
 */
@WebServlet("/GoToExamSubscribers")
public class GoToExamSubscribers extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    
    
    private Map<String, String> nameToOrderAttribute = Map.of(
			"id", "userID",
			"name", "name",
			"surname", "surname",
			"e-mail", "email",
			"degree", "studentDegree",
			"grade", "grade",
			"status", "gradeStatus"
			);
   
    private Map<String, String> nameToOrderType = Map.of(
    		"ascending", "ASC",
    		"descending", "DESC"
    		);
	
    public GoToExamSubscribers() {
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
		String dateAsString = request.getParameter("examDate");
		String courseIdAsString = request.getParameter("courseId");

		String wantedOrderingParameterPre = request.getParameter("orderParameter");
		String wantedOrdering = request.getParameter("ordering");
		String wantedOrderingParameter;

		String idOrdering = request.getParameter("idOrd");
		String nameOrdering = request.getParameter("nameOrd");
		String surnameOrdering = request.getParameter("surnameOrd");
		String emailOrdering = request.getParameter("emailOrd");
		String degreeOrdering = request.getParameter("degreeOrd");
		String gradeOrdering = request.getParameter("gradeOrd");
		String statusOrdering = request.getParameter("statusOrd");

		if (!ParameterChecker.checkString(dateAsString) ||
			!ParameterChecker.checkString(courseIdAsString)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formatted request parameters");
			return;
		}
		if (wantedOrderingParameterPre != null || wantedOrdering != null ||
		idOrdering != null ||
		nameOrdering != null ||
		surnameOrdering != null ||
		emailOrdering != null ||
		degreeOrdering != null ||
		gradeOrdering != null ||
		statusOrdering != null
		) {
			if (!nameToOrderAttribute.containsKey(wantedOrderingParameterPre) ||
					!nameToOrderType.containsKey(wantedOrdering) ||
					!nameToOrderType.containsKey(idOrdering) ||
					!nameToOrderType.containsKey(nameOrdering) ||
					!nameToOrderType.containsKey(surnameOrdering) ||
					!nameToOrderType.containsKey(emailOrdering) ||
					!nameToOrderType.containsKey(degreeOrdering) ||
					!nameToOrderType.containsKey(gradeOrdering) ||
					!nameToOrderType.containsKey(statusOrdering)

			) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formatted request parameters");
				System.out.println("wantedOrderingParameter: " + wantedOrderingParameterPre);
				System.out.println("wantedOrdering: " + wantedOrdering);
				System.out.println("idOrdering: " + idOrdering);
				System.out.println("nameOrdering: " + nameOrdering);
				System.out.println("surnameOrdering: " + surnameOrdering);
				System.out.println("emailOrdering: " + emailOrdering);
				System.out.println("degreeOrdering: " + degreeOrdering);
				System.out.println("gradeOrdering: " + gradeOrdering);
				System.out.println("statusOrdering: " + statusOrdering);
				return;
			} else {
				wantedOrderingParameter = nameToOrderAttribute.get(wantedOrderingParameterPre);
				wantedOrdering = nameToOrderType.get(wantedOrdering);
			}
		} else {
			wantedOrderingParameterPre = "grade";
			wantedOrderingParameter = "grade";
			wantedOrdering = "ASC";

			idOrdering = "ascending";
			nameOrdering = "ascending";
			surnameOrdering = "ascending";
			emailOrdering = "ascending";
			degreeOrdering = "ascending";
			gradeOrdering = "ascending";
			statusOrdering = "ascending";

		}
		
		Date date = null;
		
		try {
			date = Date.valueOf(dateAsString);
		} catch (IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formatted date");
			return;
		}
		
		int courseId = 0;
		
		try {
			courseId = Integer.parseInt(courseIdAsString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formatted id");
			return;
		}

		CourseDAO courseDAO = new CourseDAO(connection);
		ExamDAO examDAO = new ExamDAO(connection);

		
		try {
			if (!courseDAO.checkCourseExistenceAndTeaching(courseId, teacher.getId())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying to see the subscribers of an exam for a course that doesn't exist or that you don't teach");
				return;
			}
			if (!examDAO.checkExamExistence(courseId, date)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The exam you selected doesn't exist");
				return;
			}
		}catch(SQLException e) {
				response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Something went wrong in the database");
				return;
			} 
		
		List<User> subscribers = null;
		try {
			subscribers = examDAO.getAllSubscribersOfExamOrderByParameters(courseId, date, wantedOrderingParameter, wantedOrdering);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Error during database access");
			return;
		}
		
		Course selectedCourse = null;
		try {
			selectedCourse = courseDAO.getCoursesFromGivenCourseId(courseId); 
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Error during database access");
			return;
		}

		switch (wantedOrderingParameterPre) {
			case "id" : {
				if (wantedOrdering.equals("ASC")) {
					idOrdering = "descending";
				} else {
					idOrdering = "ascending";
				}
				System.out.println("setting id " + idOrdering);
				break;
			}
			case "name" : {
				if (wantedOrdering.equals("ASC")) {
					nameOrdering = "descending";
				} else {
					nameOrdering = "ascending";
				}
				System.out.println("setting name " + nameOrdering);
				break;
			}
			case "surname" : {
				if (wantedOrdering.equals("ASC")) {
					surnameOrdering = "descending";
				} else {
					surnameOrdering = "ascending";
				}
				System.out.println("setting surname " + surnameOrdering);
				break;
			}
			case "e-mail" : {
				if (wantedOrdering.equals("ASC")) {
					emailOrdering = "descending";
				} else {
					emailOrdering = "ascending";
				}
				System.out.println("setting email " + emailOrdering);
				break;
			}
			case "degree" : {
				if (wantedOrdering.equals("ASC")) {
					degreeOrdering = "descending";
				} else {
					degreeOrdering = "ascending";
				}
				System.out.println("setting degree " + degreeOrdering);
				break;
			}
			case "grade" : {
				if (wantedOrdering.equals("ASC")) {
					gradeOrdering = "descending";
				} else {
					gradeOrdering = "ascending";
				}
				System.out.println("setting grade " + gradeOrdering);
				break;
			}
			case "status" : {
				if (wantedOrdering.equals("ASC")) {
					statusOrdering = "descending";
				} else {
					statusOrdering = "ascending";
				}
				System.out.println("setting status " + statusOrdering);
				break;
			}
		}

		String pathToResource = "WEB-INF/examSubscribers.jsp";
		RequestDispatcher dispatcher = request.getRequestDispatcher(pathToResource);
		request.setAttribute("subs", subscribers);
		request.setAttribute("courseName", selectedCourse.getName());
		request.setAttribute("courseId", courseId);
		request.setAttribute("examDate", dateAsString);
		request.setAttribute("idOrd", idOrdering);
		request.setAttribute("nameOrd", nameOrdering);
		request.setAttribute("surnameOrd", surnameOrdering);
		request.setAttribute("emailOrd", emailOrdering);
		request.setAttribute("degreeOrd", degreeOrdering);
		request.setAttribute("gradeOrd", gradeOrdering);
		request.setAttribute("statusOrd", statusOrdering);

		dispatcher.forward(request, response);
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
