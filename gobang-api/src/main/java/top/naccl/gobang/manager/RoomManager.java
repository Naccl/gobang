package top.naccl.gobang.manager;

import lombok.extern.slf4j.Slf4j;
import top.naccl.gobang.model.entity.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 游戏房间管理器
 * @Author: Naccl
 * @Date: 2020-11-10
 */
@Slf4j
public class RoomManager {
	private static ConcurrentHashMap<String, Room> manager = new ConcurrentHashMap<>();

	public static void add(String owner, Room room) {
		log.info("新建房间 owner = {}", owner);
		manager.put(owner, room);
	}

	public static void remove(String owner) {
		log.info("关闭房间 owner = {}", owner);
		manager.remove(owner);
	}

	public static Room get(String owner) {
		log.info("查询房间 owner = {}", owner);
		return manager.get(owner);
	}

	public static int count() {
		int count = manager.size();
		log.info("查询房间数量 count = {}", count);
		return count;
	}

	public static List<Room> getRoomList() {
		List<Room> roomList = new ArrayList<>();
		for (String owner : manager.keySet()) {
			roomList.add(manager.get(owner));
		}
		return roomList;
	}

	public static boolean isExistUser(String username) {
		if (manager.containsKey(username)) {
			return true;
		}
		for (String owner : manager.keySet()) {
			if (username.equals(manager.get(owner).getPlayer())) {
				return true;
			}
		}
		return false;
	}
}
