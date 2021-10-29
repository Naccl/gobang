package top.naccl.gobang.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Auther: wAnG
 * @Date: 2021/10/29 16:08
 * @Description:
 */

@NoArgsConstructor
@Getter
@Setter
@ToString
public class goBang extends Chess{

    /**
     * 是否为黑棋
     */
    private boolean isBlack;


}
