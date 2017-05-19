package it.pajak.leaderelection.cluster;

import com.orbitz.consul.Consul;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.Session;

public class ReplicaSession implements Runnable {
    private Consul consul;
    private String serviceName;
    private long ttl;
    private String id;

    public ReplicaSession(Consul consul, String serviceName, long ttl) {
        this.consul = consul;
        this.serviceName = serviceName;
        this.ttl = ttl;

        id = createSession();
    }

    public synchronized void run() {
        while (true) {
            try {
                wait(ttl / 2 * 1000);
            } catch (InterruptedException e) {}

            consul.sessionClient().renewSession(getId());
        }
    }

    public String getId() {
        return id;
    }

    private String createSession() {
        final Session session = ImmutableSession.builder()
            .name(serviceName)
            .ttl(String.format("%ss", ttl))
            .build();

        id = consul
            .sessionClient()
            .createSession(session).getId();

        return id;
    }
}
