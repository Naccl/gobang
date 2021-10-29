package top.naccl.gobang.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.naccl.gobang.mapper.UserMapper;
import top.naccl.gobang.model.entity.User;
import top.naccl.gobang.service.UserService;
import top.naccl.gobang.util.Md5Utils;

/**
 * @Description: 用户业务层接口实现类
 * @Author: Naccl
 * @Date: 2020-11-09
 */
@Service
public class UserServiceImpl implements UserService {
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private UserMapper userMapper;

	@Override
	public User findUserByUsernameAndPassword(String username, String password) {
		User user = userMapper.findByUsername(username);
		if (user == null) {
//			throw new UsernameNotFoundException("用户不存在");
			logger.error("用户不存在 : {}", username);
		}
		if (!user.getPassword().equals(Md5Utils.getMd5(password))) {
//			throw new UsernameNotFoundException("密码错误");
			logger.error("密码错误 : {}", password);
		}
		return user;
	}
}
