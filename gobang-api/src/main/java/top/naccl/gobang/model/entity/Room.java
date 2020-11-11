package top.naccl.gobang.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description: 游戏房间
 * @Author: Naccl
 * @Date: 2020-11-10
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Room {
	private String owner;
	private String player;
	private Boolean isPlaying;
}
