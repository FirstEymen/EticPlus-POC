package com.EticPlus_POC.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.EticPlus_POC.dto.UserUpdateRequest;
import com.EticPlus_POC.models.*;
import com.EticPlus_POC.repository.ActionLogRepository;
import com.EticPlus_POC.repository.UserRepository;
import com.EticPlus_POC.utility.AuthenticationRequest;
import com.EticPlus_POC.utility.JwtUtil;
import com.EticPlus_POC.utility.UserRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private StoreCategoryService storeCategoryService;

    @Mock
    private ActionLogRepository actionLogRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setStoreName("StoreName");
        request.setPassword("P4ssw0rd");
        request.setCategory("Category");
        request.setPackageType(User.PackageType.SILVER);

        StoreCategory category = new StoreCategory("Category", "Description");
        User user = new User("StoreName", category, "encodedPassword", User.PackageType.SILVER);

        when(storeCategoryService.findByName("Category")).thenReturn(category);
        when(userRepository.findByStoreName("StoreName")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("P4ssw0rd")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registeredUser = userService.registerUser(request);
        assertNotNull(registeredUser);
        assertEquals("StoreName", registeredUser.getStoreName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testAuthenticateUser_Success() {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setStoreName("StoreName");
        authRequest.setPassword("P@ssw0rd");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getPassword()).thenReturn("encodedPassword");
        when(userDetailsService.loadUserByUsername("StoreName")).thenReturn(userDetails);
        when(passwordEncoder.matches("P@ssw0rd", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwtToken");

        String token = userService.authenticateUser(authRequest);

        assertEquals("jwtToken", token);
    }

    @Test
    void testUpdateUserProfile_PasswordChange_Success() {
        User user = new User("StoreName", new StoreCategory("Category","Description"), "encodedOldPassword", User.PackageType.SILVER);
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setCurrentPassword("oldPassword");
        updateRequest.setNewPassword("NewP4ssw0rd");
        updateRequest.setConfirmNewPassword("NewP4ssw0rd");

        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewP4ssw0rd")).thenReturn("encodedNewPassword");
        when(userRepository.save(user)).thenReturn(user);
        boolean updated = userService.updateUserProfile(user, updateRequest);
        assertTrue(updated);
        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void testDeleteAccount_Success() {
        String userId = "userId";
        User user = new User("StoreName", new StoreCategory("Category","Description"), "encodedPassword", User.PackageType.SILVER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        boolean result = userService.deleteAccount(userId);
        assertTrue(result);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void testTogglePlugin_Success() {
        User user = new User("StoreName", new StoreCategory("Category","Description"), "encodedPassword", User.PackageType.SILVER);
        Plugin plugin = new Plugin("PluginName", false);
        user.getPlugins().add(plugin);
        when(userRepository.save(user)).thenReturn(user);
        userService.togglePlugin(user, "PluginName");
        assertTrue(plugin.isActive());
        verify(userRepository).save(user);
    }
}
