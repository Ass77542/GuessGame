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

        this.responseMap = new HashMap<>();
        responseMap.put("CONNECTED", ServerResponse.Connected.class);
        responseMap.put("ROOM_CREATED", ServerResponse.RoomCreated.class);
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
        try {
            Command command = commandClass.getDeclaredConstructor().newInstance();
            command.setArgs(args);
            return command;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create command: " + commandName, e);
        }
   }

   public Response createResponse(String responseName, String[] args) {
        Class<? extends Response> responseClass = responseMap.get(responseName);
        if (responseClass == null) {
            throw new IllegalArgumentException("Unknown response: " + responseName);
        }
        try {
            Response response = responseClass.getDeclaredConstructor().newInstance();
            response.setArgs(args);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create response: " + responseName, e);
        }
    }
};
