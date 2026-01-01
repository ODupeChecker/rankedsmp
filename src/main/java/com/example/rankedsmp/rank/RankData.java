package com.example.rankedsmp.rank;

import java.util.UUID;

public record RankData(UUID uuid, int rank) {
    public boolean isRanked() {
        return rank > 0;
    }
}
