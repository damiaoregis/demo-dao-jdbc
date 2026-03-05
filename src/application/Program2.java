package application;


import java.util.ArrayList;
import java.util.List;

import model.dao.DaoFactory;
import model.dao.DepartmentDao;
import model.entities.Department;

public class Program2 {

	public static void main(String[] args) {
		//Scanner sc = new Scanner(System.in);

		DepartmentDao departmentDao = DaoFactory.createDepartmentDao();
		System.out.println("=== TEST 1: department findById =====");
		Department department = departmentDao.findById(2);
		System.out.println(department);

        List<Department>list= new ArrayList<>();
		System.out.println("\n=== TEST 2: Department findAll =====");
		list = departmentDao.finAll();
		for (Department obj : list) {
			System.out.println(obj);
		}
		/* 

		System.out.println("\n=== TEST 4: seller insert =====");
		Seller newSeller = new Seller(null, "Sofya", "sofya@uol.com", new Date(), 4000.0, department);
		sellerDao.insert(newSeller);
		System.out.println("Inserted! New id: " + newSeller.getId());

		System.out.println("\n=== TEST 5: seller update =====");
		seller = sellerDao.findById(11);
		seller.setName("Mylle Bob Brown");
		seller.setEmail("mylle@bow.com");
		sellerDao.update(seller);
		System.out.println("Update completed");

		System.out.println("\n=== TEST 6: seller delete =====");
		System.out.println("Enter id for delete test: ");
		int id = sc.nextInt();
        sellerDao.deleteById(id);
		System.out.println("Delete completed");
		sc.close();*/
	}
}