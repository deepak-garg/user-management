package com.deepak.usermanagement.service;

import com.deepak.usermanagement.model.User;
import com.deepak.usermanagement.model.UserPrincipal;
import com.deepak.usermanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserService implements UserDetailsService {
    private Logger LOG = LoggerFactory.getLogger(UserService.class);
    private UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepository.findUserByUserName(userName);
        if(Objects.isNull(user)) {
            LOG.error("User not found by username: {}", userName);
            throw new UsernameNotFoundException("User not found by username: "+ userName);
        }else {
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOG.info("Returning found user by username: {}", userName);
            return userPrincipal;
        }
    }
}
