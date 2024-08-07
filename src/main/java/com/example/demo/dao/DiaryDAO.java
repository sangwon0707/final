package com.example.demo.dao;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Diary;

@Repository
public interface DiaryDAO extends JpaRepository<Diary, Integer> {
	
	
	
	//----------다이어리----------
	 
	//목록 출력
	@Query(value="select * from diary where uno=?1 order by d_date desc", nativeQuery=true)
	public List<Diary> findByUsersId(Long id);
	
	
	// 상세내용 출력
	public List<Diary> findByUsersIdAndDno(Long id, int dno);

	// 게시글 번호 추가 
    @Query(value="select ifnull(max(dno),0) + 1 from diary", nativeQuery = true)
	public int getNextDno();
	
	
	// 특정 연도,월에 해당하는 일기 출력(uno=101 임시)
    @Query(value = "select * from diary where uno = ?3 and year(d_date) = ?1 and month(d_date) = ?2 order by d_date desc", nativeQuery = true)
	public List<Diary> findByYearAndMonthAndId(int year, int month, Long id);
    
    
}