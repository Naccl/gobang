package top.naccl.gobang.model.entity;

import lombok.*;

/**
 * @Auther: wAnG
 * @Date: 2021/10/29 16:04
 * @Description: 回合的基本信息
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class roundInfo {

    /**
     * 房主信息
     */
    private String owner;

    /**
     * 当前回合操作玩家
     */
    private String userName;

}
