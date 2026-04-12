package com.digitalid.app;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import com.digitalid.audit.AuditEventType;
import com.digitalid.audit.AuditLogEntry;
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

    public void start(boolean demoMode) {
        if (demoMode) {
            loadSampleData();
            System.out.println("  [Demo mode: sample identities loaded]\n");
        }
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
        System.out.println("  2. Update Identity");
        System.out.println("  3. Change Status");
        System.out.println("  4. Set/Remove Restriction");
        System.out.println("  5. Verify Identity");
        System.out.println("  6. Find Identity");
        System.out.println("  7. View Audit Logs");
        System.out.println("  8. Switch Organisation");
        System.out.println("  9. Exit");
        System.out.print("\nChoice: ");

        int choice = readInt();
        switch (choice) {
            case 1 -> handleCreateIdentity();
            case 2 -> handleUpdateIdentity();
            case 3 -> handleChangeStatus();
            case 4 -> handleSetRestriction();
            case 5 -> handleVerifyIdentity();
            case 6 -> handleFindIdentity();
            case 7 -> handleViewAuditLogs();
            case 8 -> selectOrganisation();
            case 9 -> { return false; }
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
        String firstName = readName("Enter first name: ");
        String surname = readName("Enter surname: ");
        String gender = readValidGender("Enter gender (M)ale / (F)emale / (X) Other: ");
        String dob = readValidDob("Enter date of birth (DD-MM-YYYY): ");
        String nationality = readNonEmpty("Enter nationality (e.g. British, Irish, French): ");
        nationality = capitalise(nationality);
        System.out.print("Enter address (or press Enter to skip): ");
        String address = scanner.nextLine().trim();
        String postcode = "";
        if (!address.isEmpty()) {
            postcode = readValidPostcode("Enter postcode: ");
        }

        while (true) {
            System.out.println("\n--- Review Identity ---\n");
            System.out.println("  1. First Name:  " + firstName);
            System.out.println("  2. Surname:     " + surname);
            System.out.println("  3. Gender:      " + gender);
            System.out.println("  4. DOB:         " + dob);
            System.out.println("  5. Nationality: " + nationality);
            System.out.println("  6. Address:     " + (address.isEmpty() ? "(none)" : address));
            System.out.println("  7. Postcode:    " + (postcode.isEmpty() ? "(none)" : postcode));
            System.out.print("\n(S)ubmit / (E)dit / (C)ancel: ");
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "S" -> {
                    if (!address.isEmpty() && postcode.isEmpty()) {
                        System.out.println("ERROR: Postcode is required when an address is provided. Please edit field 7.");
                        continue;
                    }
                    List<DigitalID> duplicates = identityService.findIdentity(
                            firstName + " " + surname, LocalDate.parse(dob, DATE_FORMAT));
                    if (!duplicates.isEmpty()) {
                        System.out.println("\nWARNING: An identity with this name and date of birth already exists:");
                        for (DigitalID dup : duplicates) {
                            System.out.println("  " + dup.getId() + " | " + dup.getFullName() + " | " + dup.getStatus());
                        }
                        System.out.print("Proceed with creation? (Y/N): ");
                        String confirm = scanner.nextLine().trim().toUpperCase();
                        if (!confirm.equals("Y") && !confirm.equals("YES")) {
                            System.out.println("\nIdentity creation cancelled.");
                            System.out.println("\nPress Enter to continue...");
                            scanner.nextLine();
                            return;
                        }
                    }
                    try {
                        DigitalID identity = identityService.createIdentity(firstName, surname, gender, dob, nationality, address, postcode, currentOrganisation);
                        System.out.println("\nSUCCESS: Identity created");
                        System.out.println("  ID:          " + identity.getId());
                        System.out.println("  Name:        " + identity.getFullName());
                        System.out.println("  Gender:      " + identity.getGender());
                        System.out.println("  DOB:         " + identity.getDateOfBirth().format(DATE_FORMAT));
                        System.out.println("  Nationality: " + identity.getNationality());
                        System.out.println("  Address:     " + (identity.getAddress().isEmpty() ? "(none)" : identity.getAddress()));
                        System.out.println("  Postcode:    " + (identity.getPostcode().isEmpty() ? "(none)" : identity.getPostcode()));
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
                    System.out.print("Enter field number to edit (1-7): ");
                    int field = readInt();
                    switch (field) {
                        case 1 -> firstName = readName("Enter first name: ");
                        case 2 -> surname = readName("Enter surname: ");
                        case 3 -> gender = readValidGender("Enter gender (M)ale / (F)emale / (X) Other: ");
                        case 4 -> dob = readValidDob("Enter date of birth (DD-MM-YYYY): ");
                        case 5 -> {
                            nationality = readNonEmpty("Enter nationality (e.g. British, Irish, French): ");
                            nationality = capitalise(nationality);
                        }
                        case 6 -> {
                            System.out.print("Enter address (or press Enter to clear): ");
                            address = scanner.nextLine().trim();
                            if (address.isEmpty()) {
                                postcode = "";
                            } else if (postcode.isEmpty()) {
                                postcode = readValidPostcode("Enter postcode: ");
                            }
                        }
                        case 7 -> {
                            if (address.isEmpty()) {
                                System.out.println("ERROR: Cannot set postcode without an address.");
                            } else {
                                postcode = readValidPostcode("Enter postcode: ");
                            }
                        }
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


    private void handleUpdateIdentity() {
        System.out.println("\n--- Update Identity ---\n");
        showRegisteredIdentities();
        System.out.print("Enter Digital ID (or 0 to cancel): ");
        String id = scanner.nextLine().trim();
        if (id.equals("0") || id.isEmpty()) return;

        try {
            DigitalID identity = identityService.findIdentity(id);
            System.out.println("\nCurrent Details:");
            System.out.println("  Name:        " + identity.getFullName());
            System.out.println("  Nationality: " + identity.getNationality());
            System.out.println("  Address:     " + (identity.getAddress().isEmpty() ? "(none)" : identity.getAddress()));
            System.out.println("  Postcode:    " + (identity.getPostcode().isEmpty() ? "(none)" : identity.getPostcode()));

            System.out.println("\nWhat would you like to update?");
            System.out.println("  1. Nationality");
            System.out.println("  2. Address");
            System.out.println("  3. Postcode");
            System.out.print("\nChoice: ");
            int field = readInt();

            switch (field) {
                case 1 -> {
                    String nationality = readNonEmpty("Enter new nationality: ");
                    nationality = capitalise(nationality);
                    identityService.updateNationality(id, nationality, currentOrganisation);
                    System.out.println("\nSUCCESS: Nationality updated to '" + nationality + "'");
                }
                case 2 -> {
                    String address = readNonEmpty("Enter new address: ");
                    identityService.updateAddress(id, address, currentOrganisation);
                    System.out.println("\nSUCCESS: Address updated to '" + address + "'");
                }
                case 3 -> {
                    String postcode = readNonEmpty("Enter new postcode: ");
                    identityService.updatePostcode(id, postcode, currentOrganisation);
                    System.out.println("\nSUCCESS: Postcode updated to '" + postcode + "'");
                }
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("\nERROR: " + e.getMessage());
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void handleChangeStatus() {
        System.out.println("\n--- Change Identity Status ---\n");
        showRegisteredIdentities();
        System.out.print("Enter Digital ID (or 0 to cancel): ");
        String id = scanner.nextLine().trim();
        if (id.equals("0") || id.isEmpty()) return;

        try {
            DigitalID identity = identityService.findIdentity(id);
            System.out.println("\n  Name:   " + identity.getFullName());
            System.out.println("  Status: " + identity.getStatus());

            if (identity.isRevoked()) {
                System.out.println("\nThis identity has been revoked and cannot be changed.");
            } else {
                System.out.println("\nAvailable actions:");
                int option = 1;
                if (identity.isActive()) {
                    System.out.println("  " + option++ + ". Suspend");
                    System.out.println("  " + option++ + ". Revoke");
                } else if (identity.isSuspended()) {
                    System.out.println("  " + option++ + ". Activate");
                    System.out.println("  " + option++ + ". Revoke");
                }
                System.out.print("\nChoice: ");
                int choice = readInt();

                if (identity.isActive()) {
                    switch (choice) {
                        case 1 -> { identityService.suspendIdentity(id, currentOrganisation); System.out.println("\nSUCCESS: Identity suspended."); }
                        case 2 -> { identityService.revokeIdentity(id, currentOrganisation); System.out.println("\nSUCCESS: Identity revoked."); }
                        default -> System.out.println("Invalid choice.");
                    }
                } else if (identity.isSuspended()) {
                    switch (choice) {
                        case 1 -> { identityService.activateIdentity(id, currentOrganisation); System.out.println("\nSUCCESS: Identity activated."); }
                        case 2 -> { identityService.revokeIdentity(id, currentOrganisation); System.out.println("\nSUCCESS: Identity revoked."); }
                        default -> System.out.println("Invalid choice.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("\nERROR: " + e.getMessage());
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void handleSetRestriction() {
        System.out.println("\n--- Set Identity Restriction ---\n");
        showRegisteredIdentities();
        System.out.print("Enter Digital ID (or 0 to cancel): ");
        String id = scanner.nextLine().trim();
        if (id.equals("0") || id.isEmpty()) return;

        try {
            DigitalID identity = identityService.findIdentity(id);
            System.out.println("Current restriction status: " + (identity.isRestricted() ? "RESTRICTED" : "NONE"));
            System.out.print("Apply Restriction? (Y)es/(N)o: ");
            String input = scanner.nextLine().trim().toLowerCase();
            boolean restricted = input.equals("yes") || input.equals("y");

            if (restricted == identity.isRestricted()) {
                System.out.println("\nNo change - restriction is already " + (restricted ? "applied." : "removed."));
            } else {
                identityService.setRestriction(id, restricted, currentOrganisation);
                System.out.println(restricted ? "\nRestriction applied." : "\nRestriction removed.");
            }
        } catch (Exception e) {
            System.out.println("\nERROR: " + e.getMessage());
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void handleVerifyIdentity() {
        System.out.println("\n--- Verify Identity ---\n");
        if (currentOrganisation == OrganisationType.CENTRAL_AUTHORITY) {
            showRegisteredIdentities();
        }
        System.out.print("Enter Digital ID (or 0 to cancel): ");
        String id = scanner.nextLine().trim();
        if (id.equals("0") || id.isEmpty()) return;

        try {
            if (currentOrganisation == OrganisationType.CENTRAL_AUTHORITY) {
                DigitalID identity = identityService.findIdentity(id);
                System.out.println("\nIdentity Details:");
                System.out.println("  ID:          " + identity.getId());
                System.out.println("  Name:        " + identity.getFullName());
                System.out.println("  Gender:      " + identity.getGender());
                System.out.println("  DOB:         " + identity.getDateOfBirth().format(DATE_FORMAT));
                System.out.println("  Nationality: " + identity.getNationality());
                System.out.println("  Address:     " + (identity.getAddress().isEmpty() ? "(none)" : identity.getAddress()));
                System.out.println("  Postcode:    " + (identity.getPostcode().isEmpty() ? "(none)" : identity.getPostcode()));
                System.out.println("  Status:      " + identity.getStatus());
                System.out.println("  Restricted:  " + (identity.isRestricted() ? "Yes" : "No"));
                System.out.println("  Created:     " + identity.getCreatedDate().format(DATE_FORMAT));
                auditService.recordEvent(AuditEventType.VERIFICATION_REQUESTED, id, currentOrganisation, "FULL_DETAILS_VIEWED");
            } else {
                com.digitalid.verification.VerificationResult result;
                if (currentOrganisation == OrganisationType.TAX_AUTHORITY) {
                    System.out.println("Select reporting period:");
                    System.out.println("  1. Last 30 days");
                    System.out.println("  2. Last quarter");
                    System.out.println("  3. Last year");
                    System.out.println("  4. Custom range");
                    System.out.print("\nChoice: ");
                    int periodChoice = readInt();

                    LocalDate from;
                    LocalDate to = LocalDate.now();
                    switch (periodChoice) {
                        case 1 -> from = to.minusDays(30);
                        case 2 -> from = to.minusMonths(3);
                        case 3 -> from = to.minusYears(1);
                        case 4 -> {
                            String fromDate = readValidDob("Enter start date (dd-MM-yyyy): ");
                            from = LocalDate.parse(fromDate, DATE_FORMAT);
                            String toDate = readValidDob("Enter end date (dd-MM-yyyy): ");
                            to = LocalDate.parse(toDate, DATE_FORMAT);
                            while (!to.isAfter(from)) {
                                System.out.println("ERROR: End date must be after start date.");
                                toDate = readValidDob("Enter end date (dd-MM-yyyy): ");
                                to = LocalDate.parse(toDate, DATE_FORMAT);
                            }
                        }
                        default -> {
                            System.out.println("Invalid choice. Using last 30 days.");
                            from = to.minusDays(30);
                        }
                    }
                    System.out.println("\nChecking period: " + from.format(DATE_FORMAT) + " to " + to.format(DATE_FORMAT));
                    result = verificationService.verifyIdentityWithPeriod(id, currentOrganisation, from, to);
                } else {
                    result = verificationService.verifyIdentity(id, currentOrganisation);
                }
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

    private void handleFindIdentity() {
        System.out.println("\n--- Find Identity ---\n");
        System.out.print("Enter full name (or 0 to cancel): ");
        String name = scanner.nextLine().trim();
        if (name.equals("0") || name.isEmpty()) return;
        System.out.print("Enter date of birth (dd-MM-yyyy): ");
        String dobInput = scanner.nextLine().trim();

        try {
            LocalDate dob = LocalDate.parse(dobInput, DATE_FORMAT);
            List<DigitalID> results = identityService.findIdentity(name, dob);

            if (results.isEmpty()) {
                System.out.println("\nNo identities found.");
            } else {
                System.out.println("\nFound " + results.size() + " result(s):");
                for (DigitalID identity : results) {
                    System.out.println("  ID: " + identity.getId() + " | Name: " + identity.getFullName() + " | Status: " + identity.getStatus());
                }
            }
        } catch (Exception e) {
            System.out.println("\nERROR: " + e.getMessage());
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void handleViewAuditLogs() {
        System.out.println("\n--- Audit Log ---\n");

        List<AuditLogEntry> logs = auditService.getAuditLogs();

        if (logs.isEmpty()) {
            System.out.println("No audit events recorded.");
        } else {
            for (AuditLogEntry entry : logs) {
                System.out.println("  [" + entry.getTimestamp().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "] "
                        + entry.getOperation() + " | ID: " + entry.getIdentityId()
                        + " | Org: " + entry.getOrganisation() + " | " + entry.getResult());
            }
            System.out.println("\nTotal events: " + logs.size());
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

    private void showRegisteredIdentities() {
        List<DigitalID> identities = identityService.getAllIdentities();
        if (identities.isEmpty()) {
            System.out.println("No identities registered.\n");
        } else {
            System.out.println("Registered identities:");
            for (DigitalID id : identities) {
                System.out.println("  " + id.getId() + " | " + id.getFullName() + " | " + id.getStatus() + (id.isRestricted() ? " | RESTRICTED" : ""));
            }
            System.out.println();
        }
    }

    private String readName(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("ERROR: This field cannot be empty.");
            } else if (!input.matches("[a-zA-Z\\-' ]+")) {
                System.out.println("ERROR: Name must contain only letters, hyphens or apostrophes.");
            } else {
                return capitalise(input);
            }
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

    private String readValidGender(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();
            switch (input) {
                case "M", "MALE" -> { return "Male"; }
                case "F", "FEMALE" -> { return "Female"; }
                case "X", "OTHER" -> { return "Other"; }
                default -> System.out.println("ERROR: Please enter Male/M, Female/F, or Other/X.");
            }
        }
    }

    private String readValidPostcode(String prompt) {
        while (true) {
            String input = readNonEmpty(prompt).replaceAll("\\s+", "").toUpperCase();
            if (input.matches("^[A-Z]{1,2}[0-9][0-9A-Z]?[0-9][A-Z]{2}$")) {
                return input.substring(0, input.length() - 3) + " " + input.substring(input.length() - 3);
            }
            System.out.println("ERROR: Invalid UK postcode format (e.g. E1 7AA, CB1 3AB).");
        }
    }

    private String capitalise(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
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

    private void loadSampleData() {
        OrganisationType ca = OrganisationType.CENTRAL_AUTHORITY;
        identityService.createIdentity("Sarah", "George", "Female", "15-03-1992", "British", "42 Park Lane", "SW1A 2AA", ca);
        DigitalID james = identityService.createIdentity("James", "O'Brien", "Male", "28-11-1985", "Irish", "7 Queen Street", "E1 6AN", ca);
        DigitalID aisha = identityService.createIdentity("Aisha", "Rajan", "Female", "03-07-2001", "British", "15 Mill Road", "CB1 3AB", ca);
        DigitalID tom = identityService.createIdentity("Tom", "Williams", "Male", "12-01-1978", "British", "", "", ca);
        identityService.createIdentity("Alex", "Hollow-Bales", "Other", "22-09-1995", "Spanish", "8 High Street", "N1 9GU", ca);

        // James — suspend then reactivate (creates suspension history)
        identityService.suspendIdentity(james.getId(), ca);
        identityService.activateIdentity(james.getId(), ca);
        // Aisha — set restriction
        identityService.setRestriction(aisha.getId(), true, ca);
        // Tom — revoke
        identityService.revokeIdentity(tom.getId(), ca);
    }

    public static void main(String[] args) {
        TerminalApplication app = new TerminalApplication();
        boolean demoMode = args.length > 0 && args[0].equals("--demo");
        app.start(demoMode);
    }
}
