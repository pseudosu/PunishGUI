package com.backyardmc.punishgui.network;

import com.backyardmc.punishgui.BYPunishment;

import java.sql.*;

public class Network {
    private static String DATABASE;
    private static String USERNAME;
    private static String PASSWORD;

    private String url;
    private Connection c;

    public Network() {
        DATABASE = BYPunishment.getConfigManager().getMYSQL_DATABASE();
        USERNAME = BYPunishment.getConfigManager().getMYSQL_USERNAME();
        PASSWORD = BYPunishment.getConfigManager().getMYSQL_PASSWORD();
        url = "jdbc:mysql://" + BYPunishment.getConfigManager().getMYSQL_HOST() + ":3306/";
    }

    public void init() throws SQLException {
        activateConnection();
        Statement s = c.createStatement();
        String userTableQuery =
                "CREATE TABLE IF NOT EXISTS byp_userdata" +
                        "(id int not null auto_increment," +
                        " punishment_id int(11)," +
                        " uuid varchar(255)," +
                        " is_active boolean," +
                        " warning_level int(11)," +
                        " end_date varchar(255)," +
                        "" +
                        " primary key (id))";
        String staffDataTableQuery =
                "CREATE TABLE IF NOT EXISTS byp_staffdata" +
                        "(id int not null auto_increment," +
                        "punishment_id int(11)," +
                        "staff_uuid varchar(255)," +
                        "punished_uuid varchar(255)," +
                        "date bigint(19)," +
                        "serverName varchar(255)," +
                        "primary key (id))";
        s.executeUpdate(userTableQuery);
        s.executeUpdate(staffDataTableQuery);
        closeConnection();
    }


    /**
     * @return mysql connection instance
     */
    public Connection getConnection() {
        return c;
    }

    public void activateConnection() {
        try {
            c = DriverManager.getConnection(url + DATABASE + "?useSSL=false&characterEncoding=latin1&useConfigs=maxPerformance&allowPublicKeyRetrieval=true", USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param query query to execute
     * @return returns the resultset
     */
    public ResultSet executeQuery(String query) {
        try {
            PreparedStatement ps = c.prepareStatement(query);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param query query to execute
     */
    public void executeUpdate(String query) {
        try {
            PreparedStatement ps = c.prepareStatement(query);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(query);
        }
    }

}
