package top.naccl.gobang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.gobang.model.entity.Result;
import top.naccl.gobang.model.entity.User;
import top.naccl.gobang.model.dto.LoginInfo;
import top.naccl.gobang.service.UserService;
import top.naccl.gobang.util.JwtUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 登录
 * @Author: Naccl
 * @Date: 2020-11-09
 */
@RestController
public class LoginController {
	@Autowired
	UserService userService;

	/**
	 * 登录成功，签发Token
	 *
	 * @param loginInfo
	 * @return
	 */
	@PostMapping("/login")
	public Result login(@RequestBody LoginInfo loginInfo) {
		User user = userService.findUserByUsernameAndPassword(loginInfo.getUsername(), loginInfo.getPassword());
		user.setPassword(null);
		String jwt = JwtUtils.generateToken(user.getUsername());
		Map<String, Object> map = new HashMap<>();
		map.put("user", user);
		map.put("token", jwt);
		return Result.ok("登录成功", map);
	}
}