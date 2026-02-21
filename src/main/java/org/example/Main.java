package org.example;

import uniearn.model.entities.users.User;
import uniearn.model.enums.UserRole;
import uniearn.services.users.UserService;

import java.sql.SQLException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws SQLException {
        //MyConnection mc = new MyConnection();
        User u1 = new User("John Doe","joe@email.com","123", UserRole.FREELANCER, null);
        User u2 = new User("John Doe2","joe2@email.com","123", UserRole.CLIENT, null);

        UserService us = new UserService();
//        us.addUser(u2);

        System.out.println(us.getAllUsers());

//        User u = us.getUserById(3);
//        System.out.println(u);

        //us.deleteUser(4);

        User updatedUser = new User("John Updated", "updated@email.com", "456", UserRole.CLIENT, null);
//        us.updateUser(3, updatedUser);

    }
}