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
import cn.edu.nju.TomatoMall.repository.EmploymentRepository;
import cn.edu.nju.TomatoMall.repository.EmploymentTokenRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final EmploymentRepository employmentRepository;
    private final SecurityUtil securityUtil;
    private final FileUtil fileUtil;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, StoreRepository storeRepository, EmploymentRepository employmentRepository, SecurityUtil securityUtil, FileUtil fileUtil) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.employmentRepository = employmentRepository;
        this.securityUtil = securityUtil;
        this.fileUtil = fileUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductBriefResponse> getProductList(int page, int size, String field, boolean order) {
        Sort.Direction direction = order ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE, Sort.by(direction, field));

        return productRepository
                .findAll(pageable)
                .map(ProductBriefResponse::new);
    }

    @Override
    public Page<ProductBriefResponse> getStoreProductList(int storeId, int page, int size, String field, boolean order) {
        Sort.Direction direction = order ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE, Sort.by(direction, field));

        return productRepository
                .findByStoreId(storeId, pageable)
                .map(ProductBriefResponse::new);
    }

    @Override
    public void createProduct(ProductCreateRequest params) {
        validatePermission(params.getStoreId());

        Product product = new Product();
        product.setName(params.getTitle());
        product.setPrice(params.getPrice());
        product.setStock(params.getStock());
        product.setSales(0);
        product.setDescription(params.getDescription());
        product.setImages(uploadImages(params.getImages()));
        product.setSpecifications(params.getSpecifications());
        product.setCreateTime(LocalDateTime.now());
        product.setStore(storeRepository.getReferenceById(params.getStoreId()));

        productRepository.save(product);
    }

    @Override
    public void updateProduct(int productId, ProductUpdateRequest params) {
        Product product = productRepository.findById(productId).orElseThrow(TomatoMallException::productNotFound);

        validatePermission(product.getStore().getId());

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
            deleteImages(product.getImages());
            product.setImages(uploadImages(params.getImages()));
        }
        if (params.getSpecifications() != null) {
            product.setSpecifications(params.getSpecifications());
        }

        productRepository.save(product);
    }

    @Override
    public String deleteProduct(int productId) {
        Product product = productRepository.findById(productId).orElseThrow(TomatoMallException::productNotFound);

        validatePermission(product.getStore().getId());

        if (product.getImages() != null) {
            deleteImages(product.getImages());
        }

        // TODO: 删除条件检查

        productRepository.delete(product);

        // HACK: for test
        return "删除成功";
    }

    @Override
    public ProductDetailResponse getProductDetail(int productId) {
        return new ProductDetailResponse(productRepository.findById(productId).orElseThrow(TomatoMallException::productNotFound));
    }

    private void validatePermission(Integer storeId) {
        User currentUser = securityUtil.getCurrentUser();
        // 系统默认店铺，只有系统管理员具有权限
        if (storeId == 1) {
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                throw TomatoMallException.permissionDenied();
            }
            return;
        }

        if (!storeRepository.existsByIdAndManagerId(storeId, currentUser.getId())
                && !employmentRepository.existsByStoreIdAndEmployeeId(storeId, currentUser.getId())) {
            throw TomatoMallException.permissionDenied();
        }

    }

    private List<String> uploadImages(List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            String imageUrl = fileUtil.upload(securityUtil.getCurrentUser().getId(), image);
            imageUrls.add(imageUrl);
        }
        return imageUrls;
    }

    private void deleteImages(List<String> imageUrls) {
        if (imageUrls != null) {
            imageUrls.forEach(fileUtil::delete);
        }
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
    public int getStockpile(int productId) {
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
