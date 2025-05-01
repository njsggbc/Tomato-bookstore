package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.product.*;
import cn.edu.nju.TomatoMall.models.po.Inventory;
import cn.edu.nju.TomatoMall.models.po.Product;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.*;
import cn.edu.nju.TomatoMall.service.ProductService;
import cn.edu.nju.TomatoMall.service.InventoryService;
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

import java.math.BigDecimal;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final StoreRepository storeRepository;
    private final EmploymentRepository employmentRepository;
    private final ProductSnapshotRepository productSnapshotRepository;
    private final SecurityUtil securityUtil;
    private final FileUtil fileUtil;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              InventoryService inventoryService,
                              StoreRepository storeRepository,
                              EmploymentRepository employmentRepository,
                              ProductSnapshotRepository productSnapshotRepository,
                              SecurityUtil securityUtil,
                              FileUtil fileUtil) {
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.storeRepository = storeRepository;
        this.employmentRepository = employmentRepository;
        this.productSnapshotRepository = productSnapshotRepository;
        this.securityUtil = securityUtil;
        this.fileUtil = fileUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductBriefResponse> getProductList(int page, int size, String field, boolean order) {
        Sort.Direction direction = order ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE, Sort.by(direction, field));

        return productRepository
                .findByOnSaleIsTrue(pageable)
                .map(ProductBriefResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductBriefResponse> getStoreProductList(int storeId, int page, int size, String field, boolean order) {
        Sort.Direction direction = order ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size > 0 ? size : Integer.MAX_VALUE, Sort.by(direction, field));

        return productRepository
                .findByStoreIdAndOnSaleIsTrue(storeId, pageable)
                .map(ProductBriefResponse::new);
    }

    @Override
    @Transactional
    public void createProduct(ProductCreateRequest params) {
        validatePermission(params.getStoreId());

        validateName(params.getTitle());
        validatePrice(params.getPrice());
        validateDescription(params.getDescription());
        validateImages(params.getImages());
        validateSpecifications(params.getSpecifications());

        Product product = Product.builder()
                .name(params.getTitle())
                .price(params.getPrice())
                .description(params.getDescription())
                .images(uploadImages(params.getImages()))
                .specifications(params.getSpecifications())
                .store(storeRepository.getReferenceById(params.getStoreId()))
                .build();

        product.setInventory(Inventory.builder().product(product).build());

        productRepository.save(product);
    }

    @Override
    @Transactional
    public void updateProduct(int productId, ProductUpdateRequest params) {
        Product product = productRepository.findByIdAndOnSaleIsTrue(productId).orElseThrow(TomatoMallException::productNotFound);

        validatePermission(product.getStore().getId());

        if (params.getTitle() != null) {
            validateName(params.getTitle());
            product.setName(params.getTitle());
        }
        if (params.getDescription() != null) {
            validateDescription(params.getDescription());
            product.setDescription(params.getDescription());
        }
        if (params.getPrice() != null) {
            validatePrice(params.getPrice());
            product.setPrice(params.getPrice());
        }
        if (params.getImages() != null) {
            validateImages(params.getImages());
            deleteImages(product.getImages());
            product.setImages(uploadImages(params.getImages()));
        }
        if (params.getSpecifications() != null) {
            validateSpecifications(params.getSpecifications());
            product.setSpecifications(params.getSpecifications());
        }

        // 更新版本信息，并创建新的快照
        product.update();

        productRepository.save(product);
    }

    @Override
    @Transactional
    public String deleteProduct(int productId) {
        Product product = productRepository.findById(productId).orElseThrow(TomatoMallException::productNotFound);

        validatePermission(product.getStore().getId());

        product.setOnSale(false);
        product.setDescription(null);
        product.setSpecifications(null);
        product.setRate(null);
        if (product.getImages() != null) {
            deleteImages(product.getImages().subList(1, product.getImages().size())); // 保留封面
        }
        product.setInventory(null);

        productRepository.save(product);

        // HACK: for test
        return "删除成功";
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(int productId) {
        return new ProductDetailResponse(productRepository.findByIdAndOnSaleIsTrue(productId)
                .orElseThrow(TomatoMallException::productNotFound));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSnapshotResponse getSnapshot(int snapshotId) {
        return new ProductSnapshotResponse(productSnapshotRepository.findById(snapshotId));
    }

    @Override
    @Transactional
    public String updateStockpile(int productId, int stockpile) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }
        inventoryService.setStock(productId, stockpile);
        return "调整库存成功";
    }

    @Override
    @Transactional
    public void updateThreshold(int productId, int threshold) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }
        inventoryService.setThreshold(productId, threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public int getStockpile(int productId) {
        return inventoryService.getAvailableStock(productId);
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

    /*------------- HACK: 以下为兼容测试用方法 -------------*/

    @Override
    @Transactional
    public ProductBriefResponse createProduct(Map<String, Object> params) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }

        Product product = Product.builder()
                .name(params.get("title").toString())
                .price(new BigDecimal(params.get("price").toString()))
                .rate(Double.parseDouble(params.get("rate").toString()))
                .description(params.get("description").toString())
                .images(Collections.singletonList(params.get("cover").toString()))
                .store(storeRepository.findById(1).orElseThrow(TomatoMallException::unexpectedError))
                .build();

        productRepository.save(product);

        return new ProductBriefResponse(product);
    }

    @Override
    @Transactional
    public String updateProduct(Map<String, Object> params) {
        if (!securityUtil.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw TomatoMallException.permissionDenied();
        }

        Product product = productRepository.findById(Integer.parseInt(params.get("id").toString())).orElseThrow(TomatoMallException::productNotFound);
        product.setName(params.get("title").toString());
        product.setPrice(new BigDecimal(params.get("price").toString()));
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

    private void validateName(String name) {
        if (name == null || name.isEmpty()) {
            throw TomatoMallException.invalidParameter("商品名称不能为空");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw TomatoMallException.invalidParameter("商品价格必须大于0");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isEmpty()) {
            throw TomatoMallException.invalidParameter("商品描述不能为空");
        }
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw TomatoMallException.invalidParameter("商品图片不能为空");
        }
    }

    private void validateSpecifications(Map<String, String> specifications) {
        if (specifications == null || specifications.isEmpty()) {
            throw TomatoMallException.invalidParameter("商品规格不能为空");
        }
        for (Map.Entry<String, String> entry : specifications.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isEmpty()) {
                throw TomatoMallException.invalidParameter("商品规格名称不能为空");
            }
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                throw TomatoMallException.invalidParameter("商品规格值不能为空");
            }
        }
    }

}
