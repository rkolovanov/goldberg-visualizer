package leti.practice.commands;

import leti.practice.Controller;

public class LoadNetworkCommand implements Command {
    private final Controller controller;
    private String path;

    LoadNetworkCommand(Controller controller) {
        this.controller = controller;
    }

    void setPath(String path) {
        this.path = path;
    }

    @Override
    public void execute() {
        controller.loadNetwork(path);;
    }
}
