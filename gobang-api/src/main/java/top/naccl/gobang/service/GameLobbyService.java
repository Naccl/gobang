package top.naccl.gobang.service;

import java.util.Map;

public interface GameLobbyService {
    void enterRoom(String owner, String username);

    void joinMatching(String username);

    void createRoom(String username);

    void exitRoom(String username);

    Map<String,Object> getRoomList();

    void notifyAllOnlineCount();
}
