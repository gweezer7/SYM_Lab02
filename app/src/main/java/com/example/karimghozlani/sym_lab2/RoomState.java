package com.example.karimghozlani.sym_lab2;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by karimghozlani on 08.11.15.
 */
public class RoomState implements Serializable {
    private int number;
    private boolean isBedDone;
    private boolean isTrashEmpty;
    private boolean isBathroomReady;

    private static Random random = new Random();

    public static RoomState generateRoomState() {
        return new RoomState(random.nextInt(5000), random.nextBoolean(), random.nextBoolean(), random.nextBoolean());
    }

    protected RoomState(int number, boolean isBedDone, boolean isTrashEmpty, boolean isBathroomReady) {
        this.number = number;
        this.isBedDone = isBedDone;
        this.isTrashEmpty = isTrashEmpty;
        this.isBathroomReady = isBathroomReady;
    }

    public int getNumber() {
        return number;
    }

    public boolean isBedDone() {
        return isBedDone;
    }

    public boolean isTrashEmpty() {
        return isTrashEmpty;
    }

    public boolean isBathroomReady() {
        return isBathroomReady;
    }

    @Override
    public String toString() {
        return "RoomState{" +
                "number=" + number +
                ", isBedDone=" + isBedDone +
                ", isTrashEmpty=" + isTrashEmpty +
                ", isBathroomReady=" + isBathroomReady +
                '}';
    }
}
