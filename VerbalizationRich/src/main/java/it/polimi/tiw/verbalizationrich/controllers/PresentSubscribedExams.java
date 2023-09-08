package it.polimi.tiw.verbalizationrich.controllers;

import com.google.gson.*;
import it.polimi.tiw.verbalizationrich.bean.Course;
import it.polimi.tiw.verbalizationrich.bean.Exam;
import it.polimi.tiw.verbalizationrich.bean.User;
import it.polimi.tiw.verbalizationrich.dao.CourseDAO;
import it.polimi.tiw.verbalizationrich.dao.ExamDAO;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/PresentSubscribedExams")
public class PresentSubscribedExams extends HttpServlet {
    private Connection connection;

    @Override
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession s = request.getSession();
        User student = (User) s.getAttribute("user");

        String chosenCourseIdAsString = request.getParameter("chosenCourse");
        int chosenCourse = 0;
        if (chosenCourseIdAsString == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("You must select a course");
            return;
        } else {
            try {
                chosenCourse = Integer.parseInt(chosenCourseIdAsString);

            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("The id of the chosen course must be a number");
                return;
            }
        }

        CourseDAO courseDAO = new CourseDAO(connection);
        ExamDAO examDAO = new ExamDAO(connection);

        Course selected = null;
        try {
            if (!courseDAO.checkCourseExistence(chosenCourse)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("You're trying to see exams for a course that doesn't exist");
                return;
            }
            if (!examDAO.checkSubscriptionToAtLeastOneExam(chosenCourse, student.getId())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("You're trying to see exams for a course you aren't subscribed to");
                return;
            }
        } catch(SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().print("Something went wrong with the database");
            return;
        }

        List<Exam> subscribedExams;

        try {
            selected = courseDAO.getCoursesFromGivenCourseId(chosenCourse);
            subscribedExams = examDAO.getAllDatesFromStudentIdAndCourse(student.getId(), chosenCourse);
        }
        catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().print("Something went wrong with the database");
            return;
        }

        Gson g = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();
        JsonArray exams = g.toJsonTree(subscribedExams).getAsJsonArray();
        JsonObject result = new JsonObject();



        JsonElement chosenCourseElement = g.toJsonTree(selected);

        result.add("exams", exams);
        result.add("chosenCourse", chosenCourseElement);
        System.out.println(result);
        response.getWriter().print(result);

    }

    @Override
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
