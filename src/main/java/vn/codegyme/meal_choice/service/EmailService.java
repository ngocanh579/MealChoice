package vn.codegyme.meal_choice.service;

import vn.codegyme.meal_choice.entity.User;

public interface EmailService {

    void sendVerificationEmail(User user, String verificationLink);
}
