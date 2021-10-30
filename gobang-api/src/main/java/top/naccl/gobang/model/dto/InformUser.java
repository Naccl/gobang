package top.naccl.gobang.model.dto;

import lombok.*;

/**
 * @program: Gobang
 * @author: ChenJin
 * @create: 2021-10-29 08:50
 * @description: 通知用户
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class InformUser {
    // 用户名
    private String username;
    // 路径
    private String path;
    // 通知的信息
    private String message;
}
