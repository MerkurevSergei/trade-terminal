package ru.merkurev.stuff.concurrency.robotstep;

import java.util.Objects;

public class Leg extends Thread {
    private final String name;

    private final Robot robot;

    public Leg(String name, Robot robot) {
        this.name = Objects.requireNonNull(name);
        this.robot = robot;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            robot.step(this);
        }
    }

    public String getLegName() {
        return name;
    }
}
