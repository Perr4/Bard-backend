package io.bard.presenza_service.controller;

import io.bard.presenza_service.dto.PresenzaDto;
import io.bard.presenza_service.service.PresenzaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api")
@Tag(name = "Presenze", description = "Attendance management operations")
@SecurityRequirement(name = "BearerAuth")
public class PresenzaController {

    private final PresenzaService service;

    public PresenzaController(PresenzaService service) {
        this.service = service;
    }

    // CRUD

    // C *
    @Operation(summary = "Create a new presenza (attendance record)",
            description = "Records attendance for a specific ballerino on a given date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presenza created successfully",
                    content = @Content(schema = @Schema(implementation = PresenzaDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Ballerino not found (if validated)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/presenza")
    public ResponseEntity<PresenzaDto> saveNewPresenza(@Valid @RequestBody PresenzaDto create) {
        System.out.println(create);
        return ResponseEntity.ok(service.createPresenza(create));
    }

    // U
    @Operation(summary = "Update presenza",
            description = "Updates an existing attendance record.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presenza updated successfully",
                    content = @Content(schema = @Schema(implementation = PresenzaDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Presenza not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/presenza")
    public ResponseEntity<PresenzaDto> updatePresenza(@Valid @RequestBody PresenzaDto updateEntity) {
        return ResponseEntity.ok(service.modificaPresenza(updateEntity));
    }

    // R *
    @Operation(summary = "Get all presenze by ballerino ID",
            description = "Retrieves all attendance records for a specific dancer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of presenze for the ballerino",
                    content = @Content(schema = @Schema(implementation = PresenzaDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Ballerino not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/presenza/{id}")
    public ResponseEntity<List<PresenzaDto>> getAllbyBallerinoId(
            @Parameter(description = "UUID of the ballerino", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.getAllbyBallerinoId(id));
    }

    // R (tutte) *
    @Operation(summary = "Get all presenze",
            description = "Retrieves every attendance record.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all presenze",
                    content = @Content(schema = @Schema(implementation = PresenzaDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/presenza")
    public ResponseEntity<List<PresenzaDto>> getAllPresenze() {
        return ResponseEntity.ok(service.getAllPresenze());
    }

    // D *
    @Operation(summary = "Delete presenza",
            description = "Deletes a specific attendance record by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presenza deleted successfully",
                    content = @Content(schema = @Schema(implementation = PresenzaDto.class))),
            @ApiResponse(responseCode = "404", description = "Presenza not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/presenza/{id}")
    public ResponseEntity<PresenzaDto> deletePresenza(
            @Parameter(description = "UUID of the presenza record to delete", required = true)
            @PathVariable("id") UUID uuid) {
        System.out.println(uuid);
        return ResponseEntity.ok(service.deletePresenza(uuid));
    }

}
