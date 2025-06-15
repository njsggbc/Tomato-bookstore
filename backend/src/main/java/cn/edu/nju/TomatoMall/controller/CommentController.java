package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.models.dto.comment.*;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 创建新商店评论
     * @param commentCreateRequest 评论信息
     * @return 空
     */
    @PostMapping("/store/{storeId}")
    public ApiResponse<Void> createStoreComment(
            @PathVariable int storeId,
            @RequestBody CommentCreateRequest commentCreateRequest) {
        commentService.comment(
                EntityType.STORE, storeId, commentCreateRequest.getContent(), commentCreateRequest.getRating()
        );
        return ApiResponse.success();
    }

    /**
     * 创建新商品评论
     * @param productId 商品ID
     * @param commentCreateRequest 评论信息
     * @return 空
     */
    @PostMapping("/product/{productId}")
    public ApiResponse<Void> createProductComment(
            @PathVariable int productId,
            @RequestBody CommentCreateRequest commentCreateRequest) {
        commentService.comment(
                EntityType.PRODUCT, productId, commentCreateRequest.getContent(), commentCreateRequest.getRating()
        );
        return ApiResponse.success();
    }

    /**
     * 回复评论
     * @param parentId 父评论ID
     * @param commentCreateRequest 回复信息
     * @return 空
     */
    @PostMapping("/{parentId}/reply")
    public ApiResponse<Void> replyToComment(@PathVariable int parentId,
                                            @RequestBody CommentCreateRequest commentCreateRequest) {
        commentService.reply(parentId, commentCreateRequest.getContent());
        return ApiResponse.success();
    }

    /**
     * 更新评论
     * @param commentId 评论ID
     * @param commentUpdateRequest 更新信息
     * @return 空
     */
    @PutMapping("/{commentId}")
    public ApiResponse<Void> updateComment(@PathVariable int commentId,
                                           @RequestBody CommentUpdateRequest commentUpdateRequest) {
        commentService.update(commentId, commentUpdateRequest.getContent(), commentUpdateRequest.getRating());
        return ApiResponse.success();
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     * @return 空
     */
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable int commentId) {
        commentService.delete(commentId);
        return ApiResponse.success();
    }

    /**
     * 获取商店评论列表
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 是否升序
     * @return 评论列表
     */
    @GetMapping("/store/{storeId}")
    public ApiResponse<Page<CommentResponse>> getStoreComments(
            @PathVariable int storeId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "field", defaultValue = "createTime") String field,
            @RequestParam(value = "order", defaultValue = "false") boolean order) {
        return ApiResponse.success(
                commentService.getComments(EntityType.STORE, storeId, page, size, field, order)
        );
    }

    /**
     * 获取商品评论列表
     * @param productId 商品ID
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 是否升序
     * @return 评论列表
     */
    @GetMapping("/product/{productId}")
    public ApiResponse<Page<CommentResponse>> getProductComments(
            @PathVariable int productId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "field", defaultValue = "createTime") String field,
            @RequestParam(value = "order", defaultValue = "false") boolean order) {
        return ApiResponse.success(
                commentService.getComments(EntityType.PRODUCT, productId, page, size, field, order)
        );
    }

    /**
     * 获取评论的回复列表
     * @param parentId 父评论ID
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 是否升序
     * @return 回复列表
     */
    @GetMapping("/{parentId}/reply")
    public ApiResponse<Page<CommentResponse>> getReplies(
            @PathVariable int parentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "field", defaultValue = "createTime") String field,
            @RequestParam(value = "order", defaultValue = "false") boolean order) {
        return ApiResponse.success(
                commentService.getReplies(parentId, page, size, field, order)
        );
    }

    /**
     * 点赞/取消点赞评论
     * @param commentId 评论ID
     * @return 空
     */
    @PostMapping("/{commentId}/like")
    public ApiResponse<Void> toggleLike(@PathVariable int commentId) {
        commentService.toggleLike(commentId);
        return ApiResponse.success();
    }
} 