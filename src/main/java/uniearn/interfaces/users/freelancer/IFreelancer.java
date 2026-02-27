package uniearn.interfaces.users.freelancer;

import uniearn.interfaces.users.IUser;
import uniearn.model.enums.VerifStatus;

import java.sql.SQLException;
import java.util.List;

public interface IFreelancer<F,U> extends IUser<U> {
    void addFreelancer(F freelancer) throws SQLException;
    void updateFreelancer(int id, F freelancer);
    void deleteFreelancer(int id);
    F getFreelancerById(int id);
    List<F> getAllFreelancers();
    void updateVerificationData(int id, String path, VerifStatus stat) throws SQLException;


    }
