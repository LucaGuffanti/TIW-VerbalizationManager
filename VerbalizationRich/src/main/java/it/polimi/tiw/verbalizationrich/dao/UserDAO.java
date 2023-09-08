package it.polimi.tiw.verbalizationrich.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.verbalizationrich.bean.User;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public User checkCredentials(int id, String password) throws SQLException {
		String query = "SELECT * FROM user WHERE userID=? AND userPW=?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, id);
			p.setString(2, password);
			try (ResultSet result = p.executeQuery()) {
				User u = null;
				if (!result.isBeforeFirst()) {
					return null;
				} else {
					result.next();
					u = new User();
					u.setId(result.getInt("userID"));
					u.setName(result.getString("name"));
					u.setSurname(result.getString("surname"));
					u.setEmail(result.getString("email"));
					u.setTeacher(result.getBoolean("isTeacher"));
					u.setDegree(result.getString("studentDegree"));
					return u;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new SQLException(e);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
	}
}
