package jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Project 2 for Computer Science 363. The purpose of the project is to make queries and updates using JDBC
 * @author Thomas Haddy 10/15/18
 * 
 */
public class ProjectTwo {

	public static void main(String[] args) throws Exception {

		try {
			// Connect to the database
			Connection conn1;
			String dbUrl = "jdbc:mysql://mysql.cs.iastate.edu:3306/db363tclhaddy?"
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
			ResultSet rs = null;
			
			//Print out parts A-F
			printOutCurrentIntructorSalaries_A(rs, statement);
			computeOutMeritList_B(conn1, rs, statement);
			printOutMeritList_C(rs, statement);
			updateInstructorSalaries_D(rs, statement);
			printOutRevisedInstructorSalaries_E(rs, statement);
			closeOut_F(conn1, statement);	
		} //End try
		
		catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
	}

	/* ------------------------------------- Begin A-F ---------------------------------------------------------------------------*/

	// A)
	/**
	 * From your JDBC program print names and salaries of all instructors. Also report the total of all salaries. 
	 * Note that in order to print the required information you will need to execute an SQL query from your JDBC 
	 * program and iterate over the result set returned by the query.
	 * 
	 * @param result  the result
	 * @param state  the statement
	 * @throws SQLException  throws if there was problem connecting to the database
	 */
	public static void printOutCurrentIntructorSalaries_A(ResultSet result, Statement state) throws SQLException {
		
		System.out.println("\n\nA)");
		
		result = state.executeQuery("Select Person.Name, Instructor.Salary from Person inner join Instructor on Person.ID = Instructor.InstructorID");

		int totalSalary = 0;
		int salary = 0;
		String name = "";

		while (result.next()) {
			salary = result.getInt("Salary");
			name = result.getString("Name");
			totalSalary += salary;	

			System.out.println(name + ": " + salary);
		}
		System.out.println("\nTotal Salary: " + totalSalary);
	}

	// B)
	/**
	 * We need to create a table called MeritList that contains information about top 20 (or more) students. To 
	 * determine the top 20 (or more) students, execute an SQL query from your JDBC program that selects IDs of 
	 * students, their classification, MentorID, and GPA in descending order of GPA-values. Then write java code 
	 * to go through the result set of the query and and insert the information for top 20 (or more) students into 
	 * the MeritList table. Note that this list may contain less than 20 distinct GPA-values and more than 20 students. 
	 * This is because some students may have the same GPA. After having taken top 20 students into account, you should 
	 * include those students who have the same GPA as the 20th student.
	 * 
	 * @param conn  the connection
	 * @param result  the result
	 * @param state  the statement
	 * @throws SQLException  throws if there was problem connecting to the database
	 */
	public static void computeOutMeritList_B(Connection conn, ResultSet result, Statement state) throws SQLException {
		
		System.out.println("\n\nB)");

		//Create MeritList table if it doesn't exist
		DatabaseMetaData dbmd = conn.getMetaData();
		ResultSet tables = dbmd.getTables(null, null, "MeritList", null);
		if (!tables.next()) {
			state.executeUpdate(
					"create table MeritList ("
						+ "StudentID char (9) not null, "
						+ "Classification char (10), "
						+ "GPA double,  "
						+ "MentorID char(9), "
						+ "Primary key (StudentID), "
						+ "Foreign key (MentorID) references Instructor(InstructorID)"
					+ ")");
		}

		//Select from the Students Table, order by GPA
		Statement statement2 = conn.createStatement();
		result = statement2.executeQuery("select StudentID, Classification, MentorID, GPA, CreditHours from Student order by GPA desc");

		int studentCount = 1;
		double lastgpa = -1.0;
		while(result.next()) {
			
			String studentID = "";
			String classification = "";
			String mentorID = "";
			double gpa = -1.0;

			//Parse the current student row
			studentID = result.getString("StudentID");
			classification = result.getString("Classification");
			mentorID = result.getString("MentorID");
			gpa = result.getDouble("GPA");

			if(studentCount == 19) {
				//On the 20th student
				lastgpa = gpa;
			} else if(studentCount > 19) {
				//After the 20th student
				if(gpa != lastgpa) {
					//Breaks out of the while loop
					break;
				}
			}

			//Add the student to MeritList
			String update = "Insert Into MeritList (StudentID, Classification, GPA, MentorID) Values ("
					+ ("\"" + studentID + "\"" + ", ")
					+ ("\"" + classification + "\"" + ", ")
					+ (gpa + ",")
					+ ("\"" + mentorID + "\")");
			studentCount++;
			System.out.println(studentCount + ". Update is: " + update);
			state.executeUpdate(update);
		} //End while
		
		statement2.close();
		System.out.println("\nSuccessfully created the MeritList Table");
	}

