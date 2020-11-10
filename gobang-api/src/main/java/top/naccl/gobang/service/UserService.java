package top.naccl.gobang.service;

import top.naccl.gobang.model.entity.User;

public interface UserService {
	User findUserByUsernameAndPassword(String username, String password);
}
