package vn.codegyme.meal_choice.service.impl;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.codegyme.meal_choice.dto.AddressDTO;
import vn.codegyme.meal_choice.dto.UserDTO;
import vn.codegyme.meal_choice.entity.Address;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.service.AuthService;
import vn.codegyme.meal_choice.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Override
    public UserDTO getCurrentUserProfile() {
        User user = getCurrentUser();
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateProfile(UserDTO userDTO) {
        User user = getCurrentUser();

        // CHỈ cập nhật các trường được phép
        user.setDisplayName(userDTO.getDisplayName());
        user.setDob(userDTO.getDob());
        user.setGender(userDTO.getGender());
        user.setAvatarUrl(userDTO.getAvatarUrl());

        // Cập nhật địa chỉ nếu có truyền danh sách
        if (userDTO.getAddresses() != null && !userDTO.getAddresses().isEmpty()) {
            updateAddresses(user, userDTO.getAddresses());
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO addAddress(AddressDTO addressDTO) {
        User user = getCurrentUser();

        Address address = new Address();
        address.setContactName(addressDTO.getContactName());
        address.setContactPhone(addressDTO.getContactPhone());
        address.setCity(addressDTO.getCity());
        address.setDistrict(addressDTO.getDistrict());
        address.setWard(addressDTO.getWard());
        address.setStreet(addressDTO.getStreet());
        address.setNote(addressDTO.getNote());
        address.setIsDefault(addressDTO.getIsDefault() != null && addressDTO.getIsDefault());
        address.setUser(user);

        // Nếu đặt làm mặc định, bỏ mặc định của các địa chỉ khác
        if (address.getIsDefault()) {
            user.getAddresses().forEach(a -> a.setIsDefault(false));
        }

        user.getAddresses().add(address);
        User updatedUser = userRepository.save(user);

        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        User user = getCurrentUser();

        // Tìm địa chỉ thuộc sở hữu của user hiện tại
        Address address = user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ hoặc địa chỉ không thuộc về bạn"));

        // Cập nhật thông tin địa chỉ (Giữ nguyên liên hệ contactName & contactPhone)
        address.setCity(addressDTO.getCity());
        address.setDistrict(addressDTO.getDistrict());
        address.setWard(addressDTO.getWard());
        address.setStreet(addressDTO.getStreet());
        address.setNote(addressDTO.getNote());

        // Quản lý trạng thái mặc định
        boolean isDefault = addressDTO.getIsDefault() != null && addressDTO.getIsDefault();
        if (isDefault) {
            user.getAddresses().forEach(a -> a.setIsDefault(a.getId().equals(addressId)));
        } else {
            address.setIsDefault(false);
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO deleteAddress(Long addressId) {
        User user = getCurrentUser();
        user.getAddresses().removeIf(address -> address.getId().equals(addressId));
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO setDefaultAddress(Long addressId) {
        User user = getCurrentUser();
        user.getAddresses().forEach(address -> {
            address.setIsDefault(address.getId().equals(addressId));
        });
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    // === Helper Methods ===

    private User getCurrentUser() {
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest().getSession();

        UUID userId = authService.getCurrentUserId(session);

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    private void updateAddresses(User user, List<AddressDTO> addressDTOs) {
        user.getAddresses().clear();

        addressDTOs.forEach(dto -> {
            Address address = new Address();
            address.setId(dto.getId());
            address.setContactName(dto.getContactName());
            address.setContactPhone(dto.getContactPhone());
            address.setCity(dto.getCity());
            address.setDistrict(dto.getDistrict());
            address.setWard(dto.getWard());
            address.setStreet(dto.getStreet());
            address.setNote(dto.getNote());
            address.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault());
            address.setUser(user);
            user.getAddresses().add(address);
        });
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAvatarUrl(),
                user.getDob(),
                user.getGender(),
                user.getAddresses().stream()
                        .map(this::convertAddressToDTO)
                        .collect(Collectors.toList()),
                user.getIsActive()
        );
    }

    private AddressDTO convertAddressToDTO(Address address) {
        return new AddressDTO(
                address.getId(),
                address.getContactName(),
                address.getContactPhone(),
                address.getCity(),
                address.getDistrict(),
                address.getWard(),
                address.getStreet(),
                address.getNote(),
                address.getIsDefault()
        );
    }
}