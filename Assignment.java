 /*=====================================*\
|  Rushil Gala-Shah (u1515140)          |
|  CS258 - Database Systems Coursework  |
\*=====================================*/
/**
 * ======================================
 * START INITIALISATION
 * ======================================
 * STUDENT TABLE
 * ======================================
 * The student table contains 4 attributes
 * The student id being the only unique identifier therefore is the primary key
 * It is specified to use char(4) for the student id
 * Both student name and course name contain "name" so are type varchar(30) as specified
 * Year has been defined as smallint as specified
CREATE TABLE Student (
  Student_id char(4) PRIMARY KEY,
  Student_name varchar(30),
  Course_name varchar(30),
  Year smallint
);
 * ======================================
 * MODULE TABLE
 * ======================================
 * The module code is the only unique attribute therefore becomes the primary key
 * The type for module code is specified to be varchar(6)
 * The Department_name has "name" within it so is type varchar(30) as specified
CREATE TABLE Module (
  Module_code varchar(6) PRIMARY KEY,
  Module_name varchar(30),
  Department_name varchar(30)
);
 * ======================================
 * HISTORY TABLE
 * ======================================
 * The Delivery_year is type smallint as specified
 * The module_code is type varchar(3) as specified
 * The Organiser_name has "name" within it so it is of type varchar(30)
 * History can only have actual modules that have been defined in the Module TABLE
 * Therefore, module code must reference the module_code within the Module table
 * The Primary key is a composite key containing the Module_code and Delivery_year
 * This to make records unique as a module can only be ran at most once a year
 * However, a module can be run for multiple years therefore to create an indentifier
 * Module_code and Delivery_year can e combined to create the primary key
CREATE TABLE History (
  Delivery_year smallint,
  Module_code varchar(30),
  Organiser_name varchar(30),
  FOREIGN KEY (Module_code) REFERENCES Module(Module_code),
  PRIMARY KEY (Module_code,Delivery_year)
);
 * ======================================
 * EXAM TABLE
 * ======================================
 * The Exam table contains student id which must only contain records related to the Student table
 * Therefore a foreign key is required to the relevant record in the Student table
 * The Module_code is a reference to a particular record in the Module table
 * This means the Module_code must reference the Module_code in the Module table as a Foreign Key constraint
 * The Score and Exam_year are both smallint as specified
 * The Primary key is once again a composite key consisting of the Student_id and Module_code
 * As a student can be examined at most once, then the year is irrelevant.
 * A student can take multiple modules in a given year, and many students can take a module in a given year
 * As both student_id and Module_code are unique, the combination of both makes sure that each row in the table
 * can be uniquely identified by the values in the key
CREATE TABLE Exam (
  Student_id char(4),
  Module_code varchar(6),
  Exam_year smallint,
  Score smallint,
  FOREIGN KEY (Student_id) REFERENCES Student(Student_id),
  FOREIGN KEY (Module_code) REFERENCES Module(Module_code),
  PRIMARY KEY (Student_id,Module_code)
);
 * ======================================
 * PREREQUISITES TABLE
 * ======================================
 * This table contains two attributes: Module_code and Prerequisite_code
 * Both have type varchar(6) and are references to Module_code
 * Module_code depends on Prerequisite_code
 * Both attributes can only contain values that are in the Module table
CREATE TABLE Prerequisites (
  Module_code varchar(6),
  Prerequisite_code varchar(6),
  FOREIGN KEY (Module_code) REFERENCES Module(Module_code),
  FOREIGN KEY (Prerequisite_code) REFERENCES Module(Module_code)
);
 * ======================================
 * END INITIALISATION
 * ======================================
 */
import java.sql.*;
import java.io.*;

public class Assignment {

