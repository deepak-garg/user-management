package com.deepak.usermanagement.repository;

import com.deepak.usermanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUserName(String username);
    User findUserByEmail(String email);

}
