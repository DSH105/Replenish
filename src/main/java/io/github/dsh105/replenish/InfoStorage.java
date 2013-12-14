package io.github.dsh105.replenish;

public class InfoStorage {

    private String info;
    private boolean bound;

    public InfoStorage(String info, boolean bound) {
        this.info = info;
        this.bound = bound;
    }

    public String getInfo() {
        return info;
    }

    public boolean isBound() {
        return bound;
    }
}