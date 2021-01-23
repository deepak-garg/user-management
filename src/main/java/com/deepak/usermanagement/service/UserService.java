package com.deepak.usermanagement.service;

import com.deepak.usermanagement.exception.domain.EmailExistException;
import com.deepak.usermanagement.exception.domain.EmailNotFoundException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.deepak.usermanagement.enumeration.Role.ROLE_USER;
import static com.deepak.usermanagement.utils.DateTime.utcTime;
import static com.deepak.usermanagement.utils.FileConstant.DEFAULT_PROFILE_IMAGE_PATH;
import static com.deepak.usermanagement.utils.FileConstant.USER_FOLDER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserService implements UserDetailsService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private static final String USERNAME_ALREADY_EXIST = "Username already exist";
    private static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final LoginAttemptService loginAttemptService;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.loginAttemptService = loginAttemptService;
    }
    //call at the time of login by spring security
    @Override
    public UserDetails loadUserByUsername(final String userName) throws UsernameNotFoundException {
        User user = userRepository.findUserByUserName(userName);
        if(Objects.isNull(user)) {
            LOG.error("User not found by username: {}", userName);
            throw new UsernameNotFoundException("User not found by username: "+ userName);
        }else {
            validateLoginAttempt(user);
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
        user.setPassword(encodePassword(user.getPassword()));
        user.setJoinDate(utcTime());
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImage(user.getUserName()));
        userRepository.save(user);
        return user;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User findUserByUsername(String username) {
         return userRepository.findUserByUserName(username);
    }

    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive) {
        return null;
    }

    public User updateUser(String currentUserName, String firstName, String lastName, String userName, String email, String role, boolean isNonLocked, boolean isActive) {
        return null;
    }

    public void deleteUser(long id){

    }

    public void resetPassword(String email) throws EmailNotFoundException {
        User user = findUserByEmail(email);
        if(user == null) {
            throw new EmailNotFoundException(email);
        }
    }

    public User updateProfileImage(String username, MultipartFile profileImage) {
        return null;
    }


     private void validateNewUsernameAndEmail(String newUsername, String email) throws UsernameExistException, EmailExistException {
        User userByUsername = findUserByUsername(newUsername);
        if(userByUsername!=null) {
            throw new UsernameExistException(USERNAME_ALREADY_EXIST);
        }
        User userByEmail = findUserByEmail(email);
        if(userByEmail!=null) {
            throw new EmailExistException(EMAIL_ALREADY_EXISTS);
        }
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    private String getTemporaryProfileImage(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_PROFILE_IMAGE_PATH + username).toUriString();
    }

    private void validateLoginAttempt(User user) {
        if(loginAttemptService.hasExceedMaxAttempts(user.getUserName())) {
            user.setNotLocked(false);
        }
    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
        if(profileImage!=null) {
            Path userfolder = Paths.get(USER_FOLDER + user.getUserName()).toAbsolutePath().normalize();
            if(!Files.exists(userfolder)) {
                Files.createDirectories(userfolder);
            }
            Files.deleteIfExists(Paths.get(userfolder + user.getUserName() + "." + ".jpg"));
            Files.copy(profileImage.getInputStream(), userfolder.resolve(user.getUserName() + "." + ".jpg"), REPLACE_EXISTING);
        }

    }
}