  public static void main (String args[]) throws Exception, IOException, SQLException {
    try {
      Class.forName ("oracle.jdbc.driver.OracleDriver");
    } catch (ClassNotFoundException e) {
      System.out.println ("Could not load the driver");
    }
    String user = "ops$u1515140";
    String pass = "password";
	user = readEntry("Enter userid: ");
	pass = readEntry("Enter password: ");
    Connection conn = DriverManager.getConnection (
                      "jdbc:oracle:thin:@daisy.warwick.ac.uk:1521:daisy",user,pass);

    boolean done = false;
    do {
      printMenu();
      System.out.print("Enter your choice: ");
      System.out.flush();
      String ch = readLine();
      System.out.println();
      switch (ch.charAt(0)) {
        case '1': modByStud(conn);
                  break;
        case '2': ghostMod(conn);
                  break;
        case '3': popRat(conn);
                  break;
	      case '4': topStud(conn);
		              break;
	      case '5': harshRank(conn);
		              break;
	      case '6': leafMod(conn);
		              break;
	      case '7': riskExam(conn);
		              break;
	      case '8': twistPrereq(conn);
		              break;
	      case '0': done = true;
                  break;
        default : System.out.println(" Not a valid option ");
      } //switch
    } while(!done);

    conn.close();
  } // main

  /**
   *  ===================================
   *  START TEST DATA
   *  ===================================
   *  ===================================
   *  MODULES
   *  Very simple, bare data
   *  ===================================
   *  INSERT INTO Module VALUES ('CS118', 'Programming for CS', 'Computer Science');
   *  INSERT INTO Module VALUES ('CS126', 'Design of IS', 'Computer Science');
   *  INSERT INTO Module VALUES ('CS130', 'Maths for CS 1', 'Computer Science');
   *  INSERT INTO Module VALUES ('CS131', 'Maths for CS 2', 'Computer Science');
   *  INSERT INTO Module VALUES ('CS240', 'Software Engineering', 'Computer Science');
   *  INSERT INTO Module VALUES ('CS241', 'OS and CN', 'Computer Science');
   *  INSERT INTO Module VALUES ('CS242', 'Spec and Veri', 'Computer Science');
   *  INSERT INTO Module VALUES ('CS243', 'Data structure', 'Computer Science');
   *  ===================================
   *  PREREQUISITES
   *  Used in options 6-8
   *  Special attention to CS242 (prerequisite to module is itself)
   *  Special attention to CS242/CS243 (prerequisite and module are prerequisites to eachother)
   *  Special attention to CS118->CS131->CS130->CS126->CS118 - Circular dependency
   *  Special attention to CS241 - SHOULD NOT be a circular dependency
   *  ===================================
   *  INSERT INTO Prerequisites VALUES ('CS131', 'CS130');
   *  INSERT INTO Prerequisites VALUES ('CS241', 'CS118');
   *  INSERT INTO Prerequisites VALUES ('CS241', 'CS126');
   *  INSERT INTO Prerequisites VALUES ('CS242', 'CS242');
   *  INSERT INTO Prerequisites VALUES ('CS242', 'CS243');
   *  INSERT INTO Prerequisites VALUES ('CS243', 'CS242');
   *  INSERT INTO Prerequisites VALUES ('CS118', 'CS131');
   *  INSERT INTO Prerequisites VALUES ('CS130', 'CS126');
   *  INSERT INTO Prerequisites VALUES ('CS126', 'CS118');
   *  ===================================
   *  STUDENTS
   *  Very simple, bare data
   *  ===================================
   *  INSERT INTO Student VALUES ('8125', 'Test Name', 'Computer Science', 1);
   *  INSERT INTO Student VALUES ('1235', 'Test Name2', 'Computer Science', 2);
   *  INSERT INTO Student VALUES ('7629', 'Test Name3', 'Computer Science', 1);
   *  INSERT INTO Student VALUES ('4192', 'Test Name4', 'Computer Science', 2);
   *  INSERT INTO Student VALUES ('5183', 'Test Name6', 'Computer Science', 2);
   *  ===================================
   *  EXAMS
   *  Insertion of exams for each Student
   *  Special attention to the CS240 module result for 2015 for student 4192 as low score for harshness
   *  Special attention to student 8125 & 5183 for consistent 90s for top students
   *  ===================================
   *  INSERT INTO Exam VALUES ('8125', 'CS118', 2016, 90);
   *  INSERT INTO Exam VALUES ('7629', 'CS118', 2016, 80);
   *  INSERT INTO Exam VALUES ('1235', 'CS118', 2015, 70);
   *  INSERT INTO Exam VALUES ('4192', 'CS118', 2015, 90);
   *  INSERT INTO Exam VALUES ('8125', 'CS130', 2016, 90);
   *  INSERT INTO Exam VALUES ('1235', 'CS130', 2015, 70);
   *  INSERT INTO Exam VALUES ('4192', 'CS130', 2015, 90);
   *  INSERT INTO Exam VALUES ('8125', 'CS126', 2016, 90);
   *  INSERT INTO Exam VALUES ('1235', 'CS126', 2015, 70);
   *  INSERT INTO Exam VALUES ('1235', 'CS241', 2016, 70);
   *  INSERT INTO Exam VALUES ('4192', 'CS241', 2016, 90);
   *  INSERT INTO Exam VALUES ('8125', 'CS131', 2016, 90);
   *  INSERT INTO Exam VALUES ('7629', 'CS131', 2016, 90);
   *  INSERT INTO Exam VALUES ('5183', 'CS131', 2016, 90);
   *  INSERT INTO Exam VALUES ('1235', 'CS240', 2016, 70);
   *  INSERT INTO Exam VALUES ('4192', 'CS240', 2015, 10);
   *  ===================================
   *  HISTORY
   *  Insertion of history - result of 10 refers to Prof E for lowest scores
   *  ===================================
   *  INSERT INTO History VALUES (2015, 'CS118', 'Prof A');
   *  INSERT INTO History VALUES (2016, 'CS118', 'Prof A');
   *  INSERT INTO History VALUES (2015, 'CS126', 'Prof B');
   *  INSERT INTO History VALUES (2016, 'CS126', 'Prof B');
   *  INSERT INTO History VALUES (2015, 'CS130', 'Prof C');
   *  INSERT INTO History VALUES (2016, 'CS130', 'Prof C');
   *  INSERT INTO History VALUES (2016, 'CS131', 'Prof D');
   *  INSERT INTO History VALUES (2016, 'CS240', 'Prof E');
   *  INSERT INTO History VALUES (2016, 'CS241', 'Prof F');
   *  INSERT INTO History VALUES (2015, 'CS240', 'Prof E');
   *  ===================================
   *  END TEST DATA
   *  ===================================
   */



