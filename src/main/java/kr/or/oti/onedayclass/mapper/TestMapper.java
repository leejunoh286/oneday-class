package kr.or.oti.onedayclass.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TestMapper {
	
	@Select("SELECT VERSION()")
	String getDatabaseVersion();
}
