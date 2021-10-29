package top.naccl.gobang.service;

public interface GameLobbyService {
    void enterRoom(String owner, String username);

    void joinMatching(String username);

    void createRoom(String username);
}
