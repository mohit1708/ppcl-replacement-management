package com.ppcl.replacement.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for Dashboard summary data including KPIs and aggregated views
 */
public class DashboardSummary {

    // KPI Counts
    private int totalRequests;
    private int pendingRequests;
    private int tatBreachCount;
    private int closedRequests;

    // Pending breakdown
    private int pendingServiceTL;
    private int pendingAMManager;
    private int pendingAM;

    // Aggregated views
    private List<StageSummary> stageSummary = new ArrayList<>();
    private List<OwnerSummary> ownerSummary = new ArrayList<>();
    private List<DepartmentSummary> departmentSummary = new ArrayList<>();
    private List<CategorySummary> categorySummary = new ArrayList<>();

    // Getters and Setters
    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(final int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public int getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(final int pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public int getTatBreachCount() {
        return tatBreachCount;
    }

    public void setTatBreachCount(final int tatBreachCount) {
        this.tatBreachCount = tatBreachCount;
    }

    public int getClosedRequests() {
        return closedRequests;
    }

    public void setClosedRequests(final int closedRequests) {
        this.closedRequests = closedRequests;
    }

    public int getPendingServiceTL() {
        return pendingServiceTL;
    }

    public void setPendingServiceTL(final int pendingServiceTL) {
        this.pendingServiceTL = pendingServiceTL;
    }

    public int getPendingAMManager() {
        return pendingAMManager;
    }

    public void setPendingAMManager(final int pendingAMManager) {
        this.pendingAMManager = pendingAMManager;
    }

    public int getPendingAM() {
        return pendingAM;
    }

    public void setPendingAM(final int pendingAM) {
        this.pendingAM = pendingAM;
    }

    public List<StageSummary> getStageSummary() {
        return stageSummary;
    }

    public void setStageSummary(final List<StageSummary> stageSummary) {
        this.stageSummary = stageSummary;
    }

    public List<OwnerSummary> getOwnerSummary() {
        return ownerSummary;
    }

    public void setOwnerSummary(final List<OwnerSummary> ownerSummary) {
        this.ownerSummary = ownerSummary;
    }

    public List<DepartmentSummary> getDepartmentSummary() {
        return departmentSummary;
    }

    public void setDepartmentSummary(final List<DepartmentSummary> departmentSummary) {
        this.departmentSummary = departmentSummary;
    }

    public List<CategorySummary> getCategorySummary() {
        return categorySummary;
    }

    public void setCategorySummary(final List<CategorySummary> categorySummary) {
        this.categorySummary = categorySummary;
    }

    /**
     * Inner class for Stage-wise summary
     */
    public static class StageSummary {
        private int stageId;
        private String stageCode;
        private String stageDescription;
        private int withinTatCount;
        private int beyondTatCount;
        private int totalCount;

        public int getStageId() {
            return stageId;
        }

        public void setStageId(final int stageId) {
            this.stageId = stageId;
        }

        public String getStageCode() {
            return stageCode;
        }

        public void setStageCode(final String stageCode) {
            this.stageCode = stageCode;
        }

        public String getStageDescription() {
            return stageDescription;
        }

        public void setStageDescription(final String stageDescription) {
            this.stageDescription = stageDescription;
        }

        public int getWithinTatCount() {
            return withinTatCount;
        }

        public void setWithinTatCount(final int withinTatCount) {
            this.withinTatCount = withinTatCount;
        }

        public int getBeyondTatCount() {
            return beyondTatCount;
        }

        public void setBeyondTatCount(final int beyondTatCount) {
            this.beyondTatCount = beyondTatCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(final int totalCount) {
            this.totalCount = totalCount;
        }
    }

    /**
     * Inner class for Owner-wise summary
     */
    public static class OwnerSummary {
        private int ownerId;
        private String ownerUserId;
        private String ownerName;
        private int withinTatCount;
        private int beyondTatCount;
        private int totalCount;

        public int getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(final int ownerId) {
            this.ownerId = ownerId;
        }

        public String getOwnerUserId() {
            return ownerUserId;
        }

        public void setOwnerUserId(final String ownerUserId) {
            this.ownerUserId = ownerUserId;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(final String ownerName) {
            this.ownerName = ownerName;
        }

        public int getWithinTatCount() {
            return withinTatCount;
        }

        public void setWithinTatCount(final int withinTatCount) {
            this.withinTatCount = withinTatCount;
        }

        public int getBeyondTatCount() {
            return beyondTatCount;
        }

        public void setBeyondTatCount(final int beyondTatCount) {
            this.beyondTatCount = beyondTatCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(final int totalCount) {
            this.totalCount = totalCount;
        }
    }

    /**
     * Inner class for Department-wise summary
     */
    public static class DepartmentSummary {
        private int departmentId;
        private String departmentName;
        private int withinTatCount;
        private int beyondTatCount;
        private int totalCount;

        public int getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(final int departmentId) {
            this.departmentId = departmentId;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(final String departmentName) {
            this.departmentName = departmentName;
        }

        public int getWithinTatCount() {
            return withinTatCount;
        }

        public void setWithinTatCount(final int withinTatCount) {
            this.withinTatCount = withinTatCount;
        }

        public int getBeyondTatCount() {
            return beyondTatCount;
        }

        public void setBeyondTatCount(final int beyondTatCount) {
            this.beyondTatCount = beyondTatCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(final int totalCount) {
            this.totalCount = totalCount;
        }
    }

    /**
     * Inner class for Category (Replacement Reason) summary
     */
    public static class CategorySummary {
        private int categoryId;
        private String categoryName;
        private int count;
        private BigDecimal percentage;

        public int getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(final int categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(final String categoryName) {
            this.categoryName = categoryName;
        }

        public int getCount() {
            return count;
        }

        public void setCount(final int count) {
            this.count = count;
        }

        public BigDecimal getPercentage() {
            return percentage;
        }

        public void setPercentage(final BigDecimal percentage) {
            this.percentage = percentage;
        }
    }
}

