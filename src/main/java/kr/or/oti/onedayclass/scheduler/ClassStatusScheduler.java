package kr.or.oti.onedayclass.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.onedayclass.mapper.ClassMapper;

@Component
public class ClassStatusScheduler {
	
	private final ClassMapper classMapper;
	
	public ClassStatusScheduler(ClassMapper classMapper) {
		this.classMapper = classMapper;
	}
	
	@Scheduled(fixedRate = 60000)
	@Transactional
	public void updateClassStatus() {
		
		// 진행일이 지난 클래스 -> COMPLETED
		classMapper.completeExpiredClasses();
		
		// 모집 마감일이 지난 클래스 -> CLOSED
		classMapper.closeExpiredRecruitingClasses();
	}
}
