package top.naccl.gobang.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.naccl.gobang.exception.UsernameNotFoundException;
import top.naccl.gobang.model.entity.Result;

import javax.servlet.http.HttpServletRequest;

/**
 * RestControllerAdvice 捕获异常后，返回json数据类型
 *
 * @Description: 对Controller层全局异常处理
 * @Author: Naccl
 * @Date: 2020-11-09
 */
@RestControllerAdvice
public class ControllerExceptionHandler {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 捕获自定义的登录失败异常
	 *
	 * @param request 请求
	 * @param e       自定义抛出的异常信息
	 * @return
	 */
	@ExceptionHandler(UsernameNotFoundException.class)
	public Result usernameNotFoundExceptionHandler(HttpServletRequest request, UsernameNotFoundException e) {
		logger.error("Request URL : {}, Exception : {}", request.getRequestURL(), e);
		return Result.create(401, "用户名或密码错误！");
	}

	/**
	 * 捕获其它异常
	 *
	 * @param request 请求
	 * @param e       异常信息
	 * @return
	 */
	@ExceptionHandler(Exception.class)
	public Result exceptionHandler(HttpServletRequest request, Exception e) {
		logger.error("Request URL : {}, Exception : {}", request.getRequestURL(), e);
		return Result.create(500, "异常错误");
	}
}
