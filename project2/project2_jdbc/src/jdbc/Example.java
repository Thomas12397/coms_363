package jdbc;

import java.sql.*;

public class Example
{
	public static void main(String[] args) throws Exception {
		
		try {
			// Connect to the database
			Connection conn1;
			String dbUrl = "jdbc:mysql://csdb.cs.iastate.edu:3306/db363tclhaddy?"
					+ "useUnicode=true&"
					+ "useJDBCCompliantTimezoneShift=true&"
					+ "useLegacyDatetimeCode=false&serverTimezone=UTC&"
					+ "useSSL=false";
			String user = "dbu363tclhaddy";
			String password = "KtbE5978";
			conn1 = DriverManager.getConnection(dbUrl, user, password);
			System.out.println("*** Connected to the database ***");

			// Create Statement and ResultSet variables to use throughout the project
			Statement statement = conn1.createStatement();
			ResultSet rs;

			// get salaries of all instructors
			rs = statement.executeQuery("select * from Instructor f");

			int totalSalary = 0;
			int salary;

			while (rs.next()) {
				//get value of salary from each tuple
				salary = rs.getInt("Salary");			
				totalSalary += salary;	
			}
			System.out.println("Total Salary of all faculty: "+totalSalary);
		
			// Close all statements and connections
			statement.close();
			rs.close();
			conn1.close();

		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
	}

}