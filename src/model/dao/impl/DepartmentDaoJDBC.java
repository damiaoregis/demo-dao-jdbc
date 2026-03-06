package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import db.DB;
import db.DbException;
import model.dao.DepartmentDao;
import model.entities.Department;

/**
 * JDBC implementation of the {@link DepartmentDao} interface.
 * <p>
 * Handles CRUD operations for {@link model.entities.Department} entities.
 * Errors from JDBC are wrapped in {@link db.DbException} to keep the
 * service layer decoupled from SQL specifics.
 */
public class DepartmentDaoJDBC implements DepartmentDao {
    Connection conn;

    /**
     * Creates a new DAO instance using the provided connection.
     *
     * @param conn active JDBC connection; the caller manages its lifecycle
     */
    public DepartmentDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    /**
     * Persists the given department in the database.
     * <p>
     * After a successful insert the department object's id field is set
     * to the generated key returned by the database.
     *
     * @param obj department to be inserted (must not be null)
     * @throws db.DbException if the insert fails or affects no rows
     */
    @Override
    public void insert(Department obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement("INSERT INTO department (Name) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, obj.getName());

            int rowsAffected = st.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    obj.setId(id);
                    DB.clouseResultSet(rs);
                } else {
                    throw new DbException("Unexpected error! No rows affected!");
                }
            }
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }finally{
            DB.clouseStatement(st);
        }
    }

    /**
     * Updates an existing department record in the database.
     *
     * @param obj department containing updated values; its id must match
     *            an existing record
     * @throws db.DbException if a database error occurs during update
     */
    @Override
    public void update(Department obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement("UPDATE department SET Name = ? WHERE id = ?");
            st.setString(1, obj.getName());
            st.setInt(2, obj.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }finally{

        }
    }

    /**
     * Removes the department record with the specified id.
     *
     * @param id id of the department to delete
     * @throws db.DbException if a database error occurs (e.g.
     *                         constraint violation)
     */
    @Override
    public void deleteById(Integer id) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement("DELETE FROM department WHERE department.Id = ?");

            st.setInt(1, id);
            st.executeUpdate();

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.clouseStatement(st);
        }
    }

    /**
     * Factory helper that creates a {@link Department} from the current
     * row of the supplied {@link ResultSet}.
     *
     * @param rs result set positioned at a valid row
     * @return a populated Department instance
     * @throws SQLException if an error occurs reading values from the result set
     */
    private Department instantiateDepartment(ResultSet rs) throws SQLException {
        Department dep = new Department();
        dep.setId(rs.getInt("Id"));
        dep.setName(rs.getNString("Name"));
        return dep;
    }

    /**
     * Retrieves a department by its primary key.
     *
     * @param id identifier of the department to fetch
     * @return the Department object if found, otherwise {@code null}
     * @throws db.DbException if a database error occurs
     */
    @Override
    public Department findById(Integer id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement("SELECT * FROM department WHERE Id = ?");
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                Department obj = instantiateDepartment(rs);
                return obj;
            }
            return null;
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.clouseResultSet(rs);
            DB.clouseStatement(st);
        }
    }

    /**
     * Fetches all departments from the database, ordered by name.
     *
     * @return list of all departments (never {@code null}, but may be empty)
     * @throws db.DbException if a database error occurs
     */
    @Override
    public List<Department> finAll() {

        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT department.*,department.Name as DepName "
                            + "FROM department "
                            + "ORDER BY Name");
            rs = st.executeQuery();
            List<Department> list = new ArrayList<>();
            while (rs.next()) {
                Department obj = instantiateDepartment(rs);
                list.add(obj);
            }
            return list;
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.clouseResultSet(rs);
            DB.clouseStatement(st);
        }
    }

}
