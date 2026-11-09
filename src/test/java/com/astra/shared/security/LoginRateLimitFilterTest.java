package com.astra.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginRateLimitFilterTest {

    private static final class MovingClock extends Clock {
        private Instant now = Instant.parse("2026-11-09T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final MovingClock clock = new MovingClock();
    private final LoginRateLimitFilter filter = new LoginRateLimitFilter(clock);

    private int login(String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test
    void sextaTentativaNoMesmoMinutoRecebe429EDepoisLibera() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertThat(login("10.0.0.1")).isEqualTo(200);
        }
        assertThat(login("10.0.0.1")).isEqualTo(429);
        assertThat(login("10.0.0.2")).isEqualTo(200);

        clock.advance(Duration.ofSeconds(61));
        assertThat(login("10.0.0.1")).isEqualTo(200);
    }

    @Test
    void enderecosComJanelaVencidaSaemDaMemoria() throws Exception {
        for (int i = 0; i < 10_001; i++) {
            login("10." + (i >> 16 & 255) + "." + (i >> 8 & 255) + "." + (i & 255));
        }
        assertThat(filter.trackedAddresses()).isEqualTo(10_001);

        clock.advance(Duration.ofSeconds(61));
        login("192.168.0.1");

        assertThat(filter.trackedAddresses()).isEqualTo(1);
    }
}
