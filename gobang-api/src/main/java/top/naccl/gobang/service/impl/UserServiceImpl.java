package top.naccl.gobang.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.naccl.gobang.exception.UsernameNotFoundException;
import top.naccl.gobang.mapper.UserMapper;
import top.naccl.gobang.model.dto.LoginInfo;
import top.naccl.gobang.model.entity.User;
import top.naccl.gobang.service.UserService;
import top.naccl.gobang.util.JwtUtils;
import top.naccl.gobang.util.Md5Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 用户业务层接口实现类
 * @Author: Naccl
 * @Date: 2020-11-09
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserMapper userMapper;

	@Override
	public Map<String, Object> login(LoginInfo loginInfo) {
		User user = findUserByUsernameAndPassword(loginInfo.getUsername(), loginInfo.getPassword());
		user.setPassword(null);
		String jwt = JwtUtils.generateToken(user.getUsername());
		Map<String, Object> map = new HashMap<>();
		map.put("user", user);
		map.put("token", jwt);
		return map;
	}

	private User findUserByUsernameAndPassword(String username, String password) {
		User user = userMapper.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("用户不存在");
		}
		if (!user.getPassword().equals(Md5Utils.getMd5(password))) {
			throw new UsernameNotFoundException("密码错误");
		}
		return user;
	}
}
