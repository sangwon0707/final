package com.example.demo.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dao.PuppyDAO;
import com.example.demo.dao.ScheduleDAO;
import com.example.demo.dao.UsersDAO;
import com.example.demo.entity.Puppy;
import com.example.demo.entity.Schedule;
import com.example.demo.entity.Users;


@Service
public class ScheduleService {
    
    @Autowired
    private ScheduleDAO dao;
    @Autowired
    private UsersDAO uDAO;
    @Autowired
    private PuppyDAO pDAO;
    
    //----------스케줄러----------
    public List<Puppy> getPuppyByUsersId(Long uno) {
        return dao.findPuppyById(uno);
    }
	
	// 특정 날짜 스케줄 가져오기
	public List<Schedule> getSchedulesByDate(Long uno, Date date) {
		  return dao.findSchedulesByDate(uno, date);
	}
	
	// 선택한 월,일 범위 내의 스케줄 출력
    public List<Schedule> getMonthlySchedules(Long uno, int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());
        return dao.findSchedulesByMonth(uno, Date.valueOf(startOfMonth), Date.valueOf(endOfMonth));
    }

    // schedule 번호인 sno 적용
    public int getNextSno() {
        return dao.getNextSno();
    }

    // 스케줄러 컬럼 추가(강아지 선택하여 입력)
    public void saveSchedule(Schedule schedule, Long uno, int pno) {
        Users user = uDAO.findById(uno).orElse(null);
        Puppy puppy = pDAO.findById(pno).orElse(null);

        if (user != null && puppy != null) {
            schedule.setUsers(user);
            schedule.setPuppy(puppy);
            dao.save(schedule);
        }
    }

    // 스케줄러 s_content만 수정
    public void updateSchedule(int sno, String s_content) {
        Schedule schedule = dao.findById(sno).orElse(null);
        if (schedule != null) {
            schedule.setS_content(s_content);
            dao.save(schedule);
        }
    }

    // 스케줄러 s_content 선택하여 해당 컬럼 삭제
    public void deleteSchedule(int sno) {
        dao.deleteById(sno);
    }
    
    // 완료한 스케줄러 체크
    public void checkSchedule(int sno, String s_complete) {
        Schedule schedule = dao.findById(sno).orElse(null);
        if (schedule != null) {
            schedule.setS_complete(s_complete);
            dao.save(schedule);
        }
    }
    
    // 반려견 삭제시 스케줄러의 내용도 함께 삭제되게 하기 위한 메소드
    public void deleteScheduleByPno(int pno) {
    	dao.deleteSchedule(pno);
    }
    
}