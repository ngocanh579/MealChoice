package vn.codegyme.meal_choice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.codegyme.meal_choice.dto.AdminMerchantResponse;
import vn.codegyme.meal_choice.dto.MerchantDecisionRequest;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.MerchantAdminProfile;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.MerchantAdminProfileRepository;
import vn.codegyme.meal_choice.repository.MerchantFeatureRepository;
import vn.codegyme.meal_choice.repository.RefreshTokenRepository;
import vn.codegyme.meal_choice.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMerchantManagementServiceTest {

    @Mock
    private MerchantFeatureRepository merchantRepository;

    @Mock
    private MerchantAdminProfileRepository adminProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AdminMerchantManagementService adminMerchantService;

    private UUID merchantId;
    private Merchant merchant;
    private User owner;

    @BeforeEach
    void setUp() {
        merchantId = UUID.randomUUID();
        owner = new User();
        owner.setDisplayName("Chủ quán");
        owner.setIsActive(true);

        merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setRestaurantName("Quán ngon");
        merchant.setEmail("merchant@example.com");
        merchant.setPhone("0900000000");
        merchant.setAddress("Huế");
        merchant.setMerchantStatus("PENDING");
        merchant.setUser(owner);

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
    }

    @Test
    void approveChangesStatusAndKeepsAccountActive() {
        when(adminProfileRepository.findByMerchantId(merchantId)).thenReturn(Optional.empty());
        AdminMerchantResponse response = adminMerchantService.decide(
                merchantId,
                new MerchantDecisionRequest("APPROVE", null)
        );

        assertEquals("APPROVED", response.status());
        assertTrue(response.accountActive());
        verify(merchantRepository).save(merchant);
        verify(userRepository).save(owner);
    }

    @Test
    void rejectRequiresReason() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                adminMerchantService.decide(merchantId, new MerchantDecisionRequest("REJECT", " "))
        );

        assertEquals("Vui lòng nhập lý do từ chối merchant", exception.getMessage());
        verify(merchantRepository, never()).save(any());
    }

    @Test
    void lockAndUnlockRestorePreviousStatus() {
        merchant.setMerchantStatus("APPROVED");
        when(adminProfileRepository.findByMerchantId(merchantId)).thenReturn(Optional.empty());

        AdminMerchantResponse locked = adminMerchantService.setLocked(merchantId, true);
        assertEquals("BLOCKED", locked.status());
        assertFalse(owner.getIsActive());

        ArgumentCaptor<MerchantAdminProfile> profileCaptor = ArgumentCaptor.forClass(MerchantAdminProfile.class);
        verify(adminProfileRepository).save(profileCaptor.capture());
        when(adminProfileRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.of(profileCaptor.getValue()));

        AdminMerchantResponse unlocked = adminMerchantService.setLocked(merchantId, false);
        assertEquals("APPROVED", unlocked.status());
        assertTrue(owner.getIsActive());
    }
}
