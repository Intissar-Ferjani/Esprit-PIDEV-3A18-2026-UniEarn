package uniearn.interfaces.users.freelancer;

import uniearn.interfaces.users.IUser;

import java.sql.SQLException;
import java.util.List;

public interface IFreelancer<F,U> extends IUser<U> {
    void addFreelancer(F freelancer) throws SQLException;
    void updateFreelancer(int id, F freelancer);
    void deleteFreelancer(int id);
    F getFreelancerById(int id);
    List<F> getAllFreelancers();
}
