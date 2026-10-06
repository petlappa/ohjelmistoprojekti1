package fi.haagahelia.ticketguru.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

final class BasicAuth {

    static RequestPostProcessor myyja() {
        return httpBasic("myyja", "salasana");
    }

    static RequestPostProcessor koordinaattori() {
        return httpBasic("koordinaattori", "salasana");
    }

    private BasicAuth() {
    }
}
