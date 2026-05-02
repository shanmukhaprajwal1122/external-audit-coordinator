package com.internship.tool.controller;

import com.internship.tool.entity.Notification;
import com.internship.tool.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notifications", description = "Endpoints for managing user notifications and alerts")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications", description = "Retrieves a paginated list of all system notifications.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/all")
    public ResponseEntity<Page<Notification>> getAllNotifications(Pageable pageable) {
        Page<Notification> notifications = notificationService.getAllNotifications(pageable);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get a notification by ID", description = "Retrieves a specific notification.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved notification")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        Notification notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }

    @Operation(summary = "Get notifications for user", description = "Retrieves a list of all notifications for a specific user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notifications for user", description = "Retrieves only unread notifications for a user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notification count", description = "Returns the number of unread notifications for a user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved count")
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Create a notification", description = "Creates a new notification for a user.")
    @ApiResponse(responseCode = "201", description = "Notification successfully created")
    @PostMapping("/create")
    public ResponseEntity<Notification> createNotification(
            @Valid @RequestBody Notification notification,
            @RequestParam Long recipientId,
            @RequestParam(required = false) Long senderId) {
        Notification createdNotification = notificationService.createNotification(recipientId, senderId, notification);
        return new ResponseEntity<>(createdNotification, HttpStatus.CREATED);
    }

    @Operation(summary = "Mark a notification as read", description = "Marks a specific notification as read.")
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        Notification updatedNotification = notificationService.markAsRead(id);
        return ResponseEntity.ok(updatedNotification);
    }

    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications for a user as read.")
    @ApiResponse(responseCode = "200", description = "Notifications successfully marked")
    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Integer> markAllAsReadForUser(@PathVariable Long userId) {
        int count = notificationService.markAllAsReadForUser(userId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Delete a notification", description = "Deletes a notification.")
    @ApiResponse(responseCode = "204", description = "Notification successfully deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
