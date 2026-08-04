package vn.codegyme.meal_choice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.dto.AddressResponseDTO;
import vn.codegyme.meal_choice.dto.CreateAddressDTO;
import vn.codegyme.meal_choice.dto.UpdateAddressDTO;
import vn.codegyme.meal_choice.dto.UpdateProfileDTO;
import vn.codegyme.meal_choice.dto.UserResponseDTO;
import vn.codegyme.meal_choice.entity.Address;
import vn.codegyme.meal_choice.entity.User;
import vn.codegyme.meal_choice.repository.UserRepository;
import vn.codegyme.meal_choice.security.CustomUserDetails;
import vn.codegyme.meal_choice.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserResponseDTO getCurrentUserProfile() {
        User user = getCurrentUser();
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateProfile(UpdateProfileDTO updateProfileDTO) {
        User user = getCurrentUser();

        // CHỈ cập nhật các trường được phép
        user.setDisplayName(updateProfileDTO.getDisplayName());
        user.setDob(updateProfileDTO.getDob());
        user.setGender(updateProfileDTO.getGender());
        user.setAvatarUrl(updateProfileDTO.getAvatarUrl());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    public List<AddressResponseDTO> getAllAddresses() {
        User user = getCurrentUser();
        return user.getAddresses().stream()
                .map(this::convertAddressToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponseDTO getAddressById(Long addressId) {
        User user = getCurrentUser();
        Address address = user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ hoặc địa chỉ không thuộc về bạn"));
        return convertAddressToDTO(address);
    }

    @Override
    @Transactional
    public UserResponseDTO addAddress(CreateAddressDTO createAddressDTO) {
        User user = getCurrentUser();

        Address address = new Address();
        address.setContactName(createAddressDTO.getContactName());
        address.setContactPhone(createAddressDTO.getContactPhone());
        address.setCity(createAddressDTO.getCity());
        address.setDistrict(createAddressDTO.getDistrict());
        address.setWard(createAddressDTO.getWard());
        address.setStreet(createAddressDTO.getStreet());
        address.setNote(createAddressDTO.getNote());
        address.setIsDefault(createAddressDTO.getIsDefault() != null && createAddressDTO.getIsDefault());
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
    public UserResponseDTO updateAddress(Long addressId, UpdateAddressDTO updateAddressDTO) {
        User user = getCurrentUser();

        // Tìm địa chỉ thuộc sở hữu của user hiện tại
        Address address = user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ hoặc địa chỉ không thuộc về bạn"));

        // Cập nhật thông tin địa chỉ (Giữ nguyên liên hệ contactName & contactPhone theo đúng yêu cầu đề bài)
        address.setCity(updateAddressDTO.getCity());
        address.setDistrict(updateAddressDTO.getDistrict());
        address.setWard(updateAddressDTO.getWard());
        address.setStreet(updateAddressDTO.getStreet());
        address.setNote(updateAddressDTO.getNote());

        // Quản lý trạng thái mặc định
        boolean isDefault = updateAddressDTO.getIsDefault() != null && updateAddressDTO.getIsDefault();
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
    public UserResponseDTO deleteAddress(Long addressId) {
        User user = getCurrentUser();
        user.getAddresses().removeIf(address -> address.getId().equals(addressId));
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO setDefaultAddress(Long addressId) {
        User user = getCurrentUser();
        user.getAddresses().forEach(address -> {
            address.setIsDefault(address.getId().equals(addressId));
        });
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    // === Helper Methods ===

    private User getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        UUID userId = userDetails.getId();

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    private UserResponseDTO convertToDTO(User user) {
        return new UserResponseDTO(
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

    private AddressResponseDTO convertAddressToDTO(Address address) {
        return new AddressResponseDTO(
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