package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static server.Server.connection;

public class AuthServiceDB implements AuthService {


    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }


    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Users WHERE login=? and password=?");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2,password);
            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()) {
                return rs.getString("nick");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Users WHERE login=? or nick=?");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, nickname);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                System.out.println(rs.getString(1));
                System.out.println(rs.getString(2));
                System.out.println("----");

            }
            if(rs.next()) {
                return false;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Users (login, password, nick) VALUES (?,?,?)");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2,password);
            preparedStatement.setString(3,nickname);
            preparedStatement.executeUpdate();
            } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }
}