	// C)
	/**
	 * From your JDBC program print contents of the MeritList table. For this you will need to execute the query 
	 * "select * from MeritList m order by m.GPA" and handle it as a result set.
	 * 
	 * @param result  the result
	 * @param state  the statement
	 * @throws SQLException  throws if there was problem connecting to the database
	 */
	public static void printOutMeritList_C(ResultSet result, Statement state) throws SQLException {

		System.out.println("\n\nC)");

		result = state.executeQuery("select * from MeritList m order by m.GPA");
		ResultSetMetaData rsmd = result.getMetaData();
		int columnCount = rsmd.getColumnCount();

		int count = 1;
		while (result.next()) {
			System.out.print(count + ". ");
			for(int i = 1; i < columnCount; i++) {
				System.out.print(result.getString(i) + "|");
			}
			System.out.println();
			count++;
		}
	}

	// D)
	/**
	 * The mentors of the students in the MeritList computed in Part B need to be given salary raises. The rules for raises are as follows:
	 * 		1. Mentor of a senior gets 10% raise, mentor of a junior gets 8% raise, mentor of a sophomore gets 6% raise, and mentor of a freshman gets 4% raise.
	 * 		2. If an instructor has several students in the Merit List, only one, the highest applicable raise is given.
	 * In order to make the raise effective, updates must be performed on the instructor table. A given instructor's record 
	 * must be updated only once even though he/she may have several students in the Merit List. To streamline this, retrieve 
	 * the MeritList in sorted order of MentorID. Use a ResultSet to process one tuple at a time, wisely making your decisions.
	 * 
	 * @param result  the result
	 * @param state   the statement
	 * @throws SQLException  throws if there was problem connecting to the database
	 */
	public static void updateInstructorSalaries_D(ResultSet result, Statement state) throws SQLException {

		System.out.println("\n\nD)");
		
		//Create a comparator
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		hm.put("Freshman", 0);
		hm.put("Sophomore", 1);
		hm.put("Junior", 2);
		hm.put("Senior", 3);

		//Get MeritList mentors
		result = state.executeQuery("select Classification, MentorID from MeritList order by MentorID");
		HashMap<String, String> instructors = new HashMap<String, String>();
		List<String> instrName = new ArrayList<String>();

		while (result.next()) {
			String id = result.getString("MentorID");
			String classification = result.getString("Classification");

			if(!instructors.containsKey(id)) {
				//Add instructor
				instructors.put(id, classification);
				instrName.add(id);
			} else if(instructors.containsKey(id)) {
				//See if we update classification
				if(compareClassification(hm, instructors.get(id), classification) < 0) {
					instructors.put(id, classification);
				}
			}
		}

		//Update the instructors with raises
		int j = 0;
		for(j = 0; j < instrName.size(); j++) {
			String id = instrName.get(j);
			double raise = -1.0;

			if(instructors.get(id).equals("Freshman"))
				raise = 1.04;
			if(instructors.get(id).equals("Sophomore"))
				raise = 1.06;
			if(instructors.get(id).equals("Junior"))
				raise = 1.08;
			if(instructors.get(id).equals("Senior"))
				raise = 1.10;

			//Putting it all together...
			String update = "update Instructor set Instructor.Salary = CASE WHEN Instructor.InstructorID = \""
					+ id + "\" then Instructor.Salary*"
					+ raise + " ELSE Instructor.Salary END";
			System.out.println("Update is: " + update);
			state.executeUpdate(update);
		}
		System.out.println("\nSuccessfully updated the instructor salaries");
	}
	
	private static int compareClassification(HashMap<String, Integer> classification, String a, String b) {
		if(classification.get(a) < classification.get(b))
			return -1;
		if(classification.get(a) > classification.get(b))
			return 1;
		return 0;
	}

	// E)
	/**
	 * From your JDBC program print names and salaries of all instructors. Also report the total of all salaries. 
	 * Note that this step is identical to Part A.
	 * 
	 * @param result  the result
	 * @param state  the statement
	 * @throws SQLException  throws if there was problem connecting to the database
	 */
	public static void printOutRevisedInstructorSalaries_E(ResultSet result, Statement state) throws SQLException {
		
		System.out.println("\n\nE)");
		result = state.executeQuery("Select Person.Name, Instructor.Salary from Person inner join Instructor on Person.ID = Instructor.InstructorID");

		int totalSalary = 0;
		int salary = 0;
		String name = "";

		while (result.next()) {
			salary = result.getInt("Salary");
			name = result.getString("Name");
			totalSalary += salary;	

			System.out.println(name + ": " + salary);
		}
		System.out.println("\nTotal Salary: " + totalSalary);
	}	

	// F)
	/**
	 * From your JDBC program drop the MeritList table. Then Commit and close all statements and connections.
	 * 
	 * @param conn  the connection
	 * @param state  the statement
	 * @throws SQLException  throws if there was problem connecting to the database
	 */
	public static void closeOut_F(Connection conn, Statement state) throws SQLException {

		System.out.println("\n\nF)");
		state.executeUpdate("drop table MeritList");
		System.out.println("Dropped MeritList");
		state.close();
		conn.close();

		System.out.println("*** Closed all statements and connections successfully ***");
	}

}