package it.polimi.tiw.verbalizationrich.controllers;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.polimi.tiw.verbalizationrich.bean.Course;
import it.polimi.tiw.verbalizationrich.bean.Exam;
import it.polimi.tiw.verbalizationrich.bean.User;
import it.polimi.tiw.verbalizationrich.dao.CourseDAO;
import it.polimi.tiw.verbalizationrich.dao.ExamDAO;

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
		CourseDAO courseDAO = new CourseDAO(connection);

		try {
			subscribedCourses = courseDAO.getSubscriptionFromStudentId(student.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			return;
		}
		
		

		Gson g = new GsonBuilder().setPrettyPrinting().create();

		JsonArray courses = g.toJsonTree(subscribedCourses).getAsJsonArray();
		JsonObject result = new JsonObject();
		result.add("courses", courses);

		response.getWriter().print(result.toString());
		
}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
