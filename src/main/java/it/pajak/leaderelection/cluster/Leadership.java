package it.pajak.leaderelection.cluster;

public class Leadership implements Runnable {
    private Replica replica;
    private ReplicaSession replicaSession;
    private String serviceName;
    private long delay;

    public Leadership(Replica replica, ReplicaSession replicaSession, String serviceName, long attemptsDelay) {
        this.replica = replica;
        this.replicaSession = replicaSession;
        this.serviceName = serviceName;
        this.delay = attemptsDelay;
    }

    public synchronized void run() {
        boolean gotLeadership = false;

        while(true) {
            try {
                wait(delay * 1000);
            } catch (InterruptedException e) {}

            gotLeadership = replica.claimLeadership(serviceName, replicaSession);

            if (gotLeadership) {
                System.out.println("I RULE!");
            } else {
                System.out.println("NOT MY TIME. CURRENT LEADER IS " + replica.getLeaderSessionId(serviceName));
            }
        }
    }
}
