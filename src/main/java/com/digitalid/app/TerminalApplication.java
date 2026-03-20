package com.digitalid.app;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import com.digitalid.audit.AuditService;
import com.digitalid.domain.AttributeRule;
import com.digitalid.domain.DigitalID;
import com.digitalid.domain.OrganisationType;
import com.digitalid.domain.StatusTransitionValidator;
import com.digitalid.repository.IdentityRepository;
import com.digitalid.repository.InMemoryIdentityRepository;
import com.digitalid.service.AuthorisationService;
import com.digitalid.service.IdentityService;
import com.digitalid.service.VerificationService;
import com.digitalid.util.IdGenerator;

public class TerminalApplication {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final IdentityService identityService;
    private final VerificationService verificationService;
    private final AuditService auditService;
    private final AuthorisationService authService;
    private final Scanner scanner;
    private OrganisationType currentOrganisation;

    public TerminalApplication() {
        IdentityRepository repository = new InMemoryIdentityRepository();
        this.auditService = new AuditService();
        this.authService = new AuthorisationService();
        StatusTransitionValidator transitionValidator = new StatusTransitionValidator();
        AttributeRule attributeRule = new AttributeRule();
        IdGenerator idGenerator = new IdGenerator();

        this.identityService = new IdentityService(repository, auditService, authService,
                transitionValidator, attributeRule, idGenerator);
        this.verificationService = new VerificationService(repository, auditService);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("========================================");
        System.out.println("        Digital ID Platform");
        System.out.println("========================================");

        selectOrganisation();

        boolean running = true;
        while (running) {
            if (authService.canModifyIdentity(currentOrganisation)) {
                running = showCentralAuthorityMenu();
            } else {
                running = showConsumingOrganisationMenu();
            }
        }

        System.out.println("Goodbye.");
        scanner.close();
    }

    private void selectOrganisation() {
        System.out.println("\nSelect your organisation:");
        System.out.println("  1. Central Authority");
        System.out.println("  2. Tax Authority");
        System.out.println("  3. Driving Authority");
        System.out.println("  4. Bank");
        System.out.println("  5. Employer");
        System.out.print("\nChoice: ");

        int choice = readInt();
        switch (choice) {
            case 1 -> currentOrganisation = OrganisationType.CENTRAL_AUTHORITY;
            case 2 -> currentOrganisation = OrganisationType.TAX_AUTHORITY;
            case 3 -> currentOrganisation = OrganisationType.DRIVING_AUTHORITY;
            case 4 -> currentOrganisation = OrganisationType.BANK;
            case 5 -> currentOrganisation = OrganisationType.EMPLOYER;
            default -> {
                System.out.println("Invalid choice. Defaulting to Employer.");
                currentOrganisation = OrganisationType.EMPLOYER;
            }
        }
        System.out.println("\nLogged in as: " + currentOrganisation);
    }

    private boolean showCentralAuthorityMenu() {
        System.out.println("\n========================================");
        System.out.println("        " + formatOrgName(currentOrganisation) + " Menu");
        System.out.println("========================================");
        System.out.println("  1. Create Identity");
        System.out.println("  2. Verify Identity");
        System.out.println("  3. Switch Organisation");
        System.out.println("  4. Exit");
        System.out.print("\nChoice: ");

        int choice = readInt();
        switch (choice) {
            case 1 -> handleCreateIdentity();
            case 2 -> handleVerifyIdentity();
            case 3 -> selectOrganisation();
            case 4 -> { return false; }
            default -> System.out.println("Invalid choice.");
        }
        return true;
    }

    private boolean showConsumingOrganisationMenu() {
        System.out.println("\n========================================");
        System.out.println("        " + formatOrgName(currentOrganisation) + " Menu");
        System.out.println("========================================");
        System.out.println("  1. Verify Identity");
        System.out.println("  2. Switch Organisation");
        System.out.println("  3. Exit");
        System.out.print("\nChoice: ");

        int choice = readInt();
        switch (choice) {
            case 1 -> handleVerifyIdentity();
            case 2 -> selectOrganisation();
            case 3 -> { return false; }
            default -> System.out.println("Invalid choice.");
        }
        return true;
    }

