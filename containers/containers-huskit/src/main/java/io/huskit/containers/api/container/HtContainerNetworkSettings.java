package io.huskit.containers.api.container;

import io.huskit.common.port.MappedPort;

import java.util.List;
import java.util.Optional;

public interface HtContainerNetworkSettings {

    Boolean hairpinMode();

    String sandboxKey();

    String sandboxId();

    String globalIpv6Address();

    Integer globalIpv6PrefixLen();

    List<MappedPort> ports();

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

    Integer firstMappedPort();
}