  /**
   *  Case 1: Modules by Student
   *
   *  Description: Prints out line by line the modules each student has taken exams in
   *  The first query executed is one that gets all the student_ids from the student table in a result select
   *  This result set is looped through and a second query is used to get all the modules found for that student in the exam table
   *  The second resultset is looped through, printing out a space and the module code
   *  Validation: Using the test data provided, 5 students are added and 8 modules are added. 16 exams are taken in total.
   *  The reason I have used two queries instead of a nested query is for simplicity and efficiency.
   *  It also allows for a cleaner layout and no complexities.
   *  Expected output:
   *  Student 8125's modules: CS118, CS130, CS126, CS131
   *  Student 1235's modules: CS118, CS126, CS130, CS240, CS241
   *  Student 7629's modules: CS118, CS131
   *  Student 4192's modules: CS118, CS130, CS240, CS241
   *  Student 5183's modules: CS131
   *  Actual output:
   *  Enter your choice: 1
   *
   *  1235: CS118 CS126 CS130 CS240 CS241
   *  4192: CS118 CS130 CS240 CS241
   *  5183: CS131
   *  7629: CS118 CS131
   *  8125: CS118 CS126 CS130 CS131
   */
  private static void modByStud(Connection conn) throws SQLException, IOException {
    Statement stids = conn.createStatement();
    // Get all student ids
    String sqlStudents = null;
    sqlStudents = "SELECT Student_id FROM Student";
    ResultSet cset;
    try {
      cset = stids.executeQuery(sqlStudents);
      // Check to see if we actually have students in the database
      if (cset == null) {
        System.out.println("There are no students in the database.");
        stids.close();
        return;
      } else {
        // Create a new statement for getting the exam modules
        Statement modco = conn.createStatement();
        while (cset.next()) {

          String sid = cset.getString(1);
          System.out.print(sid + ":");
          ResultSet r;
          try {
              r = modco.executeQuery("SELECT Module_code FROM Exam WHERE Student_id='" + sid + "'");
              while (r.next()) {
                System.out.print(" " + r.getString(1));
              }
              System.out.println();
              // Free up resources
              r.close();
          } catch (SQLException e) {
              System.out.println("Could not execute Query");
              modco.close();
              return;
          }
        }
        // Free up resources
        modco.close();
      }
      // Free up resources
      cset.close();
    } catch (SQLException e) {
        System.out.println("Could not execute Query");
        stids.close();
        return;
    } finally {
      // Free up resources
      stids.close();
    }
  }

