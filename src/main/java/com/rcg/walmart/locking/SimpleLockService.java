package com.rcg.walmart.locking;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimpleLockService implements LockService {
    private static long LOCK_DURATION_SECONDS = 10;
    private ConcurrentHashMap<String, Lock> locks;
    private Clock clock;
    //This class is a simplistic mock posing as a distributed acquireLock that would be expected to use some sort of consenseus
    //protocol to create locks prior to doing certain actions

    public SimpleLockService() {
        this(Clock.system(ZoneOffset.UTC));
    }

    public SimpleLockService(Clock clock) {
        this.clock = clock;
        this.locks = new ConcurrentHashMap<String, Lock>();
    }


    public Optional<Lock> acquireLock(String lockId) {
        if (locks.containsKey(lockId)) {
            Lock lock = locks.get(lockId);
            ZonedDateTime currTime = ZonedDateTime.now(clock);
            if (lock.getLockEnd().isAfter(currTime)) {
                //lock is good, return empty
                return Optional.empty();
            } else {
                locks.remove(lockId);
            }
        }
        ZonedDateTime lockEnds = ZonedDateTime.now(clock).plusSeconds(LOCK_DURATION_SECONDS);
        Lock lock = new Lock(lockId, lockEnds);
        locks.put(lockId, lock);
        return Optional.of(lock);
    }

    public boolean releaseLock(Lock lock) {
        //using the end time object as both a time and a semaphore for confirming
        //releasing correct lock
        if (!locks.containsKey(lock.getLockId())) {
            return false;
        }

        if (locks.get(lock.getLockId()).getLockEnd() == lock.getLockEnd()) {
            locks.remove(lock.getLockId());
            return true;
        } else {
            return false;
        }
    }
}
