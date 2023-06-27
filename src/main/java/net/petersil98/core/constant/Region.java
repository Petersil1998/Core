package net.petersil98.core.constant;

public enum Region {
    AMERICA("america"),
    EUROPE("europe"),
    ASIA("asia"),
    SEA("sea");

    private final String name;

    Region(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