  /**
   *  Case 2: Ghost modules
   *
   *  Description: Returns the module codes which no one has taken an exam in
   *  Validation: List all the modules_codes from Module and remove (MINUS) all the module codes that appear in Exam
   *  A MINUS function is used rather than a JOIN or a NOT EXISTS purely because a JOIN would return unneeded data
   *  hence efficiency, and a NOT EXISTS is much more complex than a simple MINUS. As there is a unique module_code in
   *  Module, there is no need to specify a unique constraint or groups.
   *  Expected output: Using the test data, CS242 and CS243 are ghost modules
   *  Actual output:
   *  Enter your choice: 2
   *
   *  CS242 CS243
   */
  private static void ghostMod(Connection conn) throws SQLException, IOException {
    Statement modco = conn.createStatement();
    ResultSet rs;
    String getModule = null;
    getModule = "SELECT Module_code FROM Module " +
                "MINUS " +
                "SELECT Module_code FROM Exam";

    try {
      rs = modco.executeQuery(getModule);
      if (rs == null) {
        System.out.println("There are no ghost modules");
      } else {
        while (rs.next()) {
          System.out.print(rs.getString(1) + " ");
        }
        System.out.println();
      }
      rs.close();
    } catch (SQLException e) {
      System.out.println("Could not execute Query");
      modco.close();
      return;
    } finally {
      if (modco != null) {
        modco.close();
      }
    }
  }

  /**
   *  Case 3: Popularity ratings
   *
   *  Description: Returns the names of the modules in the order of how many times their module code appears in Exam
   *  Validation: The first query returns the module codes in order of how many times they appear in Exam from highest to lowest
   *  The second query takes each module code and returns the name from Module table
   *  Ghost modules are included by using a left join so all modules in Module table are accounted for
   *  Modules that appear the same number of times are in any order
   *  The reason two queries are used is to simply break down the problem. The first query solves the more complex problem
   *  of ordering the Module_codes in order of popularity, and the second has the simple job of retrieving the name of the
   *  module.
   *  Expected output:
   *  CS118 - Appears 4 times (name is "Programming for CS")
   *  CS130 - Appears 3 times (name is "Maths for CS 1")
   *  CS131 - Appears 3 times (name is "Maths for CS 2")
   *  CS126 - Appears 2 times (name is "Design of IS")
   *  CS241 - Appears 2 times (name is "OS and CN")
   *  CS240 - Appears 2 times (name is "Software Engineering")
   *  CS242 - Appears 0 times (name is "Spec and veri")
   *  CS243 - Appears 0 times (name is "Data Structures")
   *  Actual output:
   *  Enter your choice: 3
   *
   *  Programming for CS
   *  Maths for CS 2
   *  Maths for CS 1
   *  Software Engineering
   *  OS and CN
   *  Design of IS
   *  Data structure
   *  Spec and Veri
   */
  private static void popRat(Connection conn) throws SQLException, IOException {
    Statement modco = conn.createStatement();
    ResultSet rs;
    String getModule = null;
    getModule = "SELECT M.Module_code " +
                "FROM Module M LEFT JOIN Exam E " +
                "ON M.Module_code=E.Module_code " +
                "GROUP BY M.Module_code " +
                "ORDER BY COUNT(*) DESC";

    try {
      rs = modco.executeQuery(getModule);
      Statement modname = conn.createStatement();
      ResultSet rset;
      String name = null;
      if (rs == null) {
        System.out.println("There are no modules");
      } else {
        while (rs.next()) {
          name = "SELECT Module_name FROM Module WHERE Module_code='" + rs.getString(1) + "'";
          try {
            rset = modname.executeQuery(name);
            rset.next();
            System.out.println(rset.getString(1));
          } catch (SQLException e) {
            System.out.println("Could not execute Query");
            modname.close();
            return;
          }
        }
        System.out.println();
      }
      modname.close();
      rs.close();
    } catch (SQLException e) {
      System.out.println("Could not execute Query");
      modco.close();
      return;
    } finally {
      if (modco != null) {
        modco.close();
      }
    }
  }

