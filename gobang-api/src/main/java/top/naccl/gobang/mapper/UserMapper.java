package top.naccl.gobang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.naccl.gobang.model.entity.User;

/**
 * @Description: 用户持久层接口
 * @Author: Naccl
 * @Date: 2020-11-09
 */
@Mapper
@Repository
public interface UserMapper {
	User findByUsername(String username);
}
