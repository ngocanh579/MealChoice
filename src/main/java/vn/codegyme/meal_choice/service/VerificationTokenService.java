package vn.codegyme.meal_choice.service;

import vn.codegyme.meal_choice.entity.User ;
import vn.codegyme.meal_choice.enums.VerificationResult;

public interface VerificationTokenService {

    String createVerificationToken(User user);

    VerificationResult verifyToken(String token);
}
