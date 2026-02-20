package com.sky.mapper;

import com.sky.entity.Employee;
import com.sky.vo.EmployeeLoginVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    List<Employee> list(String name);

    void addEmployee(Employee employee);
}
