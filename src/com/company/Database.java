package com.company;

import java.sql.*;
import java.util.ArrayList;

public class Database {
    public static Connection conn;
    public static Statement statement;
    public static ResultSet resSet;

    public static void Connect(String dbName) throws ClassNotFoundException, SQLException {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
    }

    public static void Execute(ArrayList<String> queries) throws SQLException {
        statement = conn.createStatement();

        for (String query : queries)
            statement.execute(query);

        statement.close();
    }

    public static ArrayList<String[]> SelectFromTable(String table, String fieldName) throws SQLException {
        statement = conn.createStatement();

        resSet = statement.executeQuery(String.format("SELECT * FROM %s;", table));
        ArrayList<String[]> list = new ArrayList<>();

        while (resSet.next()) {
            String country = resSet.getString("country");
            String field = resSet.getString(fieldName);
            list.add(new String[] {country, field});
        }

        statement.close();
        return list;
    }

    public static ArrayList<String> CustomSelectFirst(String query, String[] cols) throws SQLException {
        statement = conn.createStatement();
        resSet = statement.executeQuery(query);
        ArrayList<String> list = new ArrayList<>();
        resSet.next();

        String country = resSet.getString("country");
        list.add(country);
        for (String col: cols)
            list.add(resSet.getString(col));

        statement.close();
        return list;
    }

    public static String GetInsertQuery(String table, String[] cols, String[] values) {
        return String.format("INSERT INTO '%s' ('%s', '%s') VALUES ('%s', '%s');", table, cols[0], cols[1], values[0], values[1]);
    }
}
