package vn.codegyme.meal_choice.service;


import vn.codegyme.meal_choice.dto.request.RegisterRequest;

public interface UserService {

    void register(RegisterRequest request);
}
