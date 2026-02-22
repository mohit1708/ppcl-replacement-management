package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.MenuItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO extends BaseDAO {

    /**
     * Returns the list of active menu items the given user is allowed to see,
     * ordered by DISPLAY_ORDER.
     * <p>
     * Access rules stored in MENU_ITEM_ACCESS are evaluated as follows:
     * <ul>
     *   <li><b>No rules</b> for a menu item → visible to everyone.</li>
     *   <li><b>DESIGNATION</b> – user's EMP.DESIGNATION must match ACCESS_VALUE.</li>
     *   <li><b>HIERARCHY_FROM</b> – user must be in the reporting chain above
     *       the designation given in ACCESS_VALUE (uses CONNECT BY).</li>
     *   <li><b>DEPARTMENT</b> – user's USER_ACCOUNT.DEPT must match ACCESS_VALUE
     *       (case-insensitive).</li>
     * </ul>
     * Multiple rules for the same menu item are OR-ed.
     *
     * @param userAccountId the USER_ACCOUNT.ID of the logged-in user
     * @return ordered list of accessible {@link MenuItem}s
     * @throws SQLException if a database access error occurs
     */
    public List<MenuItem> getMenuItemsForUser(final int userAccountId) throws SQLException {

        final String sql = """
            WITH user_info AS (
                SELECT ua.EMP_ID,
                       e.DESIGNATION,
                       UPPER(ua.DEPT) AS DEPT
                FROM USER_ACCOUNT ua
                JOIN EMP e ON e.ID = ua.EMP_ID
                WHERE ua.ID = ?
            )
            SELECT mi.ID, mi.MENU_KEY, mi.SECTION_LABEL, mi.LABEL,
                   mi.URL, mi.ICON_CLASS, mi.DISPLAY_ORDER
            FROM MENU_ITEM mi
            WHERE mi.IS_ACTIVE = 1
              AND (
                  -- no access rules → visible to all
                  NOT EXISTS (
                      SELECT 1 FROM MENU_ITEM_ACCESS mia
                      WHERE mia.MENU_ITEM_ID = mi.ID
                  )
                  OR EXISTS (
                      SELECT 1
                      FROM MENU_ITEM_ACCESS mia, user_info ui
                      WHERE mia.MENU_ITEM_ID = mi.ID
                        AND (
                            -- exact designation match
                            (mia.ACCESS_TYPE = 'DESIGNATION'
                             AND TO_NUMBER(mia.ACCESS_VALUE) = ui.DESIGNATION)
                            OR
                            -- hierarchy: user is at or above the given designation
                            (mia.ACCESS_TYPE = 'HIERARCHY_FROM'
                             AND ui.EMP_ID IN (
                                 SELECT e.ID FROM EMP e
                                 START WITH e.DESIGNATION = TO_NUMBER(mia.ACCESS_VALUE)
                                 CONNECT BY NOCYCLE PRIOR e.REPORTING_TO = e.ID
                             ))
                            OR
                            -- department match
                            (mia.ACCESS_TYPE = 'DEPARTMENT'
                             AND UPPER(mia.ACCESS_VALUE) = ui.DEPT)
                        )
                  )
              )
            ORDER BY mi.DISPLAY_ORDER
            """;

        final List<MenuItem> items = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userAccountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final MenuItem item = new MenuItem();
                    item.setId(rs.getInt("ID"));
                    item.setMenuKey(rs.getString("MENU_KEY"));
                    item.setSectionLabel(rs.getString("SECTION_LABEL"));
                    item.setLabel(rs.getString("LABEL"));
                    item.setUrl(rs.getString("URL"));
                    item.setIconClass(rs.getString("ICON_CLASS"));
                    item.setDisplayOrder(rs.getInt("DISPLAY_ORDER"));
                    items.add(item);
                }
            }
        }

        return items;
    }
}
