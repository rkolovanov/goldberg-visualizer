package leti.practice.commands;

import leti.practice.Controller;

public class AddEdgeCommand implements Command {
    private final Controller controller;
    private String source, destination;
    private Double capacity;

    public AddEdgeCommand(Controller controller) {
        this.controller = controller;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean execute() {
        if (controller != null) {
            controller.addEdge(source, destination, capacity);
            return true;
        } else {
            return false;
        }
    }
}
