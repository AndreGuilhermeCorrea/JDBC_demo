package model.dao;

import java.util.List;

import model.entities.Department;

public interface DepartmentDao {

	void insert(Department dep);

	void update(Department dep);

	void deleteById(Integer id);

	//consulta do objeto com id no banco de dados
	Department findById(Integer id);
	//lista todos os departamentos
	List<Department> findAll();

}
