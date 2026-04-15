
package com.videoshare.common.query;


//用户信息查询条件类（分页 + 搜索）

public class UserInfoQuery {

    //  分页参数（有默认值，防止前端不传） 
    private Integer pageNum  = 1;   // 当前页码，默认第1页
    private Integer pageSize = 15;  // 每页条数，默认15条

    //  搜索条件（可为 null，不传则不过滤） 
    private String  keyword; // 关键词（邮箱 或 昵称 模糊搜索）
    private Integer status;  // 账号状态筛选：0=禁用，1=启用，null=全部

    //从第几条开始查
    // 例：第2页每页15条 → OFFSET = 15，跳过前15条
    public Integer getOffset() {
        return (this.pageNum - 1) * this.pageSize;
    }

    //  Getters & Setters 
    public Integer getPageNum()  { return pageNum; }
    public void setPageNum(Integer pageNum) {
        // 页码至少为1，防止前端传0或负数
        this.pageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
    }

    public Integer getPageSize() {return pageSize; }

    public void setPageSize(Integer pageSize) {
        // 每页条数限制在 1-100 之间，防止一次查太多
        if (pageSize == null || pageSize < 1)  this.pageSize = 15;
        else if (pageSize > 100)               this.pageSize = 100;
        else                                   this.pageSize = pageSize;
    }

    public String getKeyword()   { return keyword; }
    public void setKeyword(String keyword) {
        // 空字符串当作 null 处理
        this.keyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;
    }

    public Integer getStatus()   { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
