package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

/**
 * JDBC implementation of the {@link SellerDao} interface.
 * <p>
 * Provides CRUD operations for {@link model.entities.Seller} objects
 * backed by a relational database. Each public method manages its own
 * {@link java.sql.PreparedStatement} and handles SQLExceptions by
 * wrapping them in {@link db.DbException}.
 */
public class SellerDaoJDBC implements SellerDao {

    private Connection conn;

    /**
     * Creates a new DAO using the provided database connection.
     *
     * @param conn an open {@link java.sql.Connection} to be used for all
     *             database operations. The caller is responsible for
     *             managing the connection's lifecycle (closing it when done).
     */
    public SellerDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    /**
     * Inserts the given {@link Seller} into the database.
     * <p>
     * The seller's id field will be populated with the generated key
     * after successful insertion.
     *
     * @param obj the seller to insert; must not be {@code null}
     * @throws db.DbException if a database error occurs or no rows are
     *                         affected by the INSERT statement
     */
    @Override
    public void insert(Seller obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "INSERT INTO seller "
                            + "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
                            + "VALUES "
                            + "(?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, obj.getName());
            st.setString(2, obj.getEmail());
            st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
            st.setDouble(4, obj.getBaseSalary());
            st.setInt(5, obj.getDepartment().getId());

            int rowsAffected = st.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    obj.setId(id);
                    DB.clouseResultSet(rs);
                }
            } else {
                throw new DbException("Unexpected error! No rows affected!");
            }
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.clouseStatement(st);
        }

    }

    /**
     * Updates the database record corresponding to the given seller.
     * The seller's {@code id} must already exist in the database; all
     * other fields will be written to the corresponding columns.
     *
     * @param obj the seller containing updated values; must not be
     *            {@code null} and must have a non-null id
     * @throws db.DbException if a database error occurs during the UPDATE
     */
    @Override
    public void update(Seller obj) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "UPDATE seller "
                    + "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
                    + "WHERE id = ?");
            st.setString(1, obj.getName());
            st.setString(2, obj.getEmail());
            st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
            st.setDouble(4, obj.getBaseSalary());
            st.setInt(5, obj.getDepartment().getId());
            st.setInt(6, obj.getId());

            st.executeUpdate();

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.clouseStatement(st);
        }

    }

    /**
     * Deletes the seller record with the specified id from the database.
     *
     * @param id the id of the seller to remove;
     *           passing {@code null} will result in a SQLException
     * @throws db.DbException if a database error occurs during deletion
     */
    @Override
    public void deleteById(Integer id) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement("DELETE FROM seller WHERE seller.Id = ?");
            
            st.setInt(1, id);
            st.executeUpdate();
            
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.clouseStatement(st);
        }
    }


    /**
     * Retrieves a seller from the database using its id.
     * <p>
     * Performs an inner join with the department table to populate the
     * seller's {@link model.entities.Department} object.
     *
     * @param id the id of the seller to look up
     * @return a {@link Seller} instance if found, or {@code null} if no
     *         matching record exists
     * @throws db.DbException if a database error occurs
     */
    @Override
    public Seller findById(Integer id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT seller.*,department.Name as DepName "
                            + "FROM seller INNER JOIN department "
                            + "ON seller.DepartmentId = department.Id "
                            + "WHERE seller.Id = ?");

            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                Department dep = instantiateDepartment(rs);
                Seller obj = instantiateSeller(rs, dep);
                return obj;
            }
            return null;
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.clouseStatement(st);
            DB.clouseResultSet(rs);
        }
    }

    /**
     * Finds all sellers who belong to the specified department.
     * <p>
     * The returned list is ordered by seller name. A cache map is used to
     * ensure that only one {@link Department} instance is created per
     * department id.
     *
     * @param department the department whose sellers should be retrieved;
     *                   must not be {@code null}
     * @return a list of sellers for the given department (may be empty)
     * @throws db.DbException if a database error occurs
     */
    @Override
    public List<Seller> findByDepartment(Department department) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT seller.*,department.Name as DepName "
                            + "FROM seller INNER JOIN department "
                            + "ON seller.DepartmentId = department.Id "
                            + "WHERE DepartmentId = ? "
                            + "ORDER BY Name");

            st.setInt(1, department.getId());
            rs = st.executeQuery();

            List<Seller> list = new ArrayList<>();
            Map<Integer, Department> mep = new HashMap<>();

            while (rs.next()) {
                Department dep = mep.get(rs.getInt("DepartmentId"));
                if (dep == null) {
                    dep = instantiateDepartment(rs);
                    mep.put(rs.getInt("DepartmentId"), dep);
                }
                Seller obj = instantiateSeller(rs, dep);
                list.add(obj);
            }
            return list;
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.clouseStatement(st);
            DB.clouseResultSet(rs);
        }
    }

    /**
     * Helper method that creates a {@link Seller} entity from the current
     * row of the given {@link ResultSet}. The associated department
     * instance is provided by the caller (either created or cached).
     *
     * @param rs  the result set positioned at a valid row
     * @param dep the department to assign to the seller
     * @return a fully populated {@link Seller} object
     * @throws SQLException if an error occurs while reading from the result set
     */
    private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException {
        Seller obj = new Seller();
        obj.setId(rs.getInt("Id"));
        obj.setName(rs.getNString("Name"));
        obj.setEmail(rs.getNString("Email"));
        obj.setBaseSalary(rs.getDouble("BaseSalary"));
        obj.setBirthDate(rs.getDate("BirthDate"));
        obj.setDepartment(dep);
        return obj;

    }

    /**
     * Helper method that constructs a {@link Department} object using
     * data from the current row of the provided {@link ResultSet}.
     *
     * @param rs the result set positioned at a valid row
     * @return a {@link Department} populated with id and name
     * @throws SQLException if an error occurs accessing the result set
     */
    private Department instantiateDepartment(ResultSet rs) throws SQLException {
        Department dep = new Department();
        dep.setId(rs.getInt("DepartmentId"));
        dep.setName(rs.getNString("DepName"));
        return dep;
    }

    /**
     * Retrieves all sellers from the database, ordered by name.
     * <p>
     * Each seller is joined with its department, reusing department
     * instances via a local cache to avoid duplicates.
     *
     * @return a list containing every seller in the system (empty if none)
     * @throws db.DbException if a database error occurs
     */
    @Override
    public List<Seller> finAll() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT seller.*,department.Name as DepName "
                            + "FROM seller INNER JOIN department "
                            + "ON seller.DepartmentId = department.Id "
                            + "ORDER BY Name");

            rs = st.executeQuery();

            List<Seller> list = new ArrayList<>();
            Map<Integer, Department> mep = new HashMap<>();

            while (rs.next()) {
                Department dep = mep.get(rs.getInt("DepartmentId"));
                if (dep == null) {
                    dep = instantiateDepartment(rs);
                    mep.put(rs.getInt("DepartmentId"), dep);
                }
                Seller obj = instantiateSeller(rs, dep);
                list.add(obj);
            }
            return list;
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.clouseStatement(st);
            DB.clouseResultSet(rs);
        }

    }

}
