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
                                .requestMatchers(
                                        "/",
                                        "/index.html",
                                        "/reg.html",
                                        "/dashboard.html",
                                        "/style.css",
                                        "/script.js",
                                        "/dashboard.css",
                                        "/js/**",
                                        "/favicon.ico"
                                ).permitAll()
                                .requestMatchers("/api/v1/users/register").permitAll()
                                .requestMatchers("/api/v1/users/login").permitAll()
                                // USER endpoints
                                .requestMatchers("/api/v1/users/me", "/api/v1/users/me/**").hasAnyRole("USER", "SYSTEM_ADMIN")
                                .requestMatchers("/api/v1/notifications/**").hasAnyRole("USER", "SYSTEM_ADMIN")

                                // GROUPS
                                // просмотр групп
                                .requestMatchers(HttpMethod.GET, "/api/v1/groups/**").hasAnyRole("USER", "SYSTEM_ADMIN")
                                // создание группы
                                .requestMatchers(HttpMethod.POST, "/api/v1/groups").hasAnyRole("USER", "SYSTEM_ADMIN")
                                // изменение/удаление группы
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/groups/*").hasAnyRole("SYSTEM_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/groups/*").hasAnyRole("SYSTEM_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/groups/*").hasAnyRole("SYSTEM_ADMIN")

                                // GROUP MEMBERS
                                // просмотр участников
                                .requestMatchers(HttpMethod.GET, "/api/v1/groups/*/members").hasAnyRole("USER", "SYSTEM_ADMIN")
                                // вступление в группу
                                .requestMatchers(HttpMethod.POST, "/api/v1/groups/*/members/me").hasAnyRole("USER", "SYSTEM_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/groups/*/members/me").hasAnyRole("USER", "SYSTEM_ADMIN")
                                // удалить участника (разрешаем USER, но право проверяем в сервисе: только OWNER может кикать)
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/groups/*/members/*").hasAnyRole("USER", "SYSTEM_ADMIN")
                                // управление участниками
                                .requestMatchers("/api/v1/groups/*/members/**").hasAnyRole("SYSTEM_ADMIN")
                                // QUEUES
                                // просмотр очередей
                                .requestMatchers(HttpMethod.GET, "/api/v1/groups/*/queues").hasAnyRole("USER", "SYSTEM_ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/queues/**").hasAnyRole("USER", "SYSTEM_ADMIN")
                                // создание очереди
                                .requestMatchers(HttpMethod.POST, "/api/v1/groups/*/queues").hasAnyRole("USER", "SYSTEM_ADMIN")
                                // QUEUE ACTIONS
                                // вход/выход из очереди
                                .requestMatchers(HttpMethod.POST, "/api/v1/queues/*/join").hasAnyRole("USER", "SYSTEM_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/queues/*/leave").hasAnyRole("USER", "SYSTEM_ADMIN")
                                // просмотр записей очереди
                                .requestMatchers(HttpMethod.GET, "/api/v1/queues/*/entries").hasAnyRole("USER", "SYSTEM_ADMIN")
                                // изменение статуса записи (право проверяем в сервисе: OWNER/MODERATOR/создатель очереди)
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/queues/*/entries/*").hasAnyRole("USER", "SYSTEM_ADMIN")
                                // другие операции с entries оставляем только админам
                                .requestMatchers("/api/v1/queues/*/entries/**").hasAnyRole("SYSTEM_ADMIN")
                                // закрытие/удаление очереди (право проверяем в сервисе: creator или OWNER)
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/queues/*/close").hasAnyRole("USER", "SYSTEM_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/queues/*").hasAnyRole("USER", "SYSTEM_ADMIN")
                                // изменение очереди (пока оставляем только админам)
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/queues/*").hasAnyRole("SYSTEM_ADMIN")

                                // DEFAULT
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
