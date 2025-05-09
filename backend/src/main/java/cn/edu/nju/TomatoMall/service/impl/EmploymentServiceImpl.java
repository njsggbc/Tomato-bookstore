package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.employment.*;
import cn.edu.nju.TomatoMall.models.dto.user.UserBriefResponse;
import cn.edu.nju.TomatoMall.models.po.Employment;
import cn.edu.nju.TomatoMall.models.po.EmploymentToken;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.EmploymentRepository;
import cn.edu.nju.TomatoMall.repository.EmploymentTokenRepository;
import cn.edu.nju.TomatoMall.repository.StoreRepository;
import cn.edu.nju.TomatoMall.service.EmploymentService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmploymentServiceImpl implements EmploymentService {
    private final StoreRepository storeRepository;
    private final EmploymentTokenRepository employmentTokenRepository;
    private final EmploymentRepository employmentRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    public EmploymentServiceImpl(
            StoreRepository storeRepository,
            EmploymentTokenRepository employmentTokenRepository,
            EmploymentRepository employmentRepository,
            SecurityUtil securityUtil
    ) {
        this.storeRepository = storeRepository;
        this.employmentTokenRepository = employmentTokenRepository;
        this.employmentRepository = employmentRepository;
        this.securityUtil = securityUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TokenInfoResponse> getTokenList(int storeId) {
        validatePermission(storeId);
        return employmentTokenRepository.findAllByStoreId(storeId).stream()
                .map(TokenInfoResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String generateToken(int storeId, TokenGenerateRequest params) {
        validatePermission(storeId);

        Store storeRef = storeRepository.getReferenceById(storeId);
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (employmentTokenRepository.existsByTokenAndStoreId(token, storeId));

        EmploymentToken employmentToken = EmploymentToken.builder()
                .token(token)
                .name(params.getName())
                .store(storeRef)
                .expiresAt(params.getExpireTime() == null ? null : LocalDateTime.parse(params.getExpireTime()))
                .valid(true)
                .build();

        employmentTokenRepository.save(employmentToken);

        return employmentToken.getToken();
    }

    @Override
    @Transactional
    public void deleteToken(int storeId, int tokenId) {
        validatePermission(storeId);

        EmploymentToken token = employmentTokenRepository.findByIdAndStoreId(tokenId, storeId)
                .orElseThrow(TomatoMallException::tokenInvalid);

        employmentTokenRepository.delete(token);
    }

    @Override
    @Transactional
    public void authToken(int storeId, String tokenValue) {
        User user = securityUtil.getCurrentUser();

        if (storeRepository.existsByIdAndManagerId(storeId, user.getId())) {
            throw TomatoMallException.invalidOperation();
        }

        // 显式检查雇佣关系是否存在
        if (employmentRepository.existsByStoreIdAndEmployeeId(storeId, user.getId())) {
            throw TomatoMallException.storeStaffAlreadyExists();
        }

        // 校验 Token 有效性
        EmploymentToken token = employmentTokenRepository
                .findValidByTokenAndStoreId(tokenValue, storeId)
                .orElseThrow(TomatoMallException::tokenInvalid);

        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw TomatoMallException.tokenInvalid();
        }

        // 创建雇佣关系
        Employment employment = Employment.builder()
                .employee(user)
                .store(token.getStore())
                .build();

        // 标记 Token 为已使用
        token.setValid(false);
        token.setConsumer(user);
        token.setExpiresAt(null);

        employmentRepository.save(employment);
        employmentTokenRepository.save(token);
    }

    @Override
    @Transactional
    public void dismiss(int storeId, int userId) {
        validatePermission(storeId);

        Employment employment = employmentRepository.findByStoreIdAndEmployeeId(storeId, userId)
                .orElseThrow(TomatoMallException::invalidOperation);
        employmentRepository.delete(employment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBriefResponse> getStaffList(int storeId) {
        return employmentRepository.getEmployeeByStoreId(storeId)
                .stream()
                .map(UserBriefResponse::new)
                .collect(Collectors.toList());
    }

    private void validatePermission(Integer storeId) {
        if (!storeRepository.existsByIdAndManagerId(storeId, securityUtil.getCurrentUser().getId())) {
            throw TomatoMallException.permissionDenied();
        }
    }
}
