package com.lemongo97.iptv.iptvmanager.configuration.websocket;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.websocket")
public class WebSocketProperties {
	private String prefix = "/";
}
