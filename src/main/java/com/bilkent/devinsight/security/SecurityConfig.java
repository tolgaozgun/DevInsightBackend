package com.bilkent.devinsight.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JWTUserService jwtUserService;
	private final JWTFilter jwtFilter;

	private static final String[] AUTH_WHITELIST = {
			"/v3/api-docs/**",
			"/swagger-ui/**",
			"/auth/me",
			"/auth/login",
			"/auth/register",
			"/auth/request-reset",
			"/auth/test-reset",
			"/auth/validate-reset",
			"/auth/reset-password"
	};

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors();
		http
				.csrf().disable()
				.authorizeHttpRequests((request) -> {
					try {
						request
								.requestMatchers(AUTH_WHITELIST)
								.permitAll()
								.anyRequest().authenticated()
								.and()
								.sessionManagement()
								.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
								.and()
								.authenticationProvider(authenticationProvider());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(jwtUserService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}
}