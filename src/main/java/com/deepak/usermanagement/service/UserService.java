package com.deepak.usermanagement.service;

import com.deepak.usermanagement.exception.domain.EmailExistException;
import com.deepak.usermanagement.exception.domain.UsernameExistException;
import com.deepak.usermanagement.model.User;
import com.deepak.usermanagement.model.UserPrincipal;
import com.deepak.usermanagement.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

import static com.deepak.usermanagement.enumeration.Role.ROLE_USER;
import static com.deepak.usermanagement.utils.DateTime.utcTime;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserService implements UserDetailsService {
    private Logger LOG = LoggerFactory.getLogger(UserService.class);
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    @Override
    public UserDetails loadUserByUsername(final String userName) throws UsernameNotFoundException {
        User user = userRepository.findUserByUserName(userName);
        if(Objects.isNull(user)) {
            LOG.error("User not found by username: {}", userName);
            throw new UsernameNotFoundException("User not found by username: "+ userName);
        }else {
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(utcTime());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOG.info("Returning found user by username: {}", userName);
            return userPrincipal;
        }
    }

    public User register(final User user) throws UsernameExistException, EmailExistException {
        validateNewUsernameAndEmail(user.getUserName(), user.getEmail());
        user.setUserId(generateUserId());
        user.setPassword(encodePassword(generatePassword()));
        user.setJoinDate(utcTime());
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImage());
        userRepository.save(user);
        return null;
    }

    public List<User> getUsers() {
        return null;
    }

    public User findUserByUsername(String username) {
         return userRepository.findUserByUserName(username);
    }

    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    private void validateNewUsernameAndEmail(String newUsername, String email) throws UsernameExistException, EmailExistException {
        User userByUsername = findUserByUsername(newUsername);
        if(userByUsername!=null) {
            throw new UsernameExistException("Username already exist");
        }
        User userByEmail = findUserByEmail(email);
        if(userByEmail!=null) {
            throw new EmailExistException("Email already exists");
        }
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    private String getTemporaryProfileImage() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/image/profile/temp").toUriString();
    }
}
