module uniearn_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens uniearn.controller to javafx.fxml;
    opens uniearn.model.entities to javafx.fxml;
    opens uniearn.tests to javafx.fxml;

    exports uniearn.controller;
    exports uniearn.model.enums;
    exports uniearn.model.entities;
    exports uniearn.services;
    exports uniearn.interfaces;
    exports uniearn.database;
    exports uniearn.tests;
}