  /**
   *  Case 4: Top Student(s)
   *
   *  Description: Returns the student(s) with the highest average score
   *  Validation: A temporary table is created consisting of student ids and average scores from the exam table
   *  This temporary table is used in the first query which grabs the student id where the average score is equal to MAX
   *  This allows us to select multiple student ids who have got the same average score hence there are multiple the highest average score
   *  The second query uses the student id to get the name
   *  Much like the previous case, the reason two queries revolves around a more complex query for the first solution
   *  and the second query has the simpler job of retrieving the student name
   *  Expected output: Using the test data, the average of all students:
   *  8195: (90+90+90+90)/4    = 90
   *  7629: (90+80)/2          = 85
   *  5183: (90)/1             = 90
   *  1235: (70+70+70+70+70)/5 = 70
   *  4192: (90+90+90+10)/4    = 70
   *  Therefore Student 8195 ("Test Name") & Student 5183 ("Test Name4") will have their names printed
   *  Actual output:
   *  Enter your choice: 4
   *
   *  Test Name4
   *  Test Name
   */
  private static void topStud(Connection conn) throws SQLException, IOException {
    Statement ts = conn.createStatement();
    ResultSet rs;
    String getStudent = null;
    getStudent = "SELECT Student_name FROM Student WHERE Student_id IN " +
                 "(SELECT Student_id FROM " +
                 "(SELECT Student_id,AVG(Score) AS avg_score FROM Exam GROUP BY Student_id) " +
                 "WHERE avg_score=(SELECT MAX(avg_score) " +
                 "FROM (SELECT Student_id,AVG(Score) AS avg_score FROM Exam GROUP BY Student_id)))";
    try {
      rs = ts.executeQuery(getStudent);
      Statement top = conn.createStatement();
      ResultSet rset;
      String getName = null;
      if (rs == null) {
        System.out.println("There are no students");
      } else {
        while (rs.next()) {
          getName = "SELECT Student_name FROM Student WHERE Student_id='" + rs.getString(1) + "'";
          try {
            rset = top.executeQuery(getName);
            rset.next();
            System.out.println(rset.getString(1));
          } catch (SQLException e) {
            System.out.println("Could not execute Query");
            top.close();
            return;
          }
        }
        System.out.println();
      }
      top.close();
      rs.close();
    } catch (SQLException e) {
      System.out.println("Could not execute Query");
      ts.close();
      return;
    } finally {
      if (ts != null) {
        ts.close();
      }
    }
  }

  /**
   *  Case 5: Harshness ranking
   *
   *  Description: Returns a list of Organiser's names in the order of average scores they were involved in
   *  from the lowest to the highest
   *  Validation: The query selects the organiser name by joining the exam and history tables
   *  with the module code and year being the identifiers and orders it by the grouped average score
   *  An INNER JOIN was used as we only want the organiser names who had students take their exam, and every exam has
   *  an organiser. As both exam and history have a composite primary key, we needed to have two conditions in the ON
   *  condition. The Order By completes the specification and the GROUP BY keyword allows average scores for each organiser
   *  Expected output:
   *  6 Professors:
   *  Prof A (CS118 in 2015 & 2016): (70+90+80+90)/4 = 82.5
   *  Prof B (CS126 in 2015 & 2016): (90+70)/2       = 80
   *  Prof C (CS130 in 2015 & 2016): (90+70+90)/3    = 83.3
   *  Prof D (CS131 in 2016): (90+90+90)/3           = 90
   *  Prof E (CS240 in 2015 & 2016): (70+10)/2       = 40
   *  Prof F (CS241 in 2016): (70+90)/2              = 80
   *  Prof E -> (Prof B/Prof F) -> Prof A -> Prof C -> Prof D
   *  Actual output:
   *  Enter your choice: 5
   *
   *  Prof E
   *  Prof F
   *  Prof B
   *  Prof A
   *  Prof C
   *  Prof D
   */
  private static void harshRank(Connection conn) throws SQLException, IOException {
    Statement hr = conn.createStatement();
    ResultSet rs;
    String getProf = null;
    getProf = "SELECT Organiser_name " +
              "FROM Exam E INNER JOIN History H " +
              "ON E.Module_code=H.Module_code AND E.Exam_year=H.Delivery_year " +
              "GROUP BY Organiser_name " +
              "ORDER BY AVG(Score)";
    try {
      rs = hr.executeQuery(getProf);
      if (rs == null) {
        System.out.println("There are no modules");
      } else {
        while (rs.next()) {
          System.out.println(rs.getString(1));
        }
        System.out.println();
      }
      rs.close();
    } catch (SQLException e) {
      System.out.println("Could not execute Query");
      hr.close();
      return;
    } finally {
      if (hr != null) {
        hr.close();
      }
    }
  }

