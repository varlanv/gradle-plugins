package io.huskit.containers.api.container;

import java.util.Optional;

public interface HtContainerNetworkSettings {

    Boolean hairpinMode();

    String sandboxKey();

    String sandboxId();

    String globalIpv6Address();

    Integer globalIpv6PrefixLen();

    Integer ipPrefixLen();

    String macAddress();

    String linkLocalIpv6Address();

    Integer linkLocalIpv6PrefixLen();

    String gateway();

    String endpointId();

    Optional<String> secondaryIpV6Addresses();

    String ipv6Gateway();

    Optional<String> secondaryIpAddresses();

    String ipAddress();

    String bridge();
}
