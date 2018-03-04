package com.rcg.walmart.locking;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Optional;

public interface LockService {

    public Optional<Lock> acquireLock(String lockId);

    public boolean releaseLock(Lock lock);

}
