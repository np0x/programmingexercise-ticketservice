package com.rcg.walmart.locking;

import java.time.ZonedDateTime;

public class Lock {
    String lockId;
    private ZonedDateTime lockEnd;

    public Lock(String lockId, ZonedDateTime lockEnd) {
        this.lockId = lockId;
        this.lockEnd = lockEnd;
    }

    public String getLockId() {
        return lockId;
    }

    public ZonedDateTime getLockEnd() {
        return lockEnd;
    }
}
