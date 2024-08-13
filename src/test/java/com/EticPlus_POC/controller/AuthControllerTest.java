package com.EticPlus_POC.controller;

import com.EticPlus_POC.exception.BusinessException;
import com.EticPlus_POC.models.Plugin;
import com.EticPlus_POC.models.StoreCategory;
import com.EticPlus_POC.models.User;
import com.EticPlus_POC.service.StoreCategoryService;
import com.EticPlus_POC.service.UserService;
import com.EticPlus_POC.utility.AuthenticationRequest;
import com.EticPlus_POC.utility.AuthenticationResponse;
import com.EticPlus_POC.utility.JwtUtil;
import com.EticPlus_POC.utility.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private StoreCategoryService storeCategoryService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testRegisterUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        User mockUser = new User();
        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(mockUser);
        ResponseEntity<?> response = authController.registerUser(request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockUser, response.getBody());
    }

    @Test
    void testCreateAuthenticationToken_Success() {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        when(userService.authenticateUser(any(AuthenticationRequest.class))).thenReturn("jwt-token");
        ResponseEntity<?> response = authController.createAuthenticationToken(authRequest);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", ((AuthenticationResponse) response.getBody()).getJwt());
    }

    @Test
    void testCreateAuthenticationToken_UserNotFound() {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        when(userService.authenticateUser(any(AuthenticationRequest.class))).thenThrow(new UsernameNotFoundException("User not found"));

        Exception exception = assertThrows(BusinessException.class, () -> {
            authController.createAuthenticationToken(authRequest);
        });
        assertEquals("USER_NOT_FOUND", ((BusinessException) exception).getErrorCode());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetHomePage() {
        User user = new User();
        user.setPackageType(User.PackageType.SILVER);
        List<Plugin> pluginList = new ArrayList<>();
        pluginList.add(new Plugin("Plugin1", true));
        pluginList.add(new Plugin("Daily Sales Report", false));
        pluginList.add(new Plugin("Google Analytics", false));
        pluginList.add(new Plugin("Chat-mate", false));
        pluginList.add(new Plugin("ReviewMe", false));
        pluginList.add(new Plugin("GiftSend", false));
        user.getPlugins().clear();
        user.getPlugins().addAll(pluginList);
        when(userService.getUserFromToken(anyString())).thenReturn(user);
        ResponseEntity<?> response = authController.getHomePage("Bearer token");
        assertEquals(200, response.getStatusCodeValue());
        List<Plugin> plugins = (List<Plugin>) response.getBody();
        assertNotNull(plugins);
        assertEquals(6, plugins.size());
        assertEquals("Plugin1", plugins.get(0).getName());
        assertTrue(plugins.get(0).isActive());
    }

    @Test
    void testTogglePlugin() {
        User user = new User();
        when(userService.getUserFromToken(anyString())).thenReturn(user);
        ResponseEntity<?> response = authController.togglePlugin("Bearer token", "Plugin1");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Plugin status updated.", response.getBody());
        verify(userService, times(1)).togglePlugin(user, "Plugin1");
    }

    @Test
    void testGetAllCategories() {
        List<StoreCategory> categories = Collections.singletonList(new StoreCategory("Category1", "Category1"));
        when(storeCategoryService.getAllCategories()).thenReturn(categories);
        ResponseEntity<List<StoreCategory>> response = authController.getAllCategories();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Category1", response.getBody().get(0).getName());
    }
}
