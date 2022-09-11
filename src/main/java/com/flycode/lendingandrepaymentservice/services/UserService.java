package com.flycode.lendingandrepaymentservice.services;

import com.flycode.lendingandrepaymentservice.models.Role;
import com.flycode.lendingandrepaymentservice.models.User;
import com.flycode.lendingandrepaymentservice.repositories.RoleRepository;
import com.flycode.lendingandrepaymentservice.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Service
@Transactional
@Slf4j
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Fetch user with username and create a UserDetail object with the information. Used by spring security to load
     * the logged-in user into the SecurityContext.
     *
     * @param username String user's username
     * @return UserDetails object
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(user == null) {
            throw new UsernameNotFoundException("User not found");
        } else {
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            user.getRoles().forEach(role -> {
                authorities.add(new SimpleGrantedAuthority(role.getName()));
            });
            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
        }
    }

    /**
     * Encode password of user and persist user model to db.
     *
     * @param user User to persist
     * @return User to persist
     */
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Persist role model to db.
     *
     * @param role Role to persist
     * @return Persisted Role
     */
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    /**
     * Assign Role to user.
     *
     * @param username username of User.
     * @param roleName role name of Role.
     */
    public void addRoleToUser(String username, String roleName) {
        User user = userRepository.findByUsername(username);
        Role role = roleRepository.findByName(roleName);
        user.getRoles().add(role);
    }

    /**
     * Fetch user by username
     *
     * @param username username of User.
     * @return User
     */
    public User getUser(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Fetch all users in db.
     *
     * @return List of User
     */
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
