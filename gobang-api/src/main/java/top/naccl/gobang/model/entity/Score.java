package top.naccl.gobang.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @program: Gobang
 * @author: ChenJin
 * @create: 2021-10-28 15:41
 * @description: TODO
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Score {
    private Long id;
    private int score;
    private Long userId;
}
