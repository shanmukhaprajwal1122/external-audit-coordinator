package com.internship.tool.service;

import com.internship.tool.entity.Notification;
import com.internship.tool.entity.User;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;
    private User recipient;
    private User sender;

    @BeforeEach
    void setUp() {
        recipient = new User();
        recipient.setId(1L);
        recipient.setEmail("recipient@test.com");

        sender = new User();
        sender.setId(2L);
        sender.setEmail("sender@test.com");

        notification = new Notification();
        notification.setId(100L);
        notification.setSubject("Test Subject");
        notification.setMessage("Test Message");
        notification.setRecipient(recipient);
        notification.setIsRead(false);
    }

    // 1. Happy Path: getNotificationById
    @Test
    void getNotificationById_Success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));

        Notification result = notificationService.getNotificationById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Test Subject", result.getSubject());
        verify(notificationRepository, times(1)).findById(100L);
    }

    // 2. Error Case: getNotificationById
    @Test
    void getNotificationById_NotFound_ThrowsResourceNotFoundException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.getNotificationById(999L);
        });
        verify(notificationRepository, times(1)).findById(999L);
    }

    // 3. Happy Path: getUserNotifications
    @Test
    void getUserNotifications_Success() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(notification));

        List<Notification> result = notificationService.getUserNotifications(1L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        verify(notificationRepository, times(1)).findByRecipientIdOrderByCreatedAtDesc(1L);
    }

    // 4. Happy Path: getAllNotifications (Paginated)
    @Test
    void getAllNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(Arrays.asList(notification));
        
        when(notificationRepository.findAll(pageable)).thenReturn(page);

        Page<Notification> result = notificationService.getAllNotifications(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(notificationRepository, times(1)).findAll(pageable);
    }

    // 5. Happy Path: getUnreadNotifications
    @Test
    void getUnreadNotifications_Success() {
        when(notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(notification));

        List<Notification> result = notificationService.getUnreadNotifications(1L);

        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsRead());
        verify(notificationRepository, times(1)).findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(1L);
    }

    // 6. Happy Path: getUnreadCount
    @Test
    void getUnreadCount_Success() {
        when(notificationRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(5L, count);
        verify(notificationRepository, times(1)).countByRecipientIdAndIsReadFalse(1L);
    }

    // 7. Happy Path: createNotification (with Sender)
    @Test
    void createNotification_WithSender_Success() {
        when(userService.getUserById(1L)).thenReturn(recipient);
        when(userService.getUserById(2L)).thenReturn(sender);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification newNotification = new Notification();
        newNotification.setSubject("New Subject");

        Notification result = notificationService.createNotification(1L, 2L, newNotification);

        assertNotNull(result);
        assertEquals(recipient, newNotification.getRecipient());
        assertEquals(sender, newNotification.getSender());
        verify(userService, times(1)).getUserById(1L);
        verify(userService, times(1)).getUserById(2L);
        verify(notificationRepository, times(1)).save(newNotification);
    }

    // 8. Happy Path: markAsRead (Changes state from unread to read)
    @Test
    void markAsRead_WhenUnread_MarksReadAndSaves() {
        notification.setIsRead(false);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.markAsRead(100L);

        assertTrue(result.getIsRead());
        assertNotNull(result.getReadAt());
        verify(notificationRepository, times(1)).save(notification);
    }

    // 9. Happy Path: markAllAsReadForUser
    @Test
    void markAllAsReadForUser_Success() {
        when(notificationRepository.markAllAsRead(1L)).thenReturn(3); // 3 rows updated

        int rowsUpdated = notificationService.markAllAsReadForUser(1L);

        assertEquals(3, rowsUpdated);
        verify(notificationRepository, times(1)).markAllAsRead(1L);
    }

    // 10. Happy Path: deleteNotification
    @Test
    void deleteNotification_Success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));
        doNothing().when(notificationRepository).delete(notification);

        assertDoesNotThrow(() -> notificationService.deleteNotification(100L));

        verify(notificationRepository, times(1)).findById(100L);
        verify(notificationRepository, times(1)).delete(notification);
    }
}
