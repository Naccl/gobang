package top.naccl.gobang.exception;

/**
 * @Description: 用户不存在异常
 * @Author: Naccl
 * @Date: 2020-11-09
 */
public class UsernameNotFoundException extends RuntimeException{
	public UsernameNotFoundException() {
	}

	public UsernameNotFoundException(String message) {
		super(message);
	}

	public UsernameNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