  /**
   *  Case 6: Leaf modules
   *
   *  Description: Returns a line consisting of modules which do not appear on the Prerequisites table under Module_code
   *  Validation: Takes all the modules from the Module_code and removes any Module code that appears on the Prerequisites table under Module_code
   *  A simple MINUS query is used as it is the most efficient course of action as well as the simplest.
   *  This allows the query to be clean and direct.
   *  Expected output:
   *  From the test data, only CS240 does not appear on the prerequisites table under Module code
   *  As we are only dealing with two tables, a minus query is used
   *  Actual output:
   *  Enter your choice: 6
   *
   *  CS240
   */
  private static void leafMod(Connection conn) throws SQLException, IOException {
    Statement lm = conn.createStatement();
    ResultSet rs;
    String getLeaf = null;
    getLeaf = "(SELECT Module_code FROM Module) MINUS (SELECT Module_code FROM Prerequisites)";
    try {
      rs = lm.executeQuery(getLeaf);
      if (rs == null) {
        System.out.println("There are no modules");
      } else {
        while (rs.next()) {
          System.out.print(rs.getString(1) + " ");
        }
        System.out.println();
      }
      rs.close();
    } catch (SQLException e) {
      System.out.println("Could not execute Query");
      lm.close();
      return;
    } finally {
      if (lm != null) {
        lm.close();
      }
    }
  }

  /**
   *  Case 7: Risky exams
   *
   *  Description: Returns a single line consisting of student ids who have taken exams but not the prerequisite exams (if any!)
   *  Validation: Selecting student ids where there are prerequisite modules after removing the modules the student has already taken
   *  For each record in Exam, select the prerequisite_codes which the Module code is equal to the Module_code from Exam
   *  Remove the module codes the student has already completed from the list of prerequisite codes
   *  If there are any prerequisite_codes left, then select the student id - group by student_id so we dont get duplicates
   *  WHERE EXISTS was used as we only want the student ids where there are prerequisites left, and a minus query was used
   *  because it is simple enough to use and the most efficient.
   *  Expected output:
   *  List of students with exams -
   *  1235: CS118 (requires CS131 [NO]) CS126 (requires CS118 [YES]) CS130 (requires CS126 [YES]) CS240 CS241 (requires CS118 [YES] & CS126[YES])
   *  4192: CS118 (requires CS131 [NO]) CS130 (requires CS126 [NO]) CS240 CS241 (requires CS118 [YES] & CS126[NO])
   *  5183: CS131 (requires CS130 [NO])
   *  7629: CS118 (requires CS131 [YES]) CS131 (requires CS130 [NO])
   *  8125: CS118 (requires CS131 [YES]) CS126 (requires CS118 [YES]) CS130 (requires CS126 [YES]) CS131 (requires CS130 [YES])
   *  1235, 4192, 5183, 7629
   *  Actual output:
   *  Enter your choice: 7
   *
   *  1235 4192 5183 7629
   */
  private static void riskExam(Connection conn) throws SQLException, IOException {
    Statement re = conn.createStatement();
    ResultSet rs;
    String getStud = null;
    getStud = "SELECT Student_id FROM Exam E " +
             "WHERE EXISTS " +
             "((SELECT prerequisite_code FROM Prerequisites P WHERE P.Module_code=E.Module_code) " +
             "MINUS " +
             "(SELECT module_code FROM Exam R WHERE E.Student_id=R.Student_id)) " +
             "GROUP BY Student_id";
    try {
      rs = re.executeQuery(getStud);
      if (rs == null) {
        System.out.println("There are no students");
      } else {
        while (rs.next()) {
          System.out.print(rs.getString(1) + " ");
        }
        System.out.println();
      }
      rs.close();
    } catch (SQLException e) {
      System.out.println("Could not execute Query");
      re.close();
      return;
    } finally {
      if (re != null) {
        re.close();
      }
    }
  }

