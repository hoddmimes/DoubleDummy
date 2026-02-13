package com.hoddmimes.bridgeanalyzer.game;

import com.hoddmimes.bridgeanalyzer.model.Direction;

public class Deal {
    private final Hand[] hands = new Hand[4];
    private String boardName;
    private int boardNumber;
    private Direction dealer;
    private String vulnerability;

    public Deal() {
        for (int i = 0; i < 4; i++) {
            hands[i] = new Hand();
        }
    }

    public Hand hand(Direction dir) { return hands[dir.index()]; }
    public void setHand(Direction dir, Hand hand) { hands[dir.index()] = hand; }

    public String boardName() { return boardName; }
    public void setBoardName(String name) { this.boardName = name; }

    public int boardNumber() { return boardNumber; }
    public void setBoardNumber(int n) { this.boardNumber = n; }

    public Direction dealer() { return dealer; }
    public void setDealer(Direction d) { this.dealer = d; }

    public String vulnerability() { return vulnerability; }
    public void setVulnerability(String v) { this.vulnerability = v; }

    public String displayHands() {
        StringBuilder sb = new StringBuilder();
        for (Direction dir : Direction.values()) {
            sb.append(String.format("  %s: %s%n", dir.name().charAt(0), hands[dir.index()].toDisplayString()));
        }
        return sb.toString();
    }
}
