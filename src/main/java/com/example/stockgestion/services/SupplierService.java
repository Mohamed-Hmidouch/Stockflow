package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.SupplierRequestDto;
import com.example.stockgestion.Dto.response.SupplierResponseDto;
import com.example.stockgestion.models.Supplier;
import com.example.stockgestion.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {
    
    private final SupplierRepository supplierRepository;

    /**
     * Récupère tous les fournisseurs
     * @return Liste de tous les fournisseurs
     */
    public List<SupplierResponseDto> getAllSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(SupplierResponseDto::new)
                .toList();
    }

    /**
     * Crée un nouveau fournisseur
     * @param requestDto Données du fournisseur à créer
     * @return Le fournisseur créé
     */
    public SupplierResponseDto createSupplier(SupplierRequestDto requestDto) {
        Supplier supplier = new Supplier();
        supplier.setName(requestDto.getName());
        supplier.setContact(requestDto.getContact());
        
        Supplier savedSupplier = supplierRepository.save(supplier);
        return new SupplierResponseDto(savedSupplier);
    }

    /**
     * Récupère un fournisseur par son ID
     * @param id L'identifiant du fournisseur
     * @return Le fournisseur trouvé
     */
    public SupplierResponseDto getSupplierById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur introuvable avec l'ID: " + id));
        return new SupplierResponseDto(supplier);
    }

    /**
     * Met à jour un fournisseur existant
     * @param id L'identifiant du fournisseur à modifier
     * @param requestDto Les nouvelles données du fournisseur
     * @return Le fournisseur mis à jour
     */
    public SupplierResponseDto updateSupplier(UUID id, SupplierRequestDto requestDto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur introuvable avec l'ID: " + id));
        
        supplier.setName(requestDto.getName());
        supplier.setContact(requestDto.getContact());
        
        Supplier updatedSupplier = supplierRepository.save(supplier);
        return new SupplierResponseDto(updatedSupplier);
    }

    /**
     * Supprime un fournisseur
     * @param id L'identifiant du fournisseur à supprimer
     */
    public void deleteSupplier(UUID id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Fournisseur introuvable avec l'ID: " + id);
        }
        supplierRepository.deleteById(id);
    }
}