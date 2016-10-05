package org.wsock.internal;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Created by joco on 05.10.16.
 */
public class TokenUtil {
    public static String extactToken(ServerHttpRequest serverHttpRequest) {
        MultiValueMap<String, String> params = UriComponentsBuilder.fromHttpRequest(serverHttpRequest).build().getQueryParams();
        return params.getFirst("token");
    }

    public static String extactToken(URI uri) {
        MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
        return params.getFirst("token");
    }
}
