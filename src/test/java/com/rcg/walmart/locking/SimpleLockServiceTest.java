package com.rcg.walmart.locking;

import com.rcg.walmart.locking.Lock;
import com.rcg.walmart.locking.SimpleLockService;
import org.junit.*;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class SimpleLockServiceTest {
    private static String lock1 = "lock1";
    private static String lock2 = "lock2";


    @Test
    public void sunnyDay() {
        SimpleLockService unit = new SimpleLockService();
        Optional<Lock> result = unit.acquireLock(lock1);
        assertTrue(result.isPresent());

        Optional<Lock> secondAttempt = unit.acquireLock(lock1);
        assertTrue("lock is still valid, 2nd attempt should fail", !secondAttempt.isPresent());

        Optional<Lock> differentLockAttempt = unit.acquireLock(lock2);
        assertTrue(differentLockAttempt.isPresent());
    }

    @Test
    public void testExpiredLock() {
        Clock clock = mock(Clock.class);
        Instant fauxNow = Instant.now();
        Instant hourAgo = fauxNow.minus(Duration.ofHours(1));

        doReturn(hourAgo).doReturn(fauxNow).doReturn(fauxNow).when(clock).instant();
        doReturn(ZoneOffset.UTC).doReturn(ZoneOffset.UTC).when(clock).getZone();

        SimpleLockService unit = new SimpleLockService(clock);


        Optional<Lock> result = unit.acquireLock(lock1);
        assertTrue(result.isPresent()); //first attempt, gets lock(uses old time)

        result = unit.acquireLock(lock1);
        assertTrue(result.isPresent()); //second attempt steals stale lock

        result = unit.acquireLock(lock1);
        assertFalse(result.isPresent()); //third attempt fails as lock is still valid
    }

    @Test
    public void testReleaseLock() {
        SimpleLockService unit = new SimpleLockService();
        Optional<Lock> result = unit.acquireLock(lock1);
        assertTrue(result.isPresent());

        unit.releaseLock(result.get());

        result = unit.acquireLock(lock1);
        assertTrue(result.isPresent());
    }

    @Test
    public void testReleaseMismatchedLock() {
        SimpleLockService unit = new SimpleLockService();
        Optional<Lock> result = unit.acquireLock(lock1);
        assertTrue(result.isPresent());

        Lock bogusLock = new Lock(lock1, ZonedDateTime.now());
        unit.releaseLock(bogusLock);

        //test if lock is still present
        Optional<Lock> shouldNotWork = unit.acquireLock(lock1);
        assertFalse(shouldNotWork.isPresent());
    }

    @Test
    public void releaseMissingLock() {
        SimpleLockService unit = new SimpleLockService();
        Lock bogusLock = new Lock(lock1, ZonedDateTime.now());
        assertFalse(unit.releaseLock(bogusLock));

    }
}
