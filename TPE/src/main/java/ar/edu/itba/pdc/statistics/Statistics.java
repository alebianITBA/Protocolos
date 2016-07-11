package ar.edu.itba.pdc.statistics;

import java.util.concurrent.atomic.AtomicInteger;

public enum Statistics {
    INSTANCE;

    private final AtomicInteger clientRequests = new AtomicInteger(0);
    private final AtomicInteger bytesTransferred = new AtomicInteger(0);

    public void newClientRequest() {
        clientRequests.incrementAndGet();
    }

    public int getClientRequests() {
        return clientRequests.get();
    }

    public void newTransfer(int bytes) {
        bytesTransferred.addAndGet(bytes);
    }

    public int getBytesTransferred() {
        return bytesTransferred.get();
    }
}
