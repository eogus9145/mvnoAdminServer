package com.telesoft.mvnogen.common.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommonDAO {
	
	private final SqlSession sqlSession;
	
	public <V> Map<String, Object> selectOne(Map<String, V> param) throws Exception {
		String queryId = String.valueOf(param.get("queryId"));
		return sqlSession.selectOne(queryId, param);
	}
	
	public <V> List<Map<String, Object>> selectList(Map<String, V> param) throws Exception {
		String queryId = (String) param.get("queryId");
		return sqlSession.selectList(queryId, param);		
	}
	
	public <V> int selectListTotCnt(Map<String, V> param) throws Exception {
		String queryId = (String) param.get("queryId");
		return (Integer) sqlSession.selectOne(queryId + "_tot_cnt", param);
	}
	
	public <V> Map<String, Object> selectListTotSum(Map<String, V> param) {
		String queryId = String.valueOf(param.get("queryId"));
		return sqlSession.selectOne(queryId + "_tot_sum", param);
	}
	
	public <V> int insert(Map<String, V> param) throws Exception {
		String queryId = String.valueOf(param.get("queryId"));
		return sqlSession.insert(queryId, param);
	}
	
	public <V> int update(Map<String, V> param) throws Exception {
		String queryId = String.valueOf(param.get("queryId"));
		return sqlSession.update(queryId, param);
	}
	
	public <V> int delete(Map<String, V> param) throws Exception {
		String queryId = String.valueOf(param.get("queryId"));
		return sqlSession.delete(queryId, param);
	}
	
	
}
