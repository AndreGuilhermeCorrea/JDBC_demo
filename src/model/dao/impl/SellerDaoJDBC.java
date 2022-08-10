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

public class SellerDaoJDBC implements SellerDao {

	// objeto de conexão com o banco diponivel em toda a classe
	private Connection conn;

	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Seller dep) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("INSERT INTO seller " + "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
					+ "VALUES " + "(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

			st.setString(1, dep.getName());
			st.setString(2, dep.getEmail());
			st.setDate(3, new java.sql.Date(dep.getBirthDate().getTime()));
			st.setDouble(4, dep.getBaseSalary());
			st.setInt(5, dep.getDepartment().getId());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected > 0) {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next()) {
					int id = rs.getInt(1);
					dep.setId(id);
				}
				DB.closeResultSet(rs);
			} else {
				// exceção caso nenhuma linha seja alterada
				throw new DbException("Erro! Nenhuma linha alterada!");
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Seller dep) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("UPDATE seller "
					+ "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? " + "WHERE Id = ?");

			st.setString(1, dep.getName());
			st.setString(2, dep.getEmail());
			st.setDate(3, new java.sql.Date(dep.getBirthDate().getTime()));
			st.setDouble(4, dep.getBaseSalary());
			st.setInt(5, dep.getDepartment().getId());
			st.setInt(6, dep.getId());

			st.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("DELETE FROM seller WHERE Id = ?");

			st.setInt(1, id);

			st.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public Seller findById(Integer id) {
		PreparedStatement st = null;
		// retorna os objetos em forma de linha e coluna
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName " + "FROM seller INNER JOIN department "
							+ "ON seller.DepartmentId = department.Id " + "WHERE seller.Id = ?");
			// configurando o ?
			st.setInt(1, id);
			// consulta sql
			rs = st.executeQuery();
			// teste para saber se encontrou resultado
			if (rs.next()) {
				// navegar pelos dados para instanciar os objetos
				Department dep1 = instantiateDepartment(rs);
				// objeto seller com os atributos definidos e com melhorias aplicadas com função
				// a parte e sendo portanto reaproveitada
				Seller dep = instantiateSeller(rs, dep1);
				// retorno do objeto seller
				return dep;
			}
			// caso nao encontre registro(vendedor)
			return null;
		}
		// captura de possível exceção
		catch (SQLException e) {
			// caso ocorra possível exceção será lancada a exceção personalizada passando a
			// mensagem
			throw new DbException(e.getMessage());
		}
		// fechar os recursos
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
			// conexão nao fechada para poder ser utilizada na classe
		}
	}

	@Override
	// busca todos os vendedores e ordenar por nome
	public List<Seller> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName " + "FROM seller INNER JOIN department "
							+ "ON seller.DepartmentId = department.Id " + "ORDER BY Name");

			rs = st.executeQuery();

			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();

			while (rs.next()) {

				Department dep1 = map.get(rs.getInt("DepartmentId"));

				if (dep1 == null) {
					dep1 = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep1);
				}

				Seller dep = instantiateSeller(rs, dep1);
				list.add(dep);
			}
			return list;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	// função auxiliar a parte para ser reaproveitada com a exceção propagada
	private Seller instantiateSeller(ResultSet rs, Department dep1) throws SQLException {
		Seller dep = new Seller();
		dep.setId(rs.getInt("Id"));
		dep.setName(rs.getString("Name"));
		dep.setEmail(rs.getString("Email"));
		dep.setBaseSalary(rs.getDouble("BaseSalary"));
		dep.setBirthDate(rs.getDate("BirthDate"));
		dep.setDepartment(dep1);
		return dep;
	}

	// função auxiliar a parte para ser reaproveitada com a exceção propagada
	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		Department dep1 = new Department();
		dep1.setId(rs.getInt("DepartmentId"));
		dep1.setName(rs.getString("DepName"));
		return dep1;
	}

	@Override
	// listar os vendedores dado um departamento
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement st = null;
		// retorna os objetos em forma de linha e coluna
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName " + "FROM seller INNER JOIN department "
							+ "ON seller.DepartmentId = department.Id " + "WHERE DepartmentId = ? " + "ORDER BY Name");

			// configurar valor da interrogação
			st.setInt(1, department.getId());

			rs = st.executeQuery();

			List<Seller> list = new ArrayList<>();
			// estrutura map vazia
			Map<Integer, Department> map = new HashMap<>();

			while (rs.next()) {

				// controle dentro da estrutura enquanto para saber se o departamento ja existe,
				// através de uma teste com o id correspondente
				Department dep1 = map.get(rs.getInt("DepartmentId"));

				if (dep1 == null) {
					// instanciação do departamento caso o id do departamento seja nulo
					dep1 = instantiateDepartment(rs);
					// armazenamento do departamento no map
					map.put(rs.getInt("DepartmentId"), dep1);
				}
				// instanciação do vendedor de acordo com o departamento seja ele um ja
				// existente ou novo
				Seller dep = instantiateSeller(rs, dep1);
				list.add(dep);
			}
			return list;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}
}
