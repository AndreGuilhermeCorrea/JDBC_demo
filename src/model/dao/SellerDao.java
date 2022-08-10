package model.dao;

import java.util.List;

import model.entities.Department;
import model.entities.Seller;

public interface SellerDao {

	void insert(Seller dep);

	void update(Seller dep);

	void deleteById(Integer id);

	//consulta do objeto com id no banco de dados
	Seller findById(Integer id);
	//lista todos os vendedores
	List<Seller> findAll();
	//lista todos os vendedores por departamento
	List<Seller> findByDepartment(Department department);

}
