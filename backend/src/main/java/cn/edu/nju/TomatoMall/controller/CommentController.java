package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.models.dto.comment.*;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 创建新评论
     * @param commentCreateRequest 评论信息
     * @return 空
     */
    @PostMapping
    public ApiResponse<Void> createComment(@Valid @RequestBody CommentCreateRequest commentCreateRequest) {
        commentService.createComment(commentCreateRequest);
        return ApiResponse.success();
    }



    /**
     * 获取商品评论（分页）
     * @param itemId 商品ID
     * @param page 页码（从0开始）
     * @param size 每页显示数量
     * @param field 排序字段
     * @param order 排序方向（true为升序，false为降序）
     * @return 评论列表
     */
    @GetMapping("/item/{itemId}")
    public ApiResponse<Page<ItemCommentResponse>> getItemComments(
            @PathVariable int itemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String field,
            @RequestParam(defaultValue = "false") boolean order) {
        return ApiResponse.success(commentService.getItemCommentsPaged(itemId, page, size, field, order));
    }

    /**
     * 获取商店评论（分页）
     * @param shopId 商店ID
     * @param page 页码（从0开始）
     * @param size 每页显示数量
     * @param field 排序字段
     * @param order 排序方向（true为升序，false为降序）
     * @return 评论列表
     */
    @GetMapping("/shop/{shopId}")
    public ApiResponse<Page<StoreCommentResponse>> getShopComments(
            @PathVariable int shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String field,
            @RequestParam(defaultValue = "false") boolean order) {
        return ApiResponse.success(commentService.getShopCommentsPaged(shopId, page, size, field, order));
    }

    /**
     * 点赞评论
     * @param commentId 评论ID
     * @return 更新后的评论
     */
    @PostMapping("/{commentId}/like")
    public ApiResponse<Void> likeComment(@PathVariable int commentId) {
        commentService.likeComment(commentId);
        return ApiResponse.success();
    }

    /**
     * 回复评论
     * @param parentId 父评论ID
     * @param replyRequest 回复内容
     * @return 空
     */
    @PostMapping("/{parentId}/reply")
    public ApiResponse<Void> replyToComment(
            @PathVariable int parentId,
            @Valid @RequestBody CommentReplyRequest replyRequest) {
        commentService.replyToComment(parentId, replyRequest);
        return ApiResponse.success();
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 无内容
     */
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable int commentId,
            @RequestParam int userId) {
        commentService.deleteComment(commentId, userId);
        return ApiResponse.success();
    }

    /**
     * 获取评论的回复列表
     * @param commentId 父评论ID
     * @return 回复列表
     */
    @GetMapping("/{commentId}/replies")
    public ApiResponse<List<CommentReplyResponse>> getReplies(@PathVariable int commentId) {
        return ApiResponse.success(commentService.getReplies(commentId));
    }

    /**
     * 获取商品的平均评分
     * @param itemId 商品ID
     * @return 平均评分
     */
    @GetMapping("/item/{itemId}/rating")
    public ApiResponse<Double> getItemAverageRating(@PathVariable int itemId) {
        return ApiResponse.success(commentService.getItemAverageRating(itemId));
    }

    /**
     * 获取商店的平均评分
     * @param shopId 商店ID
     * @return 平均评分
     */
    @GetMapping("/shop/{shopId}/rating")
    public ApiResponse<Double> getShopAverageRating(@PathVariable int shopId) {
        return ApiResponse.success(commentService.getShopAverageRating(shopId));
    }
} 