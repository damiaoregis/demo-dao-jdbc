package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import db.DB;
import db.DbException;
import model.dao.DepartmentDao;
import model.entities.Department;

public class DepartmentDaoJDBC implements DepartmentDao {
    Connection conn;

    public DepartmentDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(Department obj) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insert'");
    }

    @Override
    public void update(Department obj) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

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
    private Department instantiateDepartment(ResultSet rs) throws SQLException {
        Department dep = new Department();
        dep.setId(rs.getInt("Id"));
        dep.setName(rs.getNString("Name"));
        return dep;
    }

    @Override
    public Department findById(Integer id) {
       PreparedStatement st = null;
       ResultSet rs = null;
       try{
          st = conn.prepareStatement("SELECT * FROM department WHERE Id = ?");
          st.setInt(1, id);
          rs = st.executeQuery();
          if (rs.next()){
            Department obj = instantiateDepartment(rs);
            return obj;
          }
       return null;
       }catch( SQLException e){
            throw new DbException(e.getMessage());
       }finally {
        DB.clouseResultSet(rs);
        DB.clouseStatement(st);
       }
    }

    @Override
    public List<Department> finAll() {
       
           PreparedStatement st = null;
       ResultSet rs = null;
       try{
          st = conn.prepareStatement(
             "SELECT department.*,department.Name as DepName "
                            + "FROM department "
                            + "ORDER BY Name");
          rs = st.executeQuery();
          List<Department> list = new ArrayList<>(); 
          while (rs.next()){
              Department obj = instantiateDepartment(rs);
              list.add(obj);
          }
       return list;
       }catch( SQLException e){
            throw new DbException(e.getMessage());
       }finally {
        DB.clouseResultSet(rs);
        DB.clouseStatement(st);
       }
    }

}
