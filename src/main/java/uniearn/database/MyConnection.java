package uniearn.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private String url = System.getProperty("uniearn.db.url", "jdbc:mysql://localhost:3306/uniearn2");

    private String login = System.getProperty("uniearn.db.user", "root");

    private String pwd = System.getProperty("uniearn.db.password", "");

    private Connection cnx;

    public static MyConnection instance;



    public MyConnection(){

        try{
         cnx = DriverManager.getConnection(url, login, pwd);
             System.out.println("Connected to database successfully!");
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public Connection getCnx() {
        return cnx;
    }

    public static MyConnection getInstance(){
        if(instance == null){
            instance = new MyConnection();
        }
        return instance;
    }
}
