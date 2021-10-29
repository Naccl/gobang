package top.naccl.gobang.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

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
	private String nickname;
	private String avatar;
	private String email;
	private Date createTime;
	@TableField(exist = false)
	private String token;
}
