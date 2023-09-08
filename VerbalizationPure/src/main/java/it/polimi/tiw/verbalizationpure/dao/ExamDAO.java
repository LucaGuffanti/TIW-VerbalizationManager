package it.polimi.tiw.verbalizationpure.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import it.polimi.tiw.verbalizationpure.bean.Exam;
import it.polimi.tiw.verbalizationpure.bean.User;
import it.polimi.tiw.verbalizationpure.bean.Verbal;

public class ExamDAO {
	private Connection connection;

	public ExamDAO(Connection connection) {
		this.connection = connection;
	}

	public List<Exam> getAllDatesFromcourseId(int courseId) throws SQLException {
		String query = "SELECT examDate FROM exam WHERE courseId=? ORDER BY examDate DESC";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, courseId);
			try (ResultSet result = p.executeQuery()) {
				List<Exam> exams = new ArrayList<>();
				while (result.next()) {
					Exam e = new Exam();
					e.setCourseId(courseId);
					e.setDate(result.getDate("examDate"));
					exams.add(e);
				}
				return exams;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public List<User> getAllSubscribersOfExamOrderByParameters(int courseId, Date examDate, String orderingParameter,
			String ordering) throws SQLException {
		String query = "SELECT userID, name, surname, email, studentDegree, grade, gradeStatus FROM user AS U JOIN subscription AS S ON U.userID = S.studentId WHERE courseId=? AND examDate=? ORDER BY "
				+ orderingParameter + " " + ordering;
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, courseId);
			p.setDate(2, examDate);
			try (ResultSet result = p.executeQuery()) {
				List<User> students = new ArrayList<>();
				while (result.next()) {
					User u = new User();
					u.setId(result.getInt("userID"));
					u.setName(result.getString("name"));
					u.setSurname(result.getString("surname"));
					u.setEmail(result.getString("email"));
					u.setDegree(result.getString("studentDegree"));
					u.setGrade(result.getString("grade"));
					u.setGradeStatus(result.getString("gradeStatus"));

					students.add(u);
				}
				return students;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public User getSubscriberOfExamFromId(int courseId, Date examDate, int studentId) throws SQLException {
		String querySELECT = "SELECT userID, name, surname, email, studentDegree, grade, gradeStatus ";
		String queryFROM = "FROM user JOIN subscription ON userID=studentId ";
		String queryWHERE = "WHERE studentId=? AND examDate=? AND courseId=?";

		String query = querySELECT + queryFROM + queryWHERE;

		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, studentId);
			p.setDate(2, examDate);
			p.setInt(3, courseId);

			try (ResultSet result = p.executeQuery()) {
				if (result.next()) {
					User u = new User();

					u.setId(result.getInt("userID"));
					u.setName(result.getString("name"));
					u.setSurname(result.getString("surname"));
					u.setEmail(result.getString("email"));
					u.setDegree(result.getString("studentDegree"));
					u.setGrade(result.getString("grade"));
					u.setGradeStatus(result.getString("gradeStatus"));

					return u;
				} else {
					return null;
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public void updateExamGradeAndStatus(int courseId, Date examDate, int studentId, String examGrade)
			throws SQLException {
		String query = "UPDATE subscription SET grade=?, gradeStatus='INSERITO' WHERE studentId=? AND courseId=? AND examDate=?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setString(1, examGrade);
			p.setInt(2, studentId);
			p.setInt(3, courseId);
			p.setDate(4, examDate);

			p.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
		return;
	}

	public List<Exam> getAllDatesFromStudentIdAndCourse(int studentId, int courseId) throws SQLException {
		String query = "SELECT examDate FROM subscription WHERE studentId=? AND courseId=? ORDER BY examDate DESC";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, studentId);
			p.setInt(2, courseId);
			try (ResultSet result = p.executeQuery()) {
				List<Exam> exams = new ArrayList<>();
				while (result.next()) {
					Exam exam = new Exam();
					exam.setDate(result.getDate("examDate"));
					exams.add(exam);
				}
				return exams;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public void publishGrades(int courseId, Date examDate) throws SQLException {
		String query = "UPDATE subscription SET gradeStatus='PUBBLICATO' WHERE courseId=? AND examDate=? AND gradeStatus='INSERITO'";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, courseId);
			p.setDate(2, examDate);

			p.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}

	}

	public User getExamGradeAndStatus(int studentId, int courseId, Date examDate) throws SQLException {
		String query = "SELECT grade, gradeStatus FROM subscription WHERE studentId=? AND courseId=? AND examDate=?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, studentId);
			p.setInt(2, courseId);
			p.setDate(3, examDate);

			try (ResultSet result = p.executeQuery()) {
				User u = null;
				if (result.next()) {
					u = new User();
					u.setGradeStatus(result.getString("gradeStatus"));
					if (!u.getGradeStatus().equals("NON INSERITO")) {
						u.setGrade(result.getString("grade"));
					}

				}
				return u;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	@Deprecated
	public int retrieveExamVerbalId(int courseId, Date examDate) throws SQLException {
		String query = "SELECT id FROM verbal WHERE course=? AND dateOfExam=?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, courseId);
			p.setDate(2, examDate);
			try (ResultSet result = p.executeQuery()) {
				int verbalId = 0;
				if (result.next()) {
					verbalId = result.getInt("verbalId");
				}
				return verbalId;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public int getVerbalizableGrades(int courseId, Date examDate) throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM subscription WHERE courseId=? AND examDate=? AND (gradeStatus='RIFIUTATO' OR gradeStatus='PUBBLICATO')";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, courseId);
			p.setDate(2, examDate);

			ResultSet result = p.executeQuery();
			result.next();
			return result.getInt("count");

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public int verbalizeExamResults(int courseId, Date examDate) throws SQLException {
		String insertVerbalIdQuery = "INSERT INTO verbal (verbalDate, course, dateOfExam) VALUES (?,?,?)";
		String getVerbalIdQuery = "SELECT MAX(id) AS id FROM verbal WHERE course=? AND dateOfExam=?";
		String updateRejectedWithQuery = "UPDATE subscription SET grade='RIMANDATO', gradeStatus='VERBALIZZATO', verbal=? WHERE gradeStatus='RIFIUTATO' AND courseId=? AND examDate=? ";
		String updatePublishedWithGradeQuery = "UPDATE subscription SET gradeStatus='VERBALIZZATO', wasRejected=0, verbal=? WHERE gradeStatus='PUBBLICATO' AND courseId=? AND examDate=? ";

		ResultSet result = null;
		int verbalId = -1;

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date now = new java.util.Date();
		String date = formatter.format(now);

		try {

			connection.setAutoCommit(false);

			PreparedStatement pInsert = connection.prepareStatement(insertVerbalIdQuery);
			PreparedStatement pUpdateRejected = connection.prepareStatement(updateRejectedWithQuery);
			PreparedStatement pUpdateOther = connection.prepareStatement(updatePublishedWithGradeQuery);
			PreparedStatement pGetVerbalID = connection.prepareStatement(getVerbalIdQuery);

			// FIRST THE VERBAL IS CREATED IF THERE'S AT LEAST A USER WITH

			pInsert.setTimestamp(1, Timestamp.valueOf(date));
			pInsert.setInt(2, courseId);
			pInsert.setDate(3, examDate);

			pInsert.execute();

			// THEN THE ID OF THE VERBAL IS RETRIEVED

			pGetVerbalID.setInt(1, courseId);
			pGetVerbalID.setDate(2, examDate);

			result = pGetVerbalID.executeQuery();
			result.next();

			verbalId = result.getInt("id");

			// THEN THE GRADES IN THE SUBSCRIPTION TABLE ARE UPDATED
			// (2 QUERIES: ONE FOR REJECTED AND ONE FOR PUBLISHED)

			pUpdateRejected.setInt(1, verbalId);
			pUpdateRejected.setInt(2, courseId);
			pUpdateRejected.setDate(3, examDate);

			pUpdateRejected.executeUpdate();

			pUpdateOther.setInt(1, verbalId);
			pUpdateOther.setInt(2, courseId);
			pUpdateOther.setDate(3, examDate);

			pUpdateOther.executeUpdate();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			connection.setAutoCommit(true);
			e.printStackTrace();
			throw new SQLException(e);
		}
		connection.setAutoCommit(true);
		return verbalId;
	}

	public int updateGradeStatusToRejected(int courseId, Date examDate, int studentId) throws SQLException {

		String nullVerbalQuery = "SELECT verbal FROM subscription WHERE courseId=? AND examDate=? AND studentId=?";
		String statusUpdateQuery = "UPDATE subscription SET gradeStatus='RIFIUTATO', wasRejected=true WHERE courseId=? AND examDate=? AND studentId=?";

		ResultSet result = null;
		int possibleVerbalId = -1;

		try (PreparedStatement p1 = connection.prepareStatement(nullVerbalQuery, ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY); PreparedStatement p2 = connection.prepareStatement(statusUpdateQuery)) {

			connection.setAutoCommit(false);

			p1.setInt(1, courseId);
			p1.setDate(2, examDate);
			p1.setInt(3, studentId);

			result = p1.executeQuery();
			if (result.next()) {

				possibleVerbalId = result.getInt("verbal");

				if (possibleVerbalId != 0) {
					System.out.println("found as already verbalized" + possibleVerbalId);
					connection.rollback();
					connection.setAutoCommit(true);
					return -1;
				}
			}
			p2.setInt(1, courseId);
			p2.setDate(2, examDate);
			p2.setInt(3, studentId);

			p2.executeUpdate();
			connection.commit();


		} catch (SQLException e) {
			e.printStackTrace();
			connection.rollback();
			throw new SQLException(e);
		}
		connection.setAutoCommit(true);
		return 1;
	}

	@Deprecated
	public int getLastVerbalIdFromExam(int courseId, Date date) throws SQLException {
		String query = "SELECT MAX(id) AS verbalId FROM db_verbalization.verbal WHERE course=? AND dateOfExam=?";

		int verbalId;

		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, courseId);
			p.setDate(2, date);

			ResultSet result = p.executeQuery();
			result.next();

			verbalId = result.getInt("verbalId");
			return verbalId;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public List<User> getVerbalizedResults(int courseId, Date examDate, int verbalId) throws SQLException {
		String query = "SELECT userID, name, surname, grade FROM subscription JOIN user ON studentId=userId WHERE courseId=? AND examDate=? AND verbal=?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, courseId);
			p.setDate(2, examDate);
			p.setInt(3, verbalId);

			ResultSet result = p.executeQuery();
			List<User> subs = new ArrayList<>();

			while (result.next()) {
				User u = new User();
				u.setId(result.getInt("userID"));
				u.setName(result.getString("name"));
				u.setSurname(result.getString("surname"));
				u.setGrade(result.getString("grade"));

				subs.add(u);
			}
			return subs;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public Verbal getVerbalData(int verbalId) throws SQLException {
		String query = "SELECT V.id, V.verbalDate, V.course, V.dateOfExam, C.name FROM verbal AS V JOIN course AS C ON V.course=C.id WHERE V.id=?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, verbalId);
			ResultSet result = p.executeQuery();

			Verbal v = new Verbal();

			result.next();

			v.setId(result.getInt("id"));
			v.setDate(result.getTimestamp("verbalDate").toLocalDateTime());
			v.setCourseId(result.getInt("course"));
			v.setExamDate(result.getDate("dateOfExam"));
			v.setCourseName(result.getString("name"));

			return v;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public boolean checkStudentSubscription(int courseId, Date examDate, int studentId) throws SQLException {
		return getSubscriberOfExamFromId(courseId, examDate, studentId) != null;
	}

	public boolean checkSubscriptionToAtLeastOneExam(int courseId, int studentId) throws SQLException {
		String query = "SELECT * FROM user WHERE userID IN ( SELECT DISTINCT U.studentId FROM subscription AS U where courseId=? AND userID=U.studentId AND U.studentId=?)";
		User u = null;

		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, courseId);
			p.setInt(2, studentId);

			try (ResultSet result = p.executeQuery()) {
				if (result.next()) {
					u = new User();
				}
				System.out.println(u);
				return u != null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public boolean checkExamExistence(int courseId, Date examDate) throws SQLException {
		String query = "SELECT * FROM exam WHERE courseId=? AND examDate=?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, courseId);
			p.setDate(2, examDate);

			ResultSet result = p.executeQuery();
			boolean b = result.next();
			System.out.println(b);

			return b;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

	public int checkVerbalExistence(int courseId, Date examDate) throws SQLException {
		String query = "SELECT MAX(id) as id FROM verbal WHERE course=? AND dateOfExam=?";
		try {
			PreparedStatement p = connection.prepareStatement(query);
			p.setInt(1, courseId);
			p.setDate(2, examDate);

			ResultSet result = p.executeQuery();
			if (result.next()) {
				return result.getInt("id");
			}
			return 0;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}

}
