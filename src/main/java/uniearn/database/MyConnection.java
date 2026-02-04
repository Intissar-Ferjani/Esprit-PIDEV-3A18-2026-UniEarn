package uniearn.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private String url="jdbc:mysql://localhost:3306/uniearn_db";

    private String login="root";

    private String pwd="";

    private Connection cnx;

    public MyConnection(){
        try{
        DriverManager.getConnection(url, login, pwd);
             System.out.println("Connected to database successfully!");
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

}
