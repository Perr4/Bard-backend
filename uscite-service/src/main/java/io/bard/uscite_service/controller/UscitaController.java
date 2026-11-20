package io.bard.uscite_service.controller;

import io.bard.uscite_service.dto.UscitaDto;
import io.bard.uscite_service.service.UscitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api")
@Tag(name = "Uscite", description = "Group outings management operations")
@SecurityRequirement(name = "BearerAuth")
public class UscitaController {

    private final UscitaService service;

    public UscitaController(UscitaService service) {
        this.service = service;
    }

    // C *
    @Operation(summary = "Create an uscita",
            description = "Records a group outing with its participants.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uscita created",
                    content = @Content(schema = @Schema(implementation = UscitaDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    @PostMapping("/uscite")
    public ResponseEntity<UscitaDto> saveNewUscita(@Valid @RequestBody UscitaDto create) {
        return ResponseEntity.ok(service.create(create));
    }

    /**
     * «Tutti i ballerini escono assieme»: la uscita e' creata con tutti i ballerini
     * della tabella, ciascuno con tutti i numeri.
     */
    @Operation(summary = "Create an uscita with every ballerino",
            description = "Creates the outing and signs up all ballerini.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uscita created with all ballerini",
                    content = @Content(schema = @Schema(implementation = UscitaDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    @PostMapping("/uscite/tutte")
    public ResponseEntity<UscitaDto> saveUscitaPerTutti(@Valid @RequestBody UscitaDto create) {
        return ResponseEntity.ok(service.createConTuttiIPartner(create));
    }

    // R *
    @Operation(summary = "Get all uscite", description = "Retrieves every outing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of uscite",
                    content = @Content(schema = @Schema(implementation = UscitaDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    @GetMapping("/uscite")
    public ResponseEntity<List<UscitaDto>> getAllUscite() {
        return ResponseEntity.ok(service.getAll());
    }

    // R (top 5) *
    @Operation(summary = "Get the top 5 uscite by participants",
            description = "Used by the dashboard ranking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top 5 uscite",
                    content = @Content(schema = @Schema(implementation = UscitaDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    @GetMapping("/uscite/top5")
    public ResponseEntity<List<UscitaDto>> getTop5() {
        return ResponseEntity.ok(service.getTop5());
    }

    // R by id *
    @Operation(summary = "Get an uscita by id", description = "Retrieves one outing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The uscita",
                    content = @Content(schema = @Schema(implementation = UscitaDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Uscita not found")
    })
    @GetMapping("/uscite/{id}")
    public ResponseEntity<UscitaDto> getUscitaById(
            @Parameter(description = "UUID of the uscita", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // D *
    @Operation(summary = "Delete an uscita", description = "Deletes an outing by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uscita deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Uscita not found")
    })
    @DeleteMapping("/uscite/{id}")
    public ResponseEntity<Void> deleteUscita(
            @Parameter(description = "UUID of the uscita to delete", required = true)
            @PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
