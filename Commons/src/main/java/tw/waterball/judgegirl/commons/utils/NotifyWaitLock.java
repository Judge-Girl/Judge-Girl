/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.commons.utils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A syntax-sugar that provides synchronization-free statement for notify-wait,
 * ##Note##: you have to invoke doWait() / doNotify() / doNotifyAll()
 * instead of wait() / notify() / notifyAll() on this class
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class NotifyWaitLock {
    private final Object monitor = new Object();

    public void doNotify() {
        synchronized (monitor) {
            monitor.notify();
        }
    }

    public void doNotifyAll() {
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    public void doWait(long timeout) {
        AtomicBoolean completed = new AtomicBoolean(false);
        Thread timeoutThread = new Thread(() -> {
            Delay.delay(timeout);
            if (completed.compareAndSet(false, true)) {
                // timeout
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        });
        timeoutThread.start();
        synchronized (monitor) {
            try {
                monitor.wait();
                if (completed.compareAndSet(true, true)) {
                    throw new IllegalStateException("Timeout");
                } else {
                    timeoutThread.interrupt();
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void doWait() {
        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
