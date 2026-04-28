// 路径: common/src/main/java/com/videoshare/query/CommentQuery.java
package com.videoshare.common.query;

/**
 * 评论查询条件
 */
public class CommentQuery {

    private Integer pageNum  = 1;
    private Integer pageSize = 20;
    private String  videoId;       // 必填：哪个视频的评论
    private Long    pCommentId;    // null=查顶级, 非null=查某条评论的回复
    private Integer status;        // null=全部, 1=已通过

    public Integer getOffset() {
        return (this.pageNum - 1) * this.pageSize;
    }

    public Integer getPageNum()   { return pageNum; }
    public void setPageNum(Integer v)  { this.pageNum = (v == null || v < 1) ? 1 : v; }
    public Integer getPageSize()  { return pageSize; }
    public void setPageSize(Integer v) { this.pageSize = (v == null || v < 1) ? 20 : Math.min(v, 100); }
    public String  getVideoId()   { return videoId; }
    public void    setVideoId(String v)  { this.videoId = v; }
    public Long    getPCommentId(){ return pCommentId; }
    public void    setPCommentId(Long v) { this.pCommentId = v; }
    public Integer getStatus()    { return status; }
    public void    setStatus(Integer v)  { this.status = v; }
}