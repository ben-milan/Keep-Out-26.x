package com.bmk.keepout.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import java.util.ArrayList;
import java.util.List;

public class TickScheduler {
    private record ScheduledTask(int ticksRemaining, Runnable task) {}
    private static final List<ScheduledTask> tasks = new ArrayList<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            List<ScheduledTask> remaining = new ArrayList<>();
            for (ScheduledTask t : tasks) {
                if (t.ticksRemaining() <= 1) t.task().run();
                else remaining.add(new ScheduledTask(t.ticksRemaining() - 1, t.task()));
            }
            tasks.clear();
            tasks.addAll(remaining);
        });
    }

    public static void schedule(int delayTicks, Runnable task) {
        tasks.add(new ScheduledTask(delayTicks, task));
    }
}