package it.pajak.leaderelection.cluster;

import com.google.common.base.Optional;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.kv.Value;

public class Replica {
    private Consul client;

    public Replica(Consul client) {
        this.client = client;
    }

    public boolean claimLeadership(String serviceName, ReplicaSession session) {
        final String key = getServiceKey(serviceName);

        if (client.keyValueClient().acquireLock(key, session.getId(), session.getId())) {
            return true;
        }

        return false;
    }

    public String getLeaderSessionId(String serviceName) {
        String key = getServiceKey(serviceName);

        Optional<Value> value = client.keyValueClient().getValue(key);

        if (value.isPresent() && value.get().getSession().isPresent()){
            return value.get().getValueAsString().get();
        }

        return null;
    }

    private String getServiceKey(String serviceName) {
        return "service/" + serviceName + "/leader";
    }
}