    private void handleCreateIdentity() {
        System.out.println("\n--- Create New Identity ---\n");
        String firstName = readNonEmpty("Enter first name: ");
        String surname = readNonEmpty("Enter surname: ");
        String gender = readNonEmpty("Enter gender (Male/Female/Other): ");
        String dob = readValidDob("Enter date of birth (DD-MM-YYYY): ");
        String nationality = readNonEmpty("Enter nationality (e.g. British, Irish, French): ");

        while (true) {
            System.out.println("\n--- Review Identity ---\n");
            System.out.println("  1. First Name:  " + firstName);
            System.out.println("  2. Surname:     " + surname);
            System.out.println("  3. Gender:      " + gender);
            System.out.println("  4. DOB:         " + dob);
            System.out.println("  5. Nationality: " + nationality);
            System.out.print("\n(S)ubmit / (E)dit / (C)ancel: ");
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "S" -> {
                    try {
                        DigitalID identity = identityService.createIdentity(firstName, surname, gender, dob, nationality, currentOrganisation);
                        System.out.println("\nSUCCESS: Identity created");
                        System.out.println("  ID:          " + identity.getId());
                        System.out.println("  Name:        " + identity.getFullName());
                        System.out.println("  Gender:      " + identity.getGender());
                        System.out.println("  DOB:         " + identity.getDateOfBirth().format(DATE_FORMAT));
                        System.out.println("  Nationality: " + identity.getNationality());
                        System.out.println("  Status:      " + identity.getStatus());
                        System.out.println("  Created:     " + identity.getCreatedDate().format(DATE_FORMAT));
                    } catch (Exception e) {
                        System.out.println("\nERROR: " + e.getMessage());
                    }
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                    return;
                }
                case "E" -> {
                    System.out.print("Enter field number to edit (1-5): ");
                    int field = readInt();
                    switch (field) {
                        case 1 -> firstName = readNonEmpty("Enter first name: ");
                        case 2 -> surname = readNonEmpty("Enter surname: ");
                        case 3 -> gender = readNonEmpty("Enter gender (Male/Female/Other): ");
                        case 4 -> dob = readValidDob("Enter date of birth (DD-MM-YYYY): ");
                        case 5 -> nationality = readNonEmpty("Enter nationality (e.g. British, Irish, French): ");
                        default -> System.out.println("Invalid field number.");
                    }
                }
                case "C" -> {
                    System.out.println("\nIdentity creation cancelled.");
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter S, E, or C.");
            }
        }
    }

    private void handleVerifyIdentity() {
        System.out.println("\n--- Verify Identity ---\n");
        System.out.print("Enter Digital ID: ");
        String id = scanner.nextLine();

        try {
            if (currentOrganisation == OrganisationType.CENTRAL_AUTHORITY) {
                DigitalID identity = identityService.findIdentity(id);
                System.out.println("\nIdentity Details:");
                System.out.println("  ID:          " + identity.getId());
                System.out.println("  Name:        " + identity.getFullName());
                System.out.println("  Gender:      " + identity.getGender());
                System.out.println("  DOB:         " + identity.getDateOfBirth().format(DATE_FORMAT));
                System.out.println("  Nationality: " + identity.getNationality());
                System.out.println("  Address:     " + identity.getAddress());
                System.out.println("  Status:      " + identity.getStatus());
                System.out.println("  Restricted:  " + (identity.isRestricted() ? "Yes" : "No"));
                System.out.println("  Created:     " + identity.getCreatedDate().format(DATE_FORMAT));
            } else {
                com.digitalid.verification.VerificationResult result =
                    verificationService.verifyIdentity(id, currentOrganisation);
                System.out.println("\nVerifying as: " + formatOrgName(currentOrganisation));
                System.out.println("Result: " + (result.isValid() ? "VALID" : "INVALID"));
                System.out.println("Reason: " + result.getReason());
            }
        } catch (Exception e) {
            System.out.println("\nERROR: " + e.getMessage());
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private int readInt() {
        try {
            String line = scanner.nextLine();
            return Integer.parseInt(line.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("ERROR: This field cannot be empty.");
        }
    }

    private String readValidDob(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                LocalDate dob = LocalDate.parse(input, DATE_FORMAT);
                if (dob.isAfter(LocalDate.now())) {
                    System.out.println("ERROR: Date of birth cannot be in the future.");
                } else if (dob.isBefore(LocalDate.of(1900, 1, 1))) {
                    System.out.println("ERROR: Date of birth cannot be before 01-01-1900.");
                } else {
                    return input;
                }
            } catch (DateTimeParseException e) {
                System.out.println("ERROR: Invalid date format. Please use DD-MM-YYYY.");
            }
        }
    }

    private String formatOrgName(OrganisationType org) {
        String name = org.name().replace("_", " ");
        String[] words = name.toLowerCase().split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1))
                  .append(" ");
        }
        return result.toString().trim();
    }

    public static void main(String[] args) {
        TerminalApplication app = new TerminalApplication();
        app.start();
    }
}