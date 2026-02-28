package uniearn.interfaces;

import uniearn.interfaces.users.IUser;

import java.util.List;

public interface IFreelancer<F,U> extends IUser<U> {
    void addFreelancer(F freelancer);
    void updateFreelancer(int id, F freelancer);
    void deleteFreelancer(int id);
    F getFreelancerById(int id);
    List<F> getAllFreelancers();
}
