package ru.without_title.queue_project.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.without_title.queue_project.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/api/v1/users/register").permitAll()
                                .requestMatchers("/api/v1/users/login").permitAll()
                                // USER endpoints
                                .requestMatchers("/api/v1/users/me/**").hasAnyRole("USER", "ADMIN")
                                .requestMatchers("/api/v1/notifications/**").hasAnyRole("USER", "ADMIN")

                                // GROUPS
                                // просмотр групп
                                .requestMatchers(HttpMethod.GET, "/api/v1/groups/**").hasAnyRole("USER", "ADMIN")
                                // создание группы
                                .requestMatchers(HttpMethod.POST, "/api/v1/groups").hasAnyRole("USER", "ADMIN")
                                // изменение/удаление группы
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/groups/**").hasAnyRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/groups/**").hasAnyRole("ADMIN")

                                // GROUP MEMBERS
                                // просмотр участников
                                .requestMatchers(HttpMethod.GET, "/api/v1/groups/*/members").hasAnyRole("USER", "ADMIN")
                                // управление участниками
                                .requestMatchers("/api/v1/groups/*/members/**").hasAnyRole("ADMIN")
                                // QUEUES
                                // просмотр очередей
                                .requestMatchers(HttpMethod.GET, "/api/v1/groups/*/queues").hasAnyRole("USER", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/queues/**").hasAnyRole("USER", "ADMIN")
                                // создание очереди
                                .requestMatchers(HttpMethod.POST, "/api/v1/groups/*/queues").hasAnyRole("USER", "ADMIN")
                                // изменение/удаление очереди
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/queues/**").hasAnyRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/queues/**").hasAnyRole("ADMIN")

                                // QUEUE ACTIONS
                                // вход/выход из очереди
                                .requestMatchers("/api/v1/queues/*/join").hasAnyRole("USER", "ADMIN")
                                .requestMatchers("/api/v1/queues/*/leave").hasAnyRole("USER", "ADMIN")
                                // просмотр записей очереди
                                .requestMatchers(HttpMethod.GET, "/api/v1/queues/*/entries").hasAnyRole("USER", "ADMIN")
                                // изменение статуса записи
                                .requestMatchers("/api/v1/queues/*/entries/**").hasAnyRole("ADMIN")

                                // DEFAULT
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}