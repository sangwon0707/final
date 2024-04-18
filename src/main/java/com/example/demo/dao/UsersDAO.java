package com.example.demo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Users;


import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersDAO extends JpaRepository<Users, Long> {
	public Users findByEmail(String email);
//    Optional<Users> findByEmail(String email);

    @Modifying
	@Query(value = "update users set u_name=?, u_email=?, u_phone=?, u_nickname=?, u_fname=?, rno=? where uno=?", nativeQuery = true)
	@Transactional
	public int updateInfo(String name, String email, String phone, String nickname, String filename, String rno, Long uno);

	@Modifying
	@Query(value = "update users set u_pwd=? where uno=?", nativeQuery = true)
	@Transactional
	public void updatePwd(String newPwd, Long uno);
	
	// 세션에 유지한 Name 으로 Uno 가져오기
	@Query(value="select uno from users where u_name=?1", nativeQuery = true)
	public Long findByUName(String u_name);
}