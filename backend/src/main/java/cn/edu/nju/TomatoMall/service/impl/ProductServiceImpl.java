package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.product.ProductBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.product.ProductCreateRequest;
import cn.edu.nju.TomatoMall.models.dto.product.ProductDetailResponse;
import cn.edu.nju.TomatoMall.models.dto.product.ProductUpdateRequest;
import cn.edu.nju.TomatoMall.models.po.Product;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.ProductRepository;
import cn.edu.nju.TomatoMall.repository.StoreRepository;
import cn.edu.nju.TomatoMall.service.ProductService;
import cn.edu.nju.TomatoMall.util.FileUtil;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    StoreRepository storeRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SecurityUtil securityUtil;

    @Autowired
    FileUtil fileUtil;

    @Override
    public Page<ProductBriefResponse> getProductList(int page, int size, String field, boolean order) {
        Sort.Direction direction = order ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE, Sort.by(direction, field));

        Page<Product> productPage = productRepository.findAll(pageable);

        return productPage.map(ProductBriefResponse::new);
    }

    @Override
    public Boolean createProduct(ProductCreateRequest params) {
        Store store = storeRepository.findById(params.getStoreId()).orElseThrow(TomatoMallException::storeNotFound);

        User user = securityUtil.getCurrentUser();
        if (!store.getManager().equals(user) && !store.getStaffs().contains(user)) {
            throw TomatoMallException.permissionDenied();
        }

        Product product = new Product();
        product.setName(params.getTitle());
        product.setPrice(params.getPrice());
        product.setStock(params.getStock());
        product.setSales(0);
        product.setDescription(params.getDescription());
        product.setImages(params.getImages().stream()
                        .map(image -> fileUtil.upload(user.getId(), image))
                        .collect(Collectors.toList())
        );
        product.setSpecifications(params.getSpecifications());
        product.setCreateTime(LocalDateTime.now());
        product.setStore(store);

        // 主实体级联保存
        store.getProducts().add(product);
        storeRepository.save(store);

        return true;
    }

    @Override
    public Boolean updateProduct(int productId, ProductUpdateRequest params) {
        Product product = productRepository.findById(productId).orElseThrow(TomatoMallException::productNotFound);
        Store store = product.getStore();
        User user = securityUtil.getCurrentUser();
        if (!store.getManager().equals(user) && !store.getStaffs().contains(user)) {
            throw TomatoMallException.permissionDenied();
        }

        if (params.getTitle() != null) {
            product.setName(params.getTitle());
        }
        if (params.getDescription() != null) {
            product.setDescription(params.getDescription());
        }
        if (params.getPrice() != null) {
            product.setPrice(params.getPrice());
        }
        if (params.getStock() != null) {
            product.setStock(params.getStock());
        }
        if (params.getImages() != null) {
            if (product.getImages() != null) {
                product.getImages().forEach(fileUtil::delete);
            }
            product.setImages(params.getImages().stream()
                            .map(image -> fileUtil.upload(user.getId(), image))
                            .collect(Collectors.toList())
            );
        }
        if (params.getSpecifications() != null) {
            product.setSpecifications(params.getSpecifications());
        }

        productRepository.save(product);

        return true;
    }

    @Override
    public String deleteProduct(int productId) {
        Product product = productRepository.findById(productId).orElseThrow(TomatoMallException::productNotFound);
        Store store = product.getStore();
        User user = securityUtil.getCurrentUser();
        if (!user.getRole().equals(Role.ADMIN) && !store.getManager().equals(user) && !store.getStaffs().contains(user)) {
            throw TomatoMallException.permissionDenied();
        }

        if (product.getImages() != null) {
            product.getImages().forEach(fileUtil::delete);
        }

        // 主实体级联保存
        store.getProducts().remove(product);
        storeRepository.save(store);

        return "删除成功";
    }

    @Override
    public ProductDetailResponse getProductDetail(int productId) {
        return new ProductDetailResponse(productRepository.findById(productId).orElseThrow(TomatoMallException::productNotFound));
    }

    /*------------- HACK: 以下为兼容测试用方法 -------------*/

    @Override
    public String updateStockpile(int productId, int stockpile) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }
        Product product = productRepository.findById(productId).orElseThrow(TomatoMallException::productNotFound);
        product.setStock(stockpile);
        productRepository.save(product);
        return "调整库存成功";
    }

    @Override
    public Integer getStockpile(int productId) {
        return productRepository.findById(productId).orElseThrow(TomatoMallException::productNotFound).getStock();
    }

    @Override
    public ProductBriefResponse createProduct(Map<String, Object> params) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }

        Product product = new Product();
        product.setName(params.get("title").toString());
        product.setPrice(Double.parseDouble(params.get("price").toString()));
        product.setStock(0);
        product.setRate(Double.parseDouble(params.get("rate").toString()));
        product.setDescription(params.get("description").toString());
        List<String> images = new ArrayList<>();
        images.add(params.get("cover").toString());
        product.setImages(images);
        product.setSpecifications(new HashMap<>()); // HACK: 未提供规格信息
        product.setSales(0);
        product.setCreateTime(LocalDateTime.now());

        // HACK: 实体中没有 detail 字段

        Store store = storeRepository.findById(1).orElseThrow(TomatoMallException::unexpectedError);
        product.setStore(store);

        productRepository.save(product);

        return new ProductBriefResponse(product);
    }

    @Override
    public String updateProduct(Map<String, Object> params) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }

        Product product = productRepository.findById(Integer.parseInt(params.get("id").toString())).orElseThrow(TomatoMallException::productNotFound);
        product.setName(params.get("title").toString());
        product.setPrice(Double.parseDouble(params.get("price").toString()));
        product.setRate(Double.parseDouble(params.get("rate").toString()));

        if (params.get("description") != null) {
            product.setDescription(params.get("description").toString());
        }

        if (params.get("cover") != null) {
            List<String> images = new ArrayList<>();
            images.add(params.get("cover").toString());
            product.setImages(images);
        }

        // HACK: 不兼容部分： detail, specifications

        productRepository.save(product);

        return "更新成功";
    }

}
