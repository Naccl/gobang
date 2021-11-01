package top.naccl.gobang.model.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @program: gobang
 * @author: ChenJin
 * @create: 2021-11-01 09:01
 * @description: 匹配消息
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MatchingMQEventMessage {

    // 当前用的用户名进行匹配
    private String username;
}
