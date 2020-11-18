package top.naccl.gobang.manager;

import lombok.extern.slf4j.Slf4j;
import top.naccl.gobang.model.entity.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 游戏对局管理器
 * @Author: Naccl
 * @Date: 2020-11-18
 */
@Slf4j
public class GameManager {
	private static ConcurrentHashMap<String, Game> manager = new ConcurrentHashMap<>();

	public static void add(String owner, Game game) {
		log.info("新建对局 owner = {}", owner);
		manager.put(owner, game);
	}

	public static void remove(String owner) {
		log.info("关闭对局 owner = {}", owner);
		manager.remove(owner);
	}

	public static Game get(String owner) {
		log.info("查询对局 owner = {}", owner);
		return manager.get(owner);
	}

	public static int count() {
		int count = manager.size();
		log.info("查询对局数量 count = {}", count);
		return count;
	}

	public static List<Game> getGameList() {
		List<Game> gameList = new ArrayList<>();
		for (String owner : manager.keySet()) {
			gameList.add(manager.get(owner));
		}
		return gameList;
	}

	public static Game getGameByUsername(String username) {
		if (manager.containsKey(username)) {
			return manager.get(username);
		}
		for (String owner : manager.keySet()) {
			Game game = manager.get(owner);
			if (username.equals(game.getPlayer())) {
				return game;
			}
		}
		return null;
	}
}
