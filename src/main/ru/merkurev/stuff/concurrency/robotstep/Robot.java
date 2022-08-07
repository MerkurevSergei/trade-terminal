package ru.merkurev.stuff.concurrency.robotstep;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Robot {

    private Leg prevLeg;
    private final Leg leftLeg;
    private final Leg rightLeg;

    private final Lock lock;
    private final Condition condition;

    public Robot() {
        this.leftLeg = new Leg("Left", this);
        this.rightLeg = new Leg("Right", this);
        this.prevLeg = rightLeg;
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    public void start() {
        leftLeg.start();
        rightLeg.start();
    }

    public void step(Leg leg) {
        lock.lock();
        try {
            if (prevLeg == leg) {
                condition.await();
            }
            System.out.println(leg.getLegName());
            prevLeg = leg;
            condition.signalAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        lock.unlock();
    }

    public static void main(final String[] args) {
        try {
            Robot robot = new Robot();
            robot.start();

            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
