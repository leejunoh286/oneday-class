package kr.or.oti.onedayclass;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import kr.or.oti.onedayclass.mapper.TestMapper;

@SpringBootTest
class TestMapperTest {

	@Autowired
	private TestMapper testMapper;
	
	@Autowired
	private DataSource dataSource;
	
	@Test
	void testDatabaseConnection() {
		String version = testMapper.getDatabaseVersion();
		
		System.out.println("MySQL Version = " + version);
	}
	
	@Test
	void testConnection() throws Exception {
		Connection conn = dataSource.getConnection();
		
		System.out.println("Connection isClosed = " + conn.isClosed());
		
		conn.close();
	}
}
