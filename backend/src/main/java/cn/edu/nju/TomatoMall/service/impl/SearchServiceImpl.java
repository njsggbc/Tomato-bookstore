package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.models.dto.product.ProductBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreInfoResponse;
import cn.edu.nju.TomatoMall.repository.ProductRepository;
import cn.edu.nju.TomatoMall.repository.StoreRepository;
import cn.edu.nju.TomatoMall.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SearchServiceImpl implements SearchService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    @Autowired
    public SearchServiceImpl(ProductRepository productRepository, StoreRepository storeRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public Page<ProductBriefResponse> searchProducts(String keyword, int page, int size,
                                                     String field, Boolean order,
                                                     BigDecimal minPrice, BigDecimal maxPrice) {
        // 根据是否指定排序字段来决定排序策略
        if (field == null || field.trim().isEmpty()) {
            // 未指定排序字段：使用相关度排序
            Pageable pageable = PageRequest.of(page, size);
            return productRepository.searchProductsByRelevance(
                    keyword, minPrice, maxPrice, pageable)
                    .map(ProductBriefResponse::new);
        } else {
            // 指定了排序字段：使用自定义排序
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by((order == null || !order) ? Sort.Direction.DESC : Sort.Direction.ASC, field));
            return productRepository.searchProductsWithCustomSort(
                    keyword, minPrice, maxPrice, pageable)
                    .map(ProductBriefResponse::new);
        }
    }

    @Override
    public Page<StoreInfoResponse> searchStores(String keyword, int page, int size,
                                                String field, Boolean order) {
        // 根据是否指定排序字段来决定排序策略
        if (field == null || field.trim().isEmpty()) {
            // 未指定排序字段：使用相关度排序
            Pageable pageable = PageRequest.of(page, size);
            return storeRepository.searchStoresByRelevance(keyword, pageable)
                    .map(StoreInfoResponse::new);
        } else {
            // 指定了排序字段：使用自定义排序
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by((order == null || !order) ? Sort.Direction.DESC : Sort.Direction.ASC, field));
            return storeRepository.searchStoresWithCustomSort(keyword, pageable)
                    .map(StoreInfoResponse::new);
        }
    }
}