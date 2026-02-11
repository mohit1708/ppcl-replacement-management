package com.ppcl.replacement.model;

/**
 * Model class to hold dashboard filter parameters
 * Used by DashboardServlet and DashboardDAO for filtering queries
 */
public class DashboardFilters {

    private String fromDate;
    private String toDate;
    private Integer requesterId;
    private Integer accountManagerId;
    private Integer currentOwnerId;
    private Integer stageId;
    private String status;
    private String tatFilter;        // "within" or "beyond"
    private Integer departmentId;
    private String departmentName;   // For display in filter tags
    private Integer categoryId;      // Replacement reason ID
    private String categoryName;     // For display in filter tags
    private Integer eventRequestId;  // For event tracking - filter by replacement request ID

    // Pagination
    private int page = 1;
    private int pageSize = 10;

    // Sorting
    private String sortColumn = "CREATION_DATE_TIME";
    private String sortDirection = "DESC";

    // View type for KPI drill-down
    private String view;             // "total", "pending", "tat-breach", "closed"

    // Default constructor - no default date range (show all records)
    public DashboardFilters() {
        // No default dates - show all records by default
    }

    // Constructor with date range
    public DashboardFilters(final String fromDate, final String toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        applyDefaultDates();
    }

    /**
     * Apply default date range if not provided
     */
    public void applyDefaultDates() {
        // No default dates - show all records by default
        // User can optionally select date range in the UI
    }

    /**
     * Check if any filters are applied
     */
    public boolean hasFilters() {
        return (requesterId != null) ||
                (accountManagerId != null) ||
                (currentOwnerId != null) ||
                (stageId != null) ||
                (status != null && !status.isEmpty()) ||
                (tatFilter != null && !tatFilter.isEmpty()) ||
                (departmentId != null) ||
                (categoryId != null);
    }

    /**
     * Calculate offset for pagination
     */
    public int getOffset() {
        return (page - 1) * pageSize;
    }

    // Getters and Setters
    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(final String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(final String toDate) {
        this.toDate = toDate;
    }

    public Integer getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(final Integer requesterId) {
        this.requesterId = requesterId;
    }

    public Integer getAccountManagerId() {
        return accountManagerId;
    }

    public void setAccountManagerId(final Integer accountManagerId) {
        this.accountManagerId = accountManagerId;
    }

    public Integer getCurrentOwnerId() {
        return currentOwnerId;
    }

    public void setCurrentOwnerId(final Integer currentOwnerId) {
        this.currentOwnerId = currentOwnerId;
    }

    public Integer getStageId() {
        return stageId;
    }

    public void setStageId(final Integer stageId) {
        this.stageId = stageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getTatFilter() {
        return tatFilter;
    }

    public void setTatFilter(final String tatFilter) {
        this.tatFilter = tatFilter;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(final Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(final String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(final Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(final String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getEventRequestId() {
        return eventRequestId;
    }

    public void setEventRequestId(final Integer eventRequestId) {
        this.eventRequestId = eventRequestId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(final int page) {
        this.page = page > 0 ? page : 1;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize > 0 ? pageSize : 10;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(final String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(final String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getView() {
        return view;
    }

    public void setView(final String view) {
        this.view = view;
    }

    @Override
    public String toString() {
        return "DashboardFilters{" +
                "fromDate='" + fromDate + '\'' +
                ", toDate='" + toDate + '\'' +
                ", requesterId=" + requesterId +
                ", accountManagerId=" + accountManagerId +
                ", currentOwnerId=" + currentOwnerId +
                ", stageId=" + stageId +
                ", status='" + status + '\'' +
                ", tatFilter='" + tatFilter + '\'' +
                ", departmentId=" + departmentId +
                ", categoryId=" + categoryId +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", view='" + view + '\'' +
                '}';
    }
}

