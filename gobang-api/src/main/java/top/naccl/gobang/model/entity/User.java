package top.naccl.gobang.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description: 用户实体
 * @Author: Naccl
 * @Date: 2020-11-08
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User {
	private Long id;
	private String username;
	private String password;
}
