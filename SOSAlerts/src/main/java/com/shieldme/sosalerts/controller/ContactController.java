package com.shieldme.sosalerts.controller;

import com.shieldme.sosalerts.exception.InvalidContactException;
import com.shieldme.sosalerts.dto.ContactDTO;
import com.shieldme.sosalerts.dto.UserContactList;
import com.shieldme.sosalerts.service.ContactService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sos")
@CrossOrigin("*")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/add-contacts")
    public ResponseEntity<String> addContacts(@RequestBody @Validated ContactDTO contactDTO) {
        try {
            System.out.println(contactDTO.toString());
            if (contactDTO.getUserId() == null) {
                throw new InvalidContactException("User ID is required to add contact.");
            }
            contactService.saveContact(contactDTO);
            return ResponseEntity.ok("Contact saved successfully!");
        } catch (InvalidContactException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save contact: " + e.getMessage());
        }
    }

    @GetMapping("/get-contacts/{userId}")
    public ResponseEntity<?> getContacts(@PathVariable ObjectId userId) {
        try {
            if (userId == null || userId.toString().isEmpty()) {
                throw new InvalidContactException("User ID is required to fetch contacts.");
            }
            UserContactList contactDetails = contactService.getContactDetails(userId);
            return ResponseEntity.ok(contactDetails);
        } catch (InvalidContactException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch contacts: " + e.getMessage());
        }
    }


    @DeleteMapping("/delete-contact/{userId}")
    public ResponseEntity<String> deleteContact(
            @PathVariable ObjectId userId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) {
        if (userId == null || userId.toString().isEmpty()) {
            throw new InvalidContactException("User ID is required to delete contact.");
        }
        if ((email == null || email.isBlank()) && (phoneNumber == null || phoneNumber.isBlank())) {
            throw new InvalidContactException("Either email or phone number must be provided for deletion.");
        }

        boolean isDeleted = contactService.deleteContact(userId, email, phoneNumber);
        if (isDeleted) {
            return ResponseEntity.ok("Contact deleted successfully!");
        } else {
            return ResponseEntity.status(404).body("Contact not found.");
        }
    }
}

