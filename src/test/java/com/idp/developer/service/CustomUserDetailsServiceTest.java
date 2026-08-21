package com.idp.developer.service;

import java.util.Optional;
import java.util.Set;

import com.idp.developer.entity.Group;
import com.idp.developer.entity.Role;
import com.idp.developer.entity.User;
import com.idp.developer.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
        User user = getUser();

        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("test");

        assertThat(details).isInstanceOf(CustomUserDetailsService.CustomUserDetails.class);
        assertThat(details.getUsername()).isEqualTo("test");
        assertThat(details.getPassword()).isEqualTo("encoded-password");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.getAuthorities()).hasSize(2);
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "GROUP_users");
        assertThat(((CustomUserDetailsService.CustomUserDetails) details).user()).isSameAs(user);
    }

    private @NonNull User getUser() {
        Role role = new Role();
        role.setName("USER");

        Group group = new Group();
        group.setName("users");

        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setAddress("Via Roma 1, Bologna");
        user.setPhoneNumber("+39 333 1234567");
        user.setRoles(Set.of(role));
        user.setGroups(Set.of(group));
        user.setEnabled(true);
        user.setEmailVerified(true);
        return user;
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
