package com.hipradeep.userservice.service;

import com.hipradeep.userservice.entity.User;
import com.hipradeep.userservice.entity.User2;
import com.hipradeep.userservice.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService2 {

    @Autowired
    private UserDao userDao;

    public User2 createUser(User2 user) {
        if (user.getId() == null) {
            user.setId(System.currentTimeMillis());
        }
        return userDao.save(user);
    }

    public List<User2> getAllUsers() {
        return userDao.findAll();
    }

    public User2 getUserById(Long id) {
        return userDao.findById(id);
    }

    public String deleteUser(Long id) {
        return userDao.delete(id);
    }
}
