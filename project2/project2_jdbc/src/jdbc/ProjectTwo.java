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

		int studentCount = 0;
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
				//on 20th student
				lastgpa = gpa;
			} else if(studentCount > 19) {
				//after 20th student
				if(gpa != lastgpa) {
					break;
				}
			}

			// add student to MeritList
			String update = "Insert Into MeritList (StudentID, Classification, GPA, MentorID) Values (";
			update += ("\"" + studentID + "\"" + ", ");
			update += ("\"" + classification + "\"" + ", ");
			update += (gpa + ",");
			update += ("\"" + mentorID + "\")");
			System.out.println("Update is: " + update);
			state.executeUpdate(update);
			studentCount++;
		} //End while
		
		statement2.close();
		System.out.println("Successfully created the MeritList Table");
	}

	// C)
	public static void printOutMeritList_C(ResultSet result, Statement state) throws SQLException {

		System.out.println("\n\nC)");

		result = state.executeQuery("select * from MeritList m order by m.GPA");
		ResultSetMetaData rsmd = result.getMetaData();
		int cn = rsmd.getColumnCount();

		while (result.next()) {
			for(int i = 1; i < cn; i++) {
				System.out.print(result.getString(i) + "|");
			}
			System.out.println();
		}
	}

	// D)
	public static void updateInstructorSalaries_D(ResultSet result, Statement state) throws SQLException {

		System.out.println("\n\nD)");
		
		//Create a comparator
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		hm.put("Freshman", 0);
		hm.put("Sophomore", 1);
		hm.put("Junior", 2);
		hm.put("Senior", 3);

		//get MeritList mentors
		result = state.executeQuery("select Classification, MentorID from MeritList order by MentorID");
		HashMap<String, String> instrs = new HashMap<String, String>();
		List<String> instrName = new ArrayList<String>();

		while (result.next()) {
			String id = result.getString("MentorID");
			String cl = result.getString("Classification");

			if(!instrs.containsKey(id)) {
				//add instructor
				instrs.put(id, cl);
				instrName.add(id);
			} else if(instrs.containsKey(id)) {
				//see if we update classification
				if(compareClassification(hm, instrs.get(id), cl) < 0) {
					instrs.put(id, cl);
				}
			}
		}

		//update instructors with raises
		int j = 0;
		for(j = 0; j < instrName.size(); j++) {
			String id = instrName.get(j);
			double raise = -1;

			//System.out.println("Entry is: " + id + ", " + instrs.get(id));

			if(instrs.get(id).equals("Freshman"))
				raise = 1.04;
			if(instrs.get(id).equals("Sophomore"))
				raise = 1.06;
			if(instrs.get(id).equals("Junior"))
				raise = 1.08;
			if(instrs.get(id).equals("Senior"))
				raise = 1.10;

			String str0 = "Update Instructor Set Instructor.Salary = CASE WHEN Instructor.InstructorID = \"";
			//ID here
			String str1 = "\" then Instructor.Salary*";
			//raise here
			String str2 = " ELSE Instructor.Salary END";

			String update = str0 + id + str1 + raise + str2;
			//System.out.println("Update is: " + update);
			state.executeUpdate(update);
			System.out.println("Successfully updated the instructor salaries");
		}
	}
	
	private static int compareClassification(HashMap<String, Integer> classification, String a, String b) {
		if(classification.get(a) < classification.get(b))
			return -1;
		if(classification.get(a) > classification.get(b))
			return 1;
		return 0;
	}

	// E)
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
	public static void closeOut_F(Connection conn, Statement state) throws SQLException {

		System.out.println("\n\nF)");
		state.executeUpdate("drop table MeritList");
		state.close();
		conn.close();

		System.out.println("Closed all statements and connections successfully");
	}

}