package ar.edu.itba.pdc.parser;

import ar.edu.itba.pdc.connection.Server;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

enum EmailToServerMap {
    INSTANCE;

    private final Map<String, Server> map;

    EmailToServerMap() {
        this.map = new ConcurrentHashMap<>();
    }

    public void addEmailToServerMapping(final String email, final Server server) {
        map.put(email, server);
    }

    public boolean removeEmailMapping(final String email) {
        if (map.containsKey(email)) {
            map.remove(email);
            return true;
        } else {
            return false;
        }
    }

    public Server getServer(final String email) {
        return map.get(email);
    }

    public Set<Map.Entry<String, Server>> getEntries() {
        return map.entrySet();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
