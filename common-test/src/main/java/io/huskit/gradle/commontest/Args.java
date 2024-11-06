package io.huskit.gradle.commontest;

import org.junit.jupiter.api.extension.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public @interface Args {

    @Retention(RetentionPolicy.RUNTIME)
    @ExtendWith(HeadersExtension.class)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @interface HttpHeaders {
    }
}

final class HeadersExtension implements ParameterResolver {

    static final String headersString = "HTTP/1.1 200 OK\r\n"
        + "Api-Version: 1.46\r\n"
        + "Content-Type: application/json\r\n"
        + "Date: Tue, 22 Oct 2024 01:15:00 GMT\r\n"
        + "Docker-Experimental: false\r\n"
        + "Ostype: linux\r\n"
        + "Server: Docker/27.0.3 (linux)\r\n"
        + "Content-Length: 3\r\n"
        + "\r\n";

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        try {
            var parameter = parameterContext.getParameter();
            if (parameter.getType().equals(String.class)) {
                return true;
            } else if (parameter.getType().equals(Consumer.class)) {
                var parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                var consumerType = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];
                return consumerType.getRawType().equals(Map.class) &&
                    consumerType.getActualTypeArguments()[0].equals(String.class) &&
                    consumerType.getActualTypeArguments()[1].equals(String.class);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var parameter = parameterContext.getParameter();
        if (parameter.getType().equals(String.class)) {
            return headersString;
        } else if (parameter.getType().equals(Consumer.class)) {
            return (Consumer<Map<String, String>>) headers -> {
                assertThat(headers).hasSize(7);
                assertThat(headers).containsEntry("Api-Version", "1.46");
                assertThat(headers).containsEntry("Content-Type", "application/json");
                assertThat(headers).containsEntry("Date", "Tue, 22 Oct 2024 01:15:00 GMT");
                assertThat(headers).containsEntry("Docker-Experimental", "false");
                assertThat(headers).containsEntry("Ostype", "linux");
                assertThat(headers).containsEntry("Server", "Docker/27.0.3 (linux)");
                assertThat(headers).containsEntry("Content-Length", "3");
            };
        } else {
            throw new ParameterResolutionException("Unsupported type: " + parameter.getType());
        }
    }
}
