package top.naccl.gobang.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description: 棋子信息
 * @Author: Naccl
 * @Date: 2020-11-12
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Chess {
	private int x;
	private int y;
	private boolean isBlack;
}
