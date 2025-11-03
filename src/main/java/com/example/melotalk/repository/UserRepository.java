package com.example.melotalk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.melotalk.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // email で検索したいとき用
    public User findByEmail(String email);

}
