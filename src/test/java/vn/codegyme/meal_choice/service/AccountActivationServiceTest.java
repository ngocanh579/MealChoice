package vn.codegyme.meal_choice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.entity.VerificationToken;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.repository.VerificationTokenRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountActivationServiceTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountActivationEmailService emailService;

    @InjectMocks
    private AccountActivationService accountActivationService;

    @Test
    void createAndSendTokenDisablesAccountAndCreates24HourToken() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setIsActive(true);
        accountActivationService.createAndSendToken(user);

        assertFalse(user.getIsActive());
        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepository).save(tokenCaptor.capture());
        VerificationToken token = tokenCaptor.getValue();
        assertSame(user, token.getUser());
        assertNotNull(token.getToken());
        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now().plusHours(23)));
        verify(emailService).sendActivationEmail(user, token);
    }

    @Test
    void activateEnablesAccountAndMarksTokenVerified() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setIsActive(false);
        VerificationToken token = new VerificationToken();
        token.setToken("valid-token");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(verificationTokenRepository.findByToken("valid-token")).thenReturn(java.util.Optional.of(token));

        String activatedEmail = accountActivationService.activate("valid-token");

        assertEquals("user@example.com", activatedEmail);
        assertTrue(user.getIsActive());
        assertNotNull(token.getVerifiedAt());
        verify(userRepository).save(user);
        verify(verificationTokenRepository).save(token);
    }
}
