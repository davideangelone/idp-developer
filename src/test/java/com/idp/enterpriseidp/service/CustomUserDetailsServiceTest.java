package com.idp.enterpriseidp.service;

import com.idp.enterpriseidp.entity.User;
import com.idp.enterpriseidp.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("loadUserByUsername restituisce i dettagli per un utente esistente")
    void loadUserByUsername_existingUser_returnsDetails() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setAddress("Via Roma 1, Bologna");
        user.setPhoneNumber("+39 333 1234567");
        user.setRoles(Set.of("USER"));
        user.setGroups(Set.of("users"));
        user.setEnabled(true);
        user.setEmailVerified(true);

        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("test");

        assertThat(details).isInstanceOf(CustomUserDetailsService.CustomUserDetails.class);
        assertThat(details.getUsername()).isEqualTo("test");
        assertThat(details.getPassword()).isEqualTo("encoded-password");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.getAuthorities()).hasSize(1);
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        assertThat(((CustomUserDetailsService.CustomUserDetails) details).user()).isSameAs(user);
    }

    @Test
    @DisplayName("loadUserByUsername lancia UsernameNotFoundException per utente inesistente")
    void loadUserByUsername_nonExistingUser_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    @DisplayName("CustomUserDetails espone i campi dell'entita User correttamente")
    void customUserDetails_exposesUserFields() {
        User user = new User();
        user.setId(2L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("encoded-password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAddress("123 Main Street");
        user.setPhoneNumber("+1 555 1234567");
        user.setEnabled(false);
        user.setEmailVerified(false);

        CustomUserDetailsService.CustomUserDetails details = new CustomUserDetailsService.CustomUserDetails(user);

        assertThat(details.user()).isSameAs(user);
        assertThat(details.getUsername()).isEqualTo("john");
        assertThat(details.getPassword()).isEqualTo("encoded-password");
        assertThat(details.isEnabled()).isFalse();
    }
}
