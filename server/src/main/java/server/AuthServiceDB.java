package server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static server.Server.stmt;

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


    public AuthServiceDB() {
        try {
            stmt.executeUpdate("INSERT INTO Users (login, password, nick) VALUES ('qwe', 'qwe', 'qwe');");
            stmt.executeUpdate("INSERT INTO Users (login, password, nick) VALUES ('asd', 'asd', 'asd');");
            stmt.executeUpdate("INSERT INTO Users (login, password, nick) VALUES ('zxc', 'zxc', 'zxc');");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users WHERE login='"+ login +"' and password='"+ password +"';");
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
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users WHERE login='"+ login +"' or nick='"+ nickname +"';");
            if(rs.next()) {
                return false;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            stmt.executeUpdate("INSERT INTO Users (login, password, nick) VALUES ('"+ login +"', '"+password+"', '"+nickname+"');");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }
}
