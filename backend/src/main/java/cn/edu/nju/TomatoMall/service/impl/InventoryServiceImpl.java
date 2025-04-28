package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.InventoryStatus;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.po.Inventory;
import cn.edu.nju.TomatoMall.repository.InventoryRepository;
import cn.edu.nju.TomatoMall.repository.ProductRepository;
import cn.edu.nju.TomatoMall.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void setStock(int productId, int quantity) {
        if (quantity < 0) {
            throw TomatoMallException.invalidOperation();
        }

        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(TomatoMallException::productNotFound);

        inventory.setQuantity(quantity);

        inventoryRepository.save(inventory);

        productRepository.setInventoryStatusById(productId,
                InventoryStatus.getInventoryStatus(getAvailableStock(productId), inventory.getThresholdQuantity())
        );
    }

    @Override
    public void setThreshold(int productId, int threshold) {
        if (threshold < 0) {
            throw TomatoMallException.invalidOperation();
        }

        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(TomatoMallException::productNotFound);

        inventory.setThresholdQuantity(threshold);

        inventoryRepository.save(inventory);

        productRepository.setInventoryStatusById(productId,
                InventoryStatus.getInventoryStatus(getAvailableStock(productId), inventory.getThresholdQuantity())
        );
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void lockStock(int productId, int quantity) {
        if (quantity <= 0) {
            throw TomatoMallException.invalidOperation();
        }

        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(TomatoMallException::productNotFound);

        if (getAvailableStock(productId) < quantity) {
            throw TomatoMallException.insufficientStock();
        }

        int updated = inventoryRepository.lockStock(productId, quantity, inventory.getVersion());
        if (updated == 0) {
            throw new OptimisticLockingFailureException("并发更新库存失败，请重试");
        }

        productRepository.setInventoryStatusById(productId,
                InventoryStatus.getInventoryStatus(getAvailableStock(productId), inventory.getThresholdQuantity())
        );
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void unlockStock(int productId, int quantity) {
        if (quantity <= 0) {
            throw TomatoMallException.invalidOperation();
        }

        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(TomatoMallException::productNotFound);

        if (inventory.getLockedQuantity() < quantity) {
            throw TomatoMallException.invalidOperation();
        }

        int updated = inventoryRepository.unlockStock(productId, quantity, inventory.getVersion());
        if (updated == 0) {
            throw new OptimisticLockingFailureException("并发更新库存失败，请重试");
        }

        productRepository.setInventoryStatusById(productId,
                InventoryStatus.getInventoryStatus(getAvailableStock(productId), inventory.getThresholdQuantity())
        );
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void confirmStockDeduction(int productId, int quantity) {
        if (quantity <= 0) {
            throw TomatoMallException.invalidOperation();
        }

        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(TomatoMallException::productNotFound);

        if (inventory.getLockedQuantity() < quantity) {
            throw TomatoMallException.insufficientStock();
        }

        int updated = inventoryRepository.decreaseStock(productId, quantity, inventory.getVersion());
        if (updated == 0) {
            throw new OptimisticLockingFailureException("并发更新库存失败，请重试");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getAvailableStock(int productId) {
        return inventoryRepository.getAvailableStockById(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStock(int productId, int quantity) {
        int availableStock = getAvailableStock(productId);
        return availableStock >= quantity;
    }
}