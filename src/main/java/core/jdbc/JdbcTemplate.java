package core.jdbc;

import org.apache.taglibs.standard.tag.common.sql.DataSourceUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {
    private DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter pss) throws DataAccessException {
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pss.setParameters(pstmt);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.closeConnection(conn);
        }
    }

    public void update(String sql, Object... parameters) {
        update(sql, createPreparedStatementSetter(parameters));
    }

    public void update(PreparedStatementCreator psc, KeyHolder holder) {
        Connection conn = null;

        try {
            conn = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = psc.createPreparedStatement(conn);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                holder.setId(rs.getLong(1));
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.closeConnection(conn);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rm, PreparedStatementSetter pss) {
        List<T> list = query(sql, rm, pss);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rm, Object... parameters) {
        return queryForObject(sql, rm, createPreparedStatementSetter(parameters));
    }

    public <T> List<T> query(String sql, RowMapper<T> rm, PreparedStatementSetter pss) throws DataAccessException {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            pstmt = conn.prepareStatement(sql);
            pss.setParameters(pstmt);
            rs = pstmt.executeQuery();

            List<T> list = new ArrayList<T>();
            while (rs.next()) {
                list.add(rm.mapRow(rs));
            }

            rs.close();
            pstmt.close();
            DataSourceUtils.closeConnection(conn);
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rm, Object... parameters) {
        return query(sql, rm, createPreparedStatementSetter(parameters));
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object... parameters) {
        return new PreparedStatementSetter() {
            @Override
            public void setParameters(PreparedStatement pstmt) throws SQLException {
                for (int i = 0; i < parameters.length; i++) {
                    pstmt.setObject(i + 1, parameters[i]);
                }
            }
        };
    }
}