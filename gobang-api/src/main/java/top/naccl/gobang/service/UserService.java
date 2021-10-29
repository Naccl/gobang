package top.naccl.gobang.service;

import top.naccl.gobang.model.dto.LoginInfo;

import java.util.Map;

public interface UserService {
	Map<String,Object> login(LoginInfo loginInfo);
}
