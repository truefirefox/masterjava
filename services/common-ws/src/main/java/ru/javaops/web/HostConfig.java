package ru.javaops.web;

import com.typesafe.config.Config;
import lombok.Getter;
import org.slf4j.event.Level;
import ru.javaops.masterjava.config.Configs;

/**
 * Created by val on 2017-05-05.
 */
@Getter
public class HostConfig {
    public static final HostConfig HOST =
            new HostConfig(Configs.getConfig("hosts.conf", "hosts", "mail"));

    private final String endpoint;
    private final Level debugLevel;
    private final String user;
    private final String password;

    private HostConfig(Config hosts) {
        this.endpoint = hosts.getString("endpoint");
        this.debugLevel = Level.valueOf(hosts.getString("debug.client"));
        this.user = hosts.getString("user");
        this.password = hosts.getString("password");
    }

}
