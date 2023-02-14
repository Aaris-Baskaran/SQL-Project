/*
 * LibraryModel.java
 * Author: Aaris Baskaran
 * Created on: 04/06/22
 */



import java.sql.*;
import javax.swing.*;

public class LibraryModel {

    // For use in creating dialogs and making them modal
    private JFrame dialogParent;
    private Connection con = null;

    public LibraryModel(JFrame parent, String userid, String password) {
        dialogParent = parent;
        userid = "baskaraari";
        password = "p2";
        //Register a PostgreSQL Driver
        try{
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException cnfe){
            System.out.println("Can not find the driver class: I have not installed it properly");
        }

        //Register a PostgreSQL Driver
        try {
            con = DriverManager.getConnection("jdbc:postgresql://db.ecs.vuw.ac.nz/"+userid+"_jdbc", userid, password);
        } catch (SQLException e) {
            System.out.println("Can not connect");
            System.out.println(e.getMessage());
        }
    }

    public String bookLookup(int isbn) {
        String ret = "Book Lookup: \n";

        // Create a Statement object
        Statement s = createStatement();

        // Execute the Statement object
        String sql = "SELECT * FROM book " +
                "WHERE isbn = "+isbn+";";

        ResultSet rs = doQuery(sql, s);

        // Create another Statement object
        Statement s2 = createStatement();

        // Execute another Statement
        sql = "SELECT a.surname " +
                "FROM author a, book_author b " +
                "WHERE isbn = "+isbn+" AND a.authorid = b.authorid " +
                "ORDER BY a.surname ASC;";

        ResultSet rs2 = doQuery(sql, s2);

        // Handle query answer in ResultSet objects
        try {
            while(rs.next()) {
                // Get the book details
                ret += "\t" + rs.getString(1) + ": ";
                ret += rs.getString(2) + "\n";
                ret += "\t" + "Edition: " + rs.getString(3) + " - ";
                ret += "Number of copies: " + rs.getString(4) + " - ";
                ret += "Copies left: " + rs.getString(5) + "\n";
            }

            // Get the author details
            ret += "\tAuthors: ";
            while(rs2.next()){
                ret += removeSpaces(rs2.getString(1));
                ret += ", ";
            }
            ret = ret.substring(0, ret.length()-2) + "\n";     //remove last comma

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public String showCatalogue() {
        String ret = "Show Catalogue: ";

        // Create a Statement object
        Statement s = createStatement();

        // Execute the Statement object
        String sql = "SELECT isbn, title, edition_no, numofcop, numleft " +
                "FROM book " +
                "ORDER BY isbn ASC;";

        ResultSet rs = doQuery(sql, s);

        // Handle query answer in ResultSet object
        try {

            // Get the book details for each book
            while(rs.next()) {
                // look through each book, get the details and the authors
                String isbn = rs.getString(1);
                ret += "\n\n\t" + isbn + ": ";
                ret += rs.getString(2) + "\n";
                ret += "\t" + "Edition: " + rs.getString(3) + " - ";
                ret += "Number of copies: " + rs.getString(4) + " - ";
                ret += "Copies left: " + rs.getString(5) + "\n";

                // get the author details
                Statement s2 = createStatement();
                String sql2 = "SELECT surname " +
                        "FROM author a, book_author ba " +
                        "WHERE ba.isbn = " + isbn + " AND ba.authorid = a.authorid " +
                        "ORDER BY ba.authorseqno ASC;";
                ResultSet rs2 = doQuery(sql2, s2);

                String auth = "";
                try {
                    while(rs2.next()){
                        auth += removeSpaces(rs2.getString(1)) + ", ";
                    }
                }catch (SQLException e){
                    e.printStackTrace();
                }

                if(auth.equals("")) ret += "\t(no authors)";
                else {
                    ret += "\tAuthors: " + auth;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public String showLoanedBooks() {
        String ret = "Show Loaned Books: ";

        // Create a Statement object
        Statement s = createStatement();

        // Execute the Statement object
        String sql = "SELECT * " +
                "FROM cust_book " +
                "ORDER BY isbn ASC;";

        ResultSet rs = doQuery(sql, s);

        // Handle query answer in ResultSet object
        try {
            while(rs.next()) {
                // look through each borrowed book, get the book details and the borrowers
                String isbn = rs.getString(1);
                String customerid = rs.getString(3);

                // Get the Book details
                Statement bookQ = createStatement();
                String booksql = "SELECT * " +
                        "FROM book " +
                        "WHERE isbn = " + isbn + ";";
                ResultSet resB = doQuery(booksql, bookQ);

                try{
                    while(resB.next()) {
                        ret += "\n\n\t" + isbn + ": ";
                        ret += resB.getString(2) + "\n";
                        ret += "\t" + "Edition: " + resB.getString(3) + " - ";
                        ret += "Number of copies: " + resB.getString(4) + " - ";
                        ret += "Copies left: " + resB.getString(5) + "\n";

                        Statement s2 = createStatement();
                        String sql2 = "SELECT surname " +
                                "FROM author a, book_author ba " +
                                "WHERE ba.isbn = " + isbn + " AND ba.authorid = a.authorid " +
                                "ORDER BY ba.authorseqno ASC;";
                        ResultSet rs2 = doQuery(sql2, s2);

                        String auth = "";
                        try {
                            while(rs2.next()){
                                auth += removeSpaces(rs2.getString(1)) + ", ";
                            }
                        }catch (SQLException e){
                            e.printStackTrace();
                        }

                        if(auth.equals("")) ret += "\t(no authors)\n";
                        else {
                            ret += "\tAuthors: " + auth;
                        }
                    }
                }catch (SQLException e){
                    e.printStackTrace();
                }

                // Get the borrowers
                Statement custQ = createStatement();
                String custsql = "SELECT * " +
                        "FROM customer " +
                        "WHERE customerid = " + customerid + ";";
                ResultSet resC = doQuery(custsql, custQ);
                try{
                    ret += "\n\tBorrowers: \n";
                    while(resC.next()) {
                        ret += "\t\t" + resC.getString(1) + ": ";
                        ret += removeSpaces(resC.getString(2)) + ", ";
                        ret += removeSpaces(resC.getString(3)) + " - ";
                        ret += removeSpaces(resC.getString(4)) + "\n";
                    }
                }catch (SQLException e){
                    e.printStackTrace();
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public String showAuthor(int authorID) {
        String ret = "Show Author: \n";

        // Create a Statement object
        Statement s = createStatement();

        // Execute the Statement object
        String sql = "SELECT a.authorid, a.name, a.surname, b.isbn, b.title " +
                "FROM author a, book b, book_author ba " +
                "WHERE a.authorid = " + authorID + " AND ba.authorid = " + authorID + " AND ba.isbn = b.isbn;";

        ResultSet rs = doQuery(sql, s);

        // Handle query answer in ResultSet objects
        try {
            boolean first = true;
            while(rs.next()) {
                if(first){
                    first = false;

                    // Get the author details
                    ret += "\t" + rs.getString(1) + ": ";
                    ret += removeSpaces(rs.getString(3)) + ", ";
                    ret += removeSpaces(rs.getString(2)) + "\n";

                    // Get the book details
                    ret += "\tBooks written:\n";
                }
                ret += "\t\t" + rs.getString(4) + " - " + rs.getString(5) + "\n";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public String showAllAuthors() {
        String ret = "All Authors: \n";

        // Create a Statement object
        Statement s = createStatement();

        // Execute the Statement object
        String sql = "SELECT * FROM author;";

        ResultSet rs = doQuery(sql, s);

        // Handle query answer in ResultSet objects
        try {
            while(rs.next()) {
                // Get the author details
                ret += "\t" + rs.getString(1) + ": ";
                ret += removeSpaces(rs.getString(3)) + ", ";
                ret += removeSpaces(rs.getString(2)) + "\n";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public String showCustomer(int customerID) {
        String ret = "Show Customer: \n";

        // Create a Statement object
        Statement s = createStatement();

        // Execute the Statement object
        String sql = "SELECT c.customerid, c.l_name, c.f_name, c.city, b.isbn, b.title " +
                "FROM customer c, cust_book cb, book b " +
                "WHERE c.customerid = " + customerID + " AND cb.customerid = " + customerID + " AND cb.isbn = b.isbn;";

        ResultSet rs = doQuery(sql, s);

        // Handle query answer in ResultSet objects
        try {
            boolean first = true;
            while(rs.next()) {
                if(first){
                    first = false;

                    // Get the customer details
                    ret += "\t" + rs.getString(1) + ": ";
                    ret += removeSpaces(rs.getString(2)) + ", ";
                    ret += removeSpaces(rs.getString(3)) + " - ";

                    //check if the city is empty or not
                    String city = removeSpaces(rs.getString(4));
                    if(city == null) {
                        ret += "(no city)" + "\n";
                    }else{
                        ret += city + "\n";
                    }

                    // Get the book details
                    ret += "\tBooks borrowed:\n";
                }
                ret += "\t\t" + rs.getString(5) + " - " + rs.getString(6) + "\n";
            }
            if(first) ret += "\tThis customer has not borrowed any books";

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public String showAllCustomers() {
        String ret = "All Customers: \n";

        // Create a Statement object
        Statement s = createStatement();

        // Execute the Statement object
        String sql = "SELECT * FROM customer;";

        ResultSet rs = doQuery(sql, s);

        // Handle query answer in ResultSet objects
        try {
            while(rs.next()) {
                // Get the customer details
                ret += "\t" + rs.getString(1) + ": ";
                ret += removeSpaces(rs.getString(2)) + ", ";
                ret += removeSpaces(rs.getString(3)) + " - ";

                //check if the city is empty or not
                String city = removeSpaces(rs.getString(4));
                if(city == null) {
                    ret += "(no city)" + "\n";
                }else{
                    ret += city + "\n";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public String borrowBook(int isbn, int customerID,
                             int day, int month, int year) {
        String ret = "Borrow Book: \n";

        //-- BEGIN AND LOCK CUSTOMER, BOOK --//
        Statement begin = createStatement();
        String beginSql = "BEGIN;";

        Statement lockC = createStatement();
        String lockSqlC = "SELECT * " +
                "FROM customer " +
                "WHERE customerid = " + customerID + " " +
                "FOR UPDATE;";

        Statement lockB = createStatement();
        String lockSqlB = "SELECT * " +
                "FROM book " +
                "WHERE isbn = " + isbn + " " +
                "FOR UPDATE;";

        doExecute(beginSql, begin);
        doQuery(lockSqlC, lockC);
        doQuery(lockSqlB, lockB);

        //-- CHECK IF THE CUSTOMER EXISTS --//
        Statement countC = createStatement();
        String sqlC = "SELECT COUNT(*) " +
                "FROM customer " +
                "WHERE customerid = " + customerID;

        ResultSet rsC = doQuery(sqlC, countC);

        // Handle query answer in ResultSet objects
        try {
            while(rsC.next()) {
                // Check if the customer exists
                if(rsC.getString(1).equals("0")) {
                    //JOptionPane.showMessageDialog(dialogParent, "Customer Doesn't Exist");
                    rollback();
                    return ret + "Customer Doesn't Exist";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //-- CHECK IF THE BOOK EXISTS --//
        Statement countB = createStatement();
        String sqlB = "SELECT COUNT(*) " +
                "FROM book " +
                "WHERE isbn = " + isbn;

        ResultSet rsB = doQuery(sqlB, countB);

        // Handle query answer in ResultSet objects
        try {
            while(rsB.next()) {
                // Check if the customer exists
                if(rsB.getString(1).equals("0")) {
                    JOptionPane.showMessageDialog(dialogParent, "Book Doesn't Exist");
                    rollback();
                    return ret + "Book Doesn't Exist";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //-- CHECK IF THE BOOK HAS COPIES LEFT --//
        Statement countBC = createStatement();
        String sqlBC = "SELECT numleft " +
                "FROM book " +
                "WHERE isbn = " + isbn;

        ResultSet rsBC = doQuery(sqlBC, countBC);

        // Handle query answer in ResultSet objects
        try {
            while(rsBC.next()) {
                // Check if the customer exists
                if(rsBC.getString(1).equals("0")) {
                    //JOptionPane.showMessageDialog(dialogParent, "Not enough copies left");
                    rollback();
                    return ret + "Not enough copies left";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        //-- INSERT TUPLE INTO CUST_BOOK --//
        Statement insert = createStatement();
        String sqlI = "INSERT INTO cust_book " +
                "VALUES('" + isbn + "','" + year + "-" + month + "-" + day + "','" + customerID + "');";
        int resI = doUpdate(sqlI,insert);
        if(resI == 0) {
            rollback();
            return ret + "\tCustomer already has this book on loan";
        }


        //-- UPDATE THE BOOK TABLE --//
        Statement updateB = createStatement();
        String sqlU = "UPDATE book " +
                "SET numleft = numleft - 1 " +
                "WHERE isbn = " + isbn + ";";
        int resU = doUpdate(sqlU,updateB);
        if(resU == 0) rollback();

        //-- COMMIT --//
        Statement commit = createStatement();
        String commitSql = "COMMIT;";
        doExecute(commitSql, commit);

        // Print the details
        String bookname = "";
        Statement bn = createStatement();
        String sqlbn = "SELECT title " +
                "FROM book " +
                "WHERE isbn = " + isbn;
        ResultSet rsbn = doQuery(sqlbn, bn);
        // Handle query answer in ResultSet objects
        try {
            while(rsbn.next()) {
                // Get the book name
                bookname = removeSpaces(rsbn.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String custname = "";
        Statement cn = createStatement();
        String sqlcn = "SELECT f_name, l_name " +
                "FROM customer " +
                "WHERE customerid = " + customerID;
        ResultSet rscn = doQuery(sqlcn, cn);
        // Handle query answer in ResultSet objects
        try {
            while(rscn.next()) {
                // Get the book name
                custname = removeSpaces(rscn.getString(1)) + removeSpaces(rscn.getString(2));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        ret += "\tBook: " + isbn + " (" + bookname + ")\n";
        ret += "\tLoaned to: " + customerID + " (" + custname + ")\n";
        ret += "\tDue Date: " + day + "-" + month + "-" + year;

        return ret;
    }


    public String returnBook(int isbn, int customerid) {
        String ret = "Return Book: \n";

        //-- BEGIN AND LOCK CUSTOMER, BOOK --//
        Statement begin = createStatement();
        String beginSql = "BEGIN;";

        Statement lockC = createStatement();
        String lockSqlC = "SELECT * " +
                "FROM customer " +
                "WHERE customerid = " + customerid + " " +
                "FOR UPDATE;";

        Statement lockB = createStatement();
        String lockSqlB = "SELECT * " +
                "FROM book " +
                "WHERE isbn = " + isbn + " " +
                "FOR UPDATE;";

        doExecute(beginSql, begin);
        doQuery(lockSqlC, lockC);
        doQuery(lockSqlB, lockB);

        //-- CHECK IF THE CUSTOMER EXISTS --//
        Statement countC = createStatement();
        String sqlC = "SELECT COUNT(*) " +
                "FROM customer " +
                "WHERE customerid = " + customerid;

        ResultSet rsC = doQuery(sqlC, countC);

        // Handle query answer in ResultSet objects
        try {
            while(rsC.next()) {
                // Check if the customer exists
                if(rsC.getString(1).equals("0")) {
                    //JOptionPane.showMessageDialog(dialogParent, "Customer Doesn't Exist");
                    rollback();
                    return ret + "Customer Doesn't Exist";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //-- CHECK IF THE BOOK EXISTS --//
        Statement countB = createStatement();
        String sqlB = "SELECT COUNT(*) " +
                "FROM book " +
                "WHERE isbn = " + isbn;

        ResultSet rsB = doQuery(sqlB, countB);

        // Handle query answer in ResultSet objects
        try {
            while(rsB.next()) {
                // Check if the customer exists
                if(rsB.getString(1).equals("0")) {
                    //JOptionPane.showMessageDialog(dialogParent, "Book Doesn't Exist");
                    rollback();
                    return ret + "Book Doesn't Exist";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //-- CHECK IF THE BOOK HAS BEEN BORROWED BY THIS CUSTOMER --//
        Statement countBC = createStatement();
        String sqlBC = "SELECT COUNT(*) " +
                "FROM cust_book " +
                "WHERE isbn = " + isbn + " AND customerid = " + customerid + ";";

        ResultSet rsBC = doQuery(sqlBC, countBC);

        // Handle query answer in ResultSet objects
        try {
            while(rsBC.next()) {
                // Check if the customer exists
                if(rsBC.getString(1).equals("0")) {
                    //JOptionPane.showMessageDialog(dialogParent, "Not enough copies left");
                    rollback();
                    return ret + "This Book Was Not Borrowed By This Customer";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //-- DELETE TUPLE FROM CUST_BOOK --//
        Statement delete = createStatement();
        String sqlI = "DELETE FROM cust_book " +
                "WHERE isbn = " + isbn + " AND customerid = " + customerid + ";";
        int resI = doUpdate(sqlI,delete);
        if(resI == 0) {
            rollback();
            return ret + "\tThis borrow record does not exist - the book may have already been returned";
        }


        //-- UPDATE THE BOOK TABLE --//
        Statement updateB = createStatement();
        String sqlU = "UPDATE book " +
                "SET numleft = numleft + 1 " +
                "WHERE isbn = " + isbn + ";";
        int resU = doUpdate(sqlU,updateB);
        if(resU == 0) rollback();


        //-- COMMIT --//
        Statement commit = createStatement();
        String commitSql = "COMMIT;";
        doExecute(commitSql, commit);


        // Print the details
        ret += "\tBook " + isbn + " returned for customer " + customerid;

        return ret;
    }

    public void closeDBConnection() {
        try {
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteCus(int customerID) {
        String ret = "Delete Customer: \n";

        //-- BEGIN AND LOCK CUSTOMER --//
        Statement begin = createStatement();
        String beginSql = "BEGIN;";

        Statement lockC = createStatement();
        String lockSqlC = "SELECT * " +
                "FROM customer " +
                "WHERE customerid = " + customerID + " " +
                "FOR UPDATE;";

        doExecute(beginSql, begin);
        doQuery(lockSqlC, lockC);

        //-- CHECK IF THE CUSTOMER EXISTS --//
        Statement countC = createStatement();
        String sqlC = "SELECT COUNT(*) " +
                "FROM customer " +
                "WHERE customerid = " + customerID;

        ResultSet rsC = doQuery(sqlC, countC);

        // Handle query answer in ResultSet objects
        try {
            while(rsC.next()) {
                // Check if the customer exists
                if(rsC.getString(1).equals("0")) {
                    //JOptionPane.showMessageDialog(dialogParent, "Customer Doesn't Exist");
                    rollback();
                    return ret + "Customer Doesn't Exist";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        //-- RETURN ANY BOOKS BORROWED BY THIS CUSTOMER --//
        // Get all books borrowed by this customer and return them
        Statement books = createStatement();
        String sqlB = "SELECT isbn, customerid FROM cust_book " +
                "WHERE customerid = " + customerID + ";";
        ResultSet rsB = doQuery(sqlB, books);

        // Handle query answer in ResultSet objects
        try {
            while(rsB.next()) {
                returnBook(Integer.parseInt(rsB.getString(1)), Integer.parseInt(rsB.getString(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //-- DELETE TUPLE FROM CUSTOMER --//
        Statement delete = createStatement();
        String sqlI = "DELETE FROM customer " +
                "WHERE customerid = " + customerID + ";";
        int resI = doUpdate(sqlI,delete);
        if(resI == 0) {
            rollback();
            return ret + "\tThis customer does not exist";
        }


        //-- COMMIT --//
        Statement commit = createStatement();
        String commitSql = "COMMIT;";
        doExecute(commitSql, commit);


        // Print the details
        ret += "\tCustomer " + customerID + " has been deleted";

        return ret;
    }

    public String deleteAuthor(int authorID) {
        String ret = "Delete Author: \n";

        //-- BEGIN AND LOCK AUTHOR --//
        Statement begin = createStatement();
        String beginSql = "BEGIN;";

        Statement lockC = createStatement();
        String lockSqlC = "SELECT * " +
                "FROM author " +
                "WHERE authorid = " + authorID + " " +
                "FOR UPDATE;";

        doExecute(beginSql, begin);
        doQuery(lockSqlC, lockC);

        //-- CHECK IF THE AUTHOR EXISTS --//
        Statement countC = createStatement();
        String sqlC = "SELECT COUNT(*) " +
                "FROM author " +
                "WHERE authorid = " + authorID;

        ResultSet rsC = doQuery(sqlC, countC);

        // Handle query answer in ResultSet objects
        try {
            while(rsC.next()) {
                // Check if the customer exists
                if(rsC.getString(1).equals("0")) {
                    //JOptionPane.showMessageDialog(dialogParent, "Customer Doesn't Exist");
                    rollback();
                    return ret + "Author Doesn't Exist";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


//        //-- RETURN ANY BOOKS BORROWED BY THIS CUSTOMER --//
//        // Get all books borrowed by this customer and return them
//        Statement books = createStatement();
//        String sqlB = "SELECT isbn, customerid FROM cust_book " +
//                "WHERE customerid = " + customerID + ";";
//        ResultSet rsB = doQuery(sqlB, books);
//
//        // Handle query answer in ResultSet objects
//        try {
//            while(rsB.next()) {
//                returnBook(Integer.parseInt(rsB.getString(1)), Integer.parseInt(rsB.getString(2)));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }


        //-- DELETE TUPLE FROM BOOK_AUTHOR --//
        Statement delete = createStatement();
        String sqlI = "DELETE FROM book_author " +
                "WHERE authorid = " + authorID + ";";
        int resI = doUpdate(sqlI,delete);
        if(resI == 0) {
            rollback();
            return ret + "\tThis customer does not exist";
        }


        //-- DELETE TUPLE FROM AUTHOR --//
        Statement delete2 = createStatement();
        String sqlI2 = "DELETE FROM author " +
                "WHERE authorid = " + authorID + ";";
        int resI2 = doUpdate(sqlI2,delete);
        if(resI2 == 0) {
            rollback();
            return ret + "\tThis customer does not exist";
        }


        //-- COMMIT --//
        Statement commit = createStatement();
        String commitSql = "COMMIT;";
        doExecute(commitSql, commit);


        // Print the details
        ret += "\tAuthor " + authorID + " has been deleted";

        return ret;
    }

    public String deleteBook(int isbn) {
        return "Delete Book";
    }

    /**
     * Get a Statement from the connection
     *
     * @return
     */
    private Statement createStatement() {
        Statement s = null;
        try{
            s = con.createStatement();
        } catch (SQLException sqlex){
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }
        return s;
    }

    private ResultSet doQuery(String sql, Statement s){
        ResultSet rs = null;
        try{
            rs = s.executeQuery(sql);
        } catch (SQLException sqlex){
            System.out.println(sqlex);
            System.out.println("An exception"+
                    "while executing a query, probably"+
                    "means my SQL is invalid");
        }
        return rs;
    }

    private boolean doExecute(String sql, Statement s){
        boolean res = false;
        try{
            res = s.execute(sql);
        } catch (SQLException sqlex){
            System.out.println(sqlex);
            System.out.println("An exception"+
                    "while executing, probably"+
                    "means my SQL is invalid");
        }
        return res;
    }

    private int doUpdate(String sql, Statement s){
        int res = 0;
        try{
            res = s.executeUpdate(sql);
        } catch (SQLException sqlex){
            System.out.println(sqlex);
            System.out.println("An exception"+
                    "while executing an update, probably"+
                    "means my SQL is invalid");
        }
        return res;
    }

    private String removeSpaces(String s){
        if(s == null){return s;}
        String ret = s;
        char c = ret.charAt(ret.length()-1);
        while(c == ' '){
            ret = ret.substring(0, ret.length()-1);
            c = ret.charAt(ret.length()-1);
        }
        return ret;
    }

    private void rollback() {
        Statement rb = createStatement();
        String sql = "ROLLBACK;";
        doExecute(sql, rb);
    }


}