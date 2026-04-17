package com.guessgame.client.command;

import java.util.Map;
import java.util.HashMap;

public class CommandFactory {
    private static CommandFactory instance;

    Map<String, Class<? extends Command>> commandMap;
    Map<String, Class<? extends Response>> responseMap;

    private CommandFactory() {
        this.commandMap = new HashMap<>();
        commandMap.put("CONNECT", ServerCommand.Connect.class);
        commandMap.put("CREATE_ROOM", ServerCommand.CreateRoom.class);
        commandMap.put("LIST_ROOMS", ServerCommand.ListRoom.class);
        commandMap.put("JOIN_ROOM", ServerCommand.JoinRoom.class);
        commandMap.put("LEAVE_ROOM", ServerCommand.LeaveRoom.class);
        commandMap.put("KICK_PLAYER", ServerCommand.KickPlayer.class);
        commandMap.put("START_GAME", ServerCommand.StartGame.class);
        commandMap.put("PLAY_SERVER", ServerCommand.PlayServer.class);


        this.responseMap = new HashMap<>();
        responseMap.put("CONNECTED", ServerResponse.Connected.class);
        responseMap.put("ROOM_CREATED", ServerResponse.RoomCreated.class);
        responseMap.put("ROOM_LIST", ServerResponse.RoomList.class);
        responseMap.put("JOINED_ROOM", ServerResponse.RoomJoined.class);
        responseMap.put("LEFT_ROOM", ServerResponse.RoomLeft.class);
        responseMap.put("PLAYER_KICKED", ServerResponse.PlayerKicked.class);
        responseMap.put("GAME_STARTED", ServerResponse.GameStarted.class);
        responseMap.put("SERVER_GAME_STARTED", ServerResponse.ServerGameStarted.class);
    }

    public static synchronized CommandFactory getInstance() {
        if (instance == null) {
            instance = new CommandFactory();
        }
        return instance;
    }

    public Command createCommand(String commandName, String[] args) {
        Class<? extends Command> commandClass = commandMap.get(commandName);
        if (commandClass == null) {
            throw new IllegalArgumentException("Unknown command: " + commandName);
        }
        Command command;
        try {
            command = commandClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create command: " + commandName, e);
        }
        command.setArgs(args);
        return command;
   }

   public Response createResponse(String responseName, String[] args) {
        Class<? extends Response> responseClass = responseMap.get(responseName);
        if (responseClass == null) {
            throw new IllegalArgumentException("Unknown response: " + responseName);
        }
        Response response;
        try {
            response = responseClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create response: " + responseName, e);
        }
        response.setArgs(args);
        return response;
    }
};
