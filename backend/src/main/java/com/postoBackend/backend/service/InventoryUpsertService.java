package com.postoBackend.backend.service;

import com.postoBackend.backend.domain.inventory.Inventory;
import com.postoBackend.backend.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryUpsertService {

    private final InventoryRepository inventoryRepository;

    public InventoryUpsertService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Inventory saveOrUpdate(Inventory incomingInventory) {
        if (incomingInventory.getProduct() == null || incomingInventory.getProduct().getId() == null) {
            throw new IllegalArgumentException("Inventory must reference a persisted product");
        }

        Inventory existingInventory = inventoryRepository.findByProduct_Id(
                incomingInventory.getProduct().getId()
        ).orElse(null);

        if (existingInventory == null) {
            return inventoryRepository.save(incomingInventory);
        }

        existingInventory.setProduct(incomingInventory.getProduct());
        existingInventory.setQuantityInStock(incomingInventory.getQuantityInStock());

        return inventoryRepository.save(existingInventory);
    }
}
