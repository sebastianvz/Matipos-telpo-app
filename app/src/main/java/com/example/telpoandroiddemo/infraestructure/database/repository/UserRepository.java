package com.example.telpoandroiddemo.infraestructure.database.repository;

import android.content.Context;

import com.example.telpoandroiddemo.application.dao.UserDao;
import com.example.telpoandroiddemo.domain.entities.User;
import com.example.telpoandroiddemo.infraestructure.database.persistence.AppDatabase;

import java.time.LocalDateTime;

public class UserRepository {

    public User readUserByUsername(Context context, String username) {
        return AppDatabase.getInstance(context).userDao().select(username);
    }

    public User createUser (Context context, User user) {
        user.createdAt = LocalDateTime.now().toString();
        UserDao dao = AppDatabase.getInstance(context).userDao();
        dao.insert(user);
        return dao.select(user.username);
    }

}
