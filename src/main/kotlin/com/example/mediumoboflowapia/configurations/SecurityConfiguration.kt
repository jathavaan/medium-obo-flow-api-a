package com.example.mediumoboflowapia.configurations

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component
import java.time.Instant

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val jwtToOidcConverter: JwtToOidcConverter
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { requests ->
            requests
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "actuator/health", "actuator/prometheus")
                .permitAll()
                .anyRequest().authenticated()
        }.oauth2ResourceServer { oauth2 ->
            oauth2
                .jwt { jwtConfigurer ->
                    jwtConfigurer.jwtAuthenticationConverter(jwtToOidcConverter)
                }
        }

        return http.build()
    }

    @Component
    class JwtToOidcConverter : Converter<Jwt, AbstractAuthenticationToken> {
        private val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

        override fun convert(jwt: Jwt): AbstractAuthenticationToken {
            val authorities: Collection<GrantedAuthority> = jwtGrantedAuthoritiesConverter.convert(jwt) ?: emptyList()
            val oidcUser: OidcUser = createOidcUser(jwt, authorities)
            return OidcAuthenticationToken(oidcUser, authorities, jwt.tokenValue)
        }

        private fun createOidcUser(jwt: Jwt, authorities: Collection<GrantedAuthority>): OidcUser {
            val claims = jwt.claims
            val oidcIdToken = OidcIdToken(
                jwt.tokenValue,
                jwt.issuedAt ?: Instant.now(),
                jwt.expiresAt ?: Instant.now().plusSeconds(3600),
                claims
            )

            val oidcUserInfo = OidcUserInfo(claims)
            return DefaultOidcUser(authorities, oidcIdToken, oidcUserInfo, "sub")
        }
    }

    class OidcAuthenticationToken(
        private val oidcUser: OidcUser,
        authorities: Collection<GrantedAuthority>,
        private val token: String
    ) : AbstractAuthenticationToken(authorities) {
        init {
            isAuthenticated = true
        }

        override fun getCredentials(): Any = token
        override fun getPrincipal(): Any = oidcUser
    }
}