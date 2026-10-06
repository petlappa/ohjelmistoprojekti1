package fi.haagahelia.ticketguru.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import fi.haagahelia.ticketguru.domain.Kayttaja;
import fi.haagahelia.ticketguru.repository.KayttajaRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/*")
                                .hasAnyRole("MYYJA", "TAPAHTUMAKOORDINAATTORI")
                        .requestMatchers(HttpMethod.GET, "/api/events/*/ticket-types")
                                .hasAnyRole("MYYJA", "TAPAHTUMAKOORDINAATTORI")
                        .requestMatchers(HttpMethod.POST, "/api/events").hasRole("TAPAHTUMAKOORDINAATTORI")
                        .requestMatchers(HttpMethod.PUT, "/api/events/*").hasRole("TAPAHTUMAKOORDINAATTORI")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/*").hasRole("TAPAHTUMAKOORDINAATTORI")
                        .requestMatchers(HttpMethod.POST, "/api/events/*/ticket-types")
                                .hasRole("TAPAHTUMAKOORDINAATTORI")
                        .requestMatchers("/api/sales", "/api/sales/**").hasRole("MYYJA")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(KayttajaRepository kayttajat) {
        return kayttajanimi -> {
            Kayttaja kayttaja = kayttajat.findByKayttajanimi(kayttajanimi)
                    .orElseThrow(() -> new UsernameNotFoundException(kayttajanimi));
            return User.withUsername(kayttaja.getKayttajanimi())
                    .password(kayttaja.getSalasana())
                    .roles(kayttaja.getRooli().getNimi())
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
