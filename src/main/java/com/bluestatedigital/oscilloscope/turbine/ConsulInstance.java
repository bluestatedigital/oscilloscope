package com.bluestatedigital.oscilloscope.turbine;

public class ConsulInstance {
    public static enum Status {
        UP, DOWN
    }
    private final String hostname;
    private final int port;
    private final String cluster;
    private final Status status;

    public ConsulInstance(String hostname, int port, String cluster, Status status) {
        this.hostname = hostname;
        this.port = port;
        this.cluster = cluster;
        this.status = status;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getCluster() {
        return cluster;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getHostname() == null) ? 0 : getHostname().hashCode());
        result = prime * result + ((getPort() == 0) ? 0 : String.valueOf(getPort()).hashCode());
        result = prime * result + ((getCluster() == null) ? 0 : getCluster().hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        ConsulInstance other = (ConsulInstance) obj;
        if (getHostname() == null) {
            if (other.getHostname() != null)
                return false;
        } else if (!getHostname().equals(other.getHostname()))
            return false;

        if (getPort() != other.getPort())
            return false;

        if (getCluster() == null) {
            if (other.getCluster() != null)
                return false;
        } else if (!getCluster().equals(other.getCluster()))
            return false;

        if (status != other.status)
            return false;

        return true;
    }
}
