package top.naccl.gobang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.naccl.gobang.model.entity.User;

public interface UserService {
	User findUserByUsernameAndPassword(String username, String password);
}
