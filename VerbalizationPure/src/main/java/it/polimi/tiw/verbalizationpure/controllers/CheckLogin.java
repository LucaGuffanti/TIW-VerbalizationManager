package it.polimi.tiw.verbalizationpure.controllers;

import java.io.IOException;
import java.sql.Connection;
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
import it.polimi.tiw.verbalizationpure.dao.UserDAO;
import it.polimi.tiw.verbalizationpure.util.ParameterChecker;

/**
 * Servlet implementation class CheckLogin
 */
@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private Connection connection;
	
    public CheckLogin() {
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

		HttpSession s = request.getSession(true);
		int id = 0;
		String pw;

		if (!ParameterChecker.checkString(request.getParameter("id"))
				|| !ParameterChecker.checkString(request.getParameter("password"))) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formatted request parameters");
			return;
		}

		try {
			id = Integer.parseInt(request.getParameter("id"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The id must be a number");
			return;
		}
		
		pw = request.getParameter("password");
		
		UserDAO userDao = new UserDAO(connection);
		User u = null; 
		
		try {
			u = userDao.checkCredentials(id, pw);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Couldn't perform database interrogation");
			return;
		}
		String redirectionPath;
		if (u != null) {
			s.setAttribute("user", u);
			System.out.print("Setting http session ...\n");
			if (u.isTeacher()) {
				System.out.println("User is a teacher");
				redirectionPath = getServletContext().getContextPath() + "/GoToTeacherHome";
			} else {
				System.out.println("User is a student");
				redirectionPath = getServletContext().getContextPath() + "/GoToStudentHome";
			}
		} else {
			System.out.println("Login was not successful");
			redirectionPath = getServletContext().getContextPath() + "/index.jsp";
		}
			
		
		response.sendRedirect(redirectionPath);
		
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
