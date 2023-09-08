package it.polimi.tiw.verbalizationrich.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.polimi.tiw.verbalizationrich.bean.User;
import it.polimi.tiw.verbalizationrich.dao.CourseDAO;
import it.polimi.tiw.verbalizationrich.dao.ExamDAO;
import it.polimi.tiw.verbalizationrich.util.ParameterChecker;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@MultipartConfig
@WebServlet("/MultiGradeInsert")
public class MultiGradeInsert extends HttpServlet {
    private Connection connection;
    private HashMap<String, String> gradeToEnum = new HashMap<>();

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession s = request.getSession();
        User teacher = (User) s.getAttribute("user");
        String[] studentIdsAsStrings = request.getParameterValues("studentId");
        String courseIdAsString = request.getParameter("courseId");
        String examDateAsString = request.getParameter("examDate");
        String gradeAsString = request.getParameter("grade");

        System.out.println(Arrays.toString(studentIdsAsStrings));

        if (studentIdsAsStrings == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("Badly formatted request parameter");
            return;
        }

        for (String studentId : studentIdsAsStrings) {
            if (!ParameterChecker.checkString(studentId)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("Badly formatted request parameter");
                return;
            }
        }

        if (!ParameterChecker.checkString(courseIdAsString) || !ParameterChecker.checkString(examDateAsString)
        || !ParameterChecker.checkString(gradeAsString)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("Badly formatted request parameter");
            return;
        }

        ArrayList<Integer> studentIds = new ArrayList<>();
        for (int i = 0; i < studentIdsAsStrings.length; i++) {
            try {
                System.out.println("got student id "+studentIdsAsStrings[i]);
                studentIds.add(Integer.parseInt(studentIdsAsStrings[i]));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("The student id "+ studentIdsAsStrings[i]+ " is not a number");
            }
        }

        int courseId;
        Date examDate;
        String grade;


        if (!gradeToEnum.containsKey(gradeAsString)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("The grade you submitted is not recognized by the system");
            return;
        }

        grade = gradeToEnum.get(gradeAsString);

        try {
            courseId = Integer.parseInt(courseIdAsString);
        } catch(NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("The id you submitted for the course is not a number");
            return;
        }

        try {
            examDate = Date.valueOf(examDateAsString);
        } catch(IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("The date you submitted for the exam is badly formatted");
            return;
        }

        ExamDAO examDAO = new ExamDAO(connection);
        CourseDAO courseDAO = new CourseDAO(connection);

        try {
            if (!courseDAO.checkCourseExistenceAndTeaching(courseId, teacher.getId())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("You're trying to update grades for a non existent course or for a course that" +
                        "you don't teach");
            }
            if (!examDAO.checkExamExistence(courseId, examDate)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("You're trying to update grades for an exam that doesn't exist");
            }
            for (Integer studentId : studentIds) {
                if (!examDAO.checkStudentSubscription(courseId, examDate, studentId)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().print("You're trying to insert grades for a student who isn't subscribed");
                    return;
                }
            }
        } catch (SQLException e){
            response.getWriter().print("Error during database access (tried to check input validity)");
        }

        try {
            examDAO.insertMultiGrades(courseId, examDate, studentIds, grade);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().print("Error during database access");
        }

        Gson g = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();

        JsonElement examDateJson = g.toJsonTree(examDateAsString);
        JsonElement courseIdJson = g.toJsonTree(courseId);

        JsonObject result = new JsonObject();
        result.add("course", courseIdJson);
        result.add("examDate", examDateJson);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().print(result);
    }

    @Override
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
