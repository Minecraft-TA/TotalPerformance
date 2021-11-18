package com.github.minecraft_ta.totalperformance.dimThreading;

public class ForwardingEventHandlerInvocationException extends Exception {

    private final int listenerIndex;

    public ForwardingEventHandlerInvocationException(Throwable cause, int listenerIndex) {
        super(cause);
        this.listenerIndex = listenerIndex;
    }

    public int getListenerIndex() {
        return this.listenerIndex;
    }
}