  /**
   *  Case 8: Twisted prerequisites
   *
   *  Description: Returns a line consisting of module_codes who's prerequisites cannot be satisfied
   *  Validation: A circular dependecy can occur whenever a loop occurs. The solution to this is to use CONNECT
   *  and find if there is a CYCLE. If there is a cycle then, the iscycle value is 1 and the root module_code
   *  is added to the list
   *  CONNECT_BY functions were used as there is an in-built function to detect cycles. This is much more efficient than
   *  recursion in terms of speed management.
   *  Expected output:
   *  List of prerequisites -
   *  (CS131 -> CS130)
   *  (CS241 -> CS118)
   *  (CS241 -> CS126)
   *  (CS242 -> CS242)
   *  (CS243 -> CS242)
   *  (CS242 -> CS243)
   *  (CS118 -> CS131)
   *  (CS130 -> CS126)
   *  (CS126 -> CS118)
   *  CIRCULAR DEPENDENCY: CS242 -> CS242, CS131 -> CS130 -> CS126 -> CS118 -> CS131, CS243 -> CS242 -> CS243, CS241 -> CS118 [LOOP], CS241 -> CS126 [LOOP]
   *  CS242, CS131, CS130, CS126, CS118, CS243, CS241
   *  Actual output:
   *  Enter your choice: 8
   *
   *  CS126 CS130 CS241 CS242 CS118 CS131 CS243
   */
  private static void twistPrereq(Connection conn) throws SQLException, IOException {
    Statement tp = conn.createStatement();
    ResultSet rs;
    String getMod = null;
    // Ways of getting circular dependency
    // 1. A module is a prerequisite of itself.
    // 2. Recursive: Module A is dependent on Module B, Module B is dependent on Module C & Module C is dependent on Module A
    // 3. Module D's prerequisite is Module A which causes a loop, so Module D must be added to the list
    getMod = "SELECT DISTINCT CONNECT_BY_ROOT(Module_code) " +
             "FROM Prerequisites " +
             "WHERE CONNECT_BY_ISCYCLE = 1 " +
             "CONNECT BY NOCYCLE " +
             "Module_code = PRIOR Prerequisite_code";
    try {
      rs = tp.executeQuery(getMod);
      if (rs == null) {
        System.out.println("There are no modules");
      } else {
        while (rs.next()) {
          System.out.print(rs.getString(1) + " ");
        }
        System.out.println();
      }
      rs.close();
    } catch (SQLException e) {
      System.out.println("Could not execute Query");
      tp.close();
      return;
    } finally {
      if (tp != null) {
        tp.close();
      }
    }
  }

  private static String readEntry(String prompt) {
    try {
      StringBuffer buffer = new StringBuffer();
      System.out.print(prompt);
      System.out.flush();
      int c = System.in.read();
      while(c != '\n' && c != -1) {
        buffer.append((char)c);
        c = System.in.read();
      }
      return buffer.toString().trim();
    } catch (IOException e) {
       return "";
      }
    }

  private static String readLine() {
    InputStreamReader isr = new InputStreamReader(System.in);
    BufferedReader br = new BufferedReader(isr, 1);
    String line = "";

    try {
      line = br.readLine();
    } catch (IOException e) {
      System.out.println("Error in SimpleIO.readLine: " +
                         "IOException was thrown");
      System.exit(1);
    }
    return line;
  }

  private static void printMenu() {
    System.out.println("\n        Menu ");
    System.out.println("(1) Modules by student. ");
    System.out.println("(2) Ghost modules. ");
    System.out.println("(3) Popularity ratings. ");
    System.out.println("(4) Top student(s). ");
    System.out.println("(5) Harshness ranking. ");
    System.out.println("(6) Leaf modules. ");
    System.out.println("(7) Risky exams. ");
    System.out.println("(8) Twisted prerequisites. ");
    System.out.println("(0) Quit. \n");
  }

}
