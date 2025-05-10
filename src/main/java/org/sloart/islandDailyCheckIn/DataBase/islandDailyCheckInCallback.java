package org.sloart.islandDailyCheckIn.DataBase;

@FunctionalInterface
public interface islandDailyCheckInCallback<T> {
    void call(T result);
}
