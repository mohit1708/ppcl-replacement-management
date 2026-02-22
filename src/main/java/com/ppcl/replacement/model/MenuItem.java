package com.ppcl.replacement.model;

import java.io.Serializable;

public class MenuItem implements Serializable {

    private int id;
    private String menuKey;
    private String sectionLabel;
    private String label;
    private String url;
    private String iconClass;
    private int displayOrder;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMenuKey() { return menuKey; }
    public void setMenuKey(String menuKey) { this.menuKey = menuKey; }

    public String getSectionLabel() { return sectionLabel; }
    public void setSectionLabel(String sectionLabel) { this.sectionLabel = sectionLabel; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
