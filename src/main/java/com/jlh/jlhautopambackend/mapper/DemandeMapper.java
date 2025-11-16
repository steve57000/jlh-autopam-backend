package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {DemandeTimelineMapper.class, ClientMapper.class},
        builder = @Builder(disableBuilder = true)
)
public interface DemandeMapper {

    // ----------- ENTITY -> DTO -----------

    @Mapping(target = "client", source = "client") // utilisera toClientSummaryDto
    @Mapping(target = "typeDemande", source = "typeDemande")
    @Mapping(target = "statutDemande", source = "statutDemande")
    @Mapping(target = "services", source = "services") // utilisera toDemandeServiceDto
    @Mapping(target = "documents", source = "documents")
    @Mapping(target = "timeline", source = "timelineEntries")
    DemandeResponse toResponse(Demande ent);

    @Mapping(target = "dateSoumission", source = "dateDemande")
    @Mapping(target = "codeType", source = "typeDemande.codeType")
    @Mapping(target = "typeLibelle", source = "typeDemande.libelle")
    @Mapping(target = "codeStatut", source = "statutDemande.codeStatut")
    @Mapping(target = "statutLibelle", source = "statutDemande.libelle")
    @Mapping(target = "client", source = "client")
    @Mapping(target = "services", source = "services")
    DemandeDto toDto(Demande ent);

    List<DemandeDto> toDtos(List<Demande> entities);

    // Client -> ClientSummaryDto
    @Mapping(target = "idClient", source = "idClient")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "prenom", source = "prenom")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "telephone", source = "telephone")
    @Mapping(target = "immatriculation", source = "immatriculation")
    @Mapping(target = "vehiculeMarque", source = "vehiculeMarque")
    @Mapping(target = "vehiculeModele", source = "vehiculeModele")
    @Mapping(target = "adresseLigne1", source = "adresseLigne1")
    @Mapping(target = "adresseLigne2", source = "adresseLigne2")
    @Mapping(target = "adresseCodePostal", source = "adresseCodePostal")
    @Mapping(target = "adresseVille", source = "adresseVille")
    ClientSummaryDto toClientSummaryDto(Client client);

    // DemandeService -> DemandeServiceDto
    @Mapping(target = "idDemande", source = "demande.idDemande")
    @Mapping(target = "idService", source = "id.idService")
    @Mapping(target = "libelle", source = "libelleService")
    @Mapping(target = "libelleService", source = "libelleService")
    @Mapping(target = "description", source = "descriptionService")
    @Mapping(target = "descriptionService", source = "descriptionService")
    @Mapping(target = "prixUnitaire", source = "prixUnitaireService")
    @Mapping(target = "prixUnitaireService", source = "prixUnitaireService")
    @Mapping(target = "quantite", source = "quantite")
    @Mapping(target = "quantiteMax", source = "service.quantiteMax")
    DemandeServiceDto toDemandeServiceDto(DemandeService ds);

    DemandeDocumentDto toDocumentDto(DemandeDocument document);

    @AfterMapping
    default void ensureServiceFallback(DemandeService source, @MappingTarget DemandeServiceDto target) {
        if (source == null || target == null) {
            return;
        }
        if (target.getIdService() == null && source.getService() != null) {
            target.setIdService(source.getService().getIdService());
        }
        if (target.getIdService() == null && source.getId() != null) {
            target.setIdService(source.getId().getIdService());
        }
        if (target.getIdDemande() == null && source.getDemande() != null) {
            target.setIdDemande(source.getDemande().getIdDemande());
        }
        if (target.getIdDemande() == null && source.getId() != null) {
            target.setIdDemande(source.getId().getIdDemande());
        }
        if (target.getQuantiteMax() == null && source.getService() != null) {
            target.setQuantiteMax(source.getService().getQuantiteMax());
        }
        if (target.getLibelle() == null && source.getService() != null) {
            target.setLibelle(source.getService().getLibelle());
        }
        if (target.getLibelleService() == null && source.getService() != null) {
            target.setLibelleService(source.getService().getLibelle());
        }
        if (target.getDescription() == null && source.getService() != null) {
            target.setDescription(source.getService().getDescription());
        }
        if (target.getDescriptionService() == null && source.getService() != null) {
            target.setDescriptionService(source.getService().getDescription());
        }
        if (target.getPrixUnitaire() == null && source.getService() != null) {
            target.setPrixUnitaire(source.getService().getPrixUnitaire());
        }
        if (target.getPrixUnitaireService() == null && source.getService() != null) {
            target.setPrixUnitaireService(source.getService().getPrixUnitaire());
        }
    }

    // TypeDemande -> TypeDemandeDto (si tu n’as pas déjà un mapper dédié)
    TypeDemandeDto toDto(TypeDemande td);

    // StatutDemande -> StatutDemandeDto
    StatutDemandeDto toDto(StatutDemande sd);

    // BONUS : si la demande est de type "RendezVous", on écrase le statutDemande par le statut du RDV
    @AfterMapping
    default void overrideStatutForRendezVous(Demande src, @MappingTarget DemandeResponse target) {
        if (src == null || src.getTypeDemande() == null) return;
        if (!"RendezVous".equals(src.getTypeDemande().getCodeType())) return;

        RendezVous rdv = src.getRendezVous(); // suppose un champ OneToOne<->Demande
        if (rdv != null && rdv.getStatut() != null) {
            // On mappe le StatutRendezVous → StatutDemandeDto (mêmes champs code/libellé)
            target.setStatutDemande(new StatutDemandeDto(
                    rdv.getStatut().getCodeStatut(),
                    rdv.getStatut().getLibelle()
            ));
        }
    }

    // ----------- DTO -> ENTITY (pour create/update) -----------
    // On ignore les relations ; on les associe en Service (par codes/ids)
    @Mapping(target = "idDemande", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "typeDemande", ignore = true)
    @Mapping(target = "statutDemande", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "rendezVous", ignore = true) // si lien 1-1
    Demande toEntity(DemandeRequest req);
}
